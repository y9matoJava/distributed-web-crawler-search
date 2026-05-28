package io.github.ymatojava.crawler.worker.pipeline;

import io.github.ymatojava.crawler.common.model.UrlStatus;
import io.github.ymatojava.crawler.common.url.UrlHash;
import io.github.ymatojava.crawler.core.download.DownloadResult;
import io.github.ymatojava.crawler.core.download.PageDownloader;
import io.github.ymatojava.crawler.core.parse.PageParser;
import io.github.ymatojava.crawler.core.parse.ParsedPage;
import io.github.ymatojava.crawler.worker.entity.PageEntity;
import io.github.ymatojava.crawler.worker.entity.UrlEntity;
import io.github.ymatojava.crawler.worker.mq.UrlMessage;
import io.github.ymatojava.crawler.worker.mq.UrlProducer;
import io.github.ymatojava.crawler.worker.repository.PageRepository;
import io.github.ymatojava.crawler.worker.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.logging.Logger;

/**
 * Главный конвейер (Pipeline) обработки одного URL.
 * 
 * Заменяет собой In-Memory CrawlEngine из ядра. Теперь очередь находится в RabbitMQ,
 * а состояние графа — в PostgreSQL. Этот сервис берет один URL, скачивает его,
 * распарсивает, сохраняет данные в БД и генерирует новые сообщения для найденных ссылок.
 */
@Service
public class WorkerPipeline {

    private static final Logger log = Logger.getLogger(WorkerPipeline.class.getName());
    private static final int MAX_DEPTH = 3; // Временный хардкод, позже вынесем в конфигурацию

    private final UrlRepository urlRepository;
    private final PageRepository pageRepository;
    private final UrlProducer urlProducer;
    
    // Компоненты из модуля crawler-core
    private final PageDownloader downloader;
    private final PageParser parser;
    
    // Компоненты вежливости (Politeness)
    private final io.github.ymatojava.crawler.worker.politeness.PolitenessLimiter politenessLimiter;
    private final io.github.ymatojava.crawler.worker.politeness.RobotsTxtService robotsTxtService;

    public WorkerPipeline(UrlRepository urlRepository,
                          PageRepository pageRepository,
                          UrlProducer urlProducer,
                          PageDownloader downloader,
                          PageParser parser,
                          io.github.ymatojava.crawler.worker.politeness.PolitenessLimiter politenessLimiter,
                          io.github.ymatojava.crawler.worker.politeness.RobotsTxtService robotsTxtService) {
        this.urlRepository = urlRepository;
        this.pageRepository = pageRepository;
        this.urlProducer = urlProducer;
        this.downloader = downloader;
        this.parser = parser;
        this.politenessLimiter = politenessLimiter;
        this.robotsTxtService = robotsTxtService;
    }

    /**
     * Основной метод обработки одного задания.
     * Использует транзакцию БД: либо все сохраняется, либо ничего.
     * 
     * @param message Задание из RabbitMQ
     */
    @Transactional
    public void process(UrlMessage message) {
        log.info("Начало обработки URL: " + message.url());

        // 1. Ищем сущность URL в базе. Если кто-то уже пометил её как CRAWLED — пропускаем.
        UrlEntity urlEntity = urlRepository.findById(message.urlId())
                .orElseThrow(() -> new IllegalStateException("URL не найден в БД: " + message.urlId()));

        if (urlEntity.getStatus() != UrlStatus.DISCOVERED) {
            log.warning("URL уже обработан (статус " + urlEntity.getStatus() + "). Пропуск.");
            return;
        }

        // 1.5. Проверка Robots.txt и Rate Limiting (Politeness Policy)
        if (!robotsTxtService.isAllowed(message.url())) {
            log.warning("URL запрещен в robots.txt: " + message.url());
            updateUrlStatus(urlEntity, UrlStatus.SKIPPED);
            return;
        }
        
        politenessLimiter.applyDelay(message.url());

        // 2. Скачивание страницы (блокирующая сетевая операция)
        DownloadResult downloadResult = downloader.download(message.url());

        switch (downloadResult) {
            case DownloadResult.Failure f -> {
                log.warning("Ошибка скачивания: " + f.reason());
                updateUrlStatus(urlEntity, UrlStatus.FAILED);
                // Бросаем исключение, чтобы RabbitMQ отправил сообщение в DLQ, 
                // либо можно просто завершить метод, если это перманентная ошибка.
                // Пока просто помечаем как FAILED.
            }
            case DownloadResult.Success s -> {
                // 3. Парсинг страницы
                ParsedPage parsedPage = parser.parse(s.url(), s.body());
                
                // Временно извлекаем ключевые слова здесь (в будущем вынесем в отдельный бин)
                io.github.ymatojava.crawler.core.keyword.KeywordExtractor extractor = 
                        new io.github.ymatojava.crawler.core.keyword.KeywordExtractor();
                java.util.List<String> keywords = extractor.extract(parsedPage.bodyText());

                // 4. Сохранение распарсенной страницы в БД
                PageEntity pageEntity = new PageEntity(
                        urlEntity,
                        parsedPage.url(),
                        parsedPage.title(),
                        parsedPage.bodyText(),
                        keywords
                );
                pageRepository.save(pageEntity);

                // 5. Обработка новых исходящих ссылок
                processOutLinks(parsedPage.outLinks(), message.depth());

                // 6. Обновление статуса текущего URL
                updateUrlStatus(urlEntity, UrlStatus.CRAWLED);
                
                log.info("Успешно обработан URL: " + s.url() + ". Найдено ссылок: " + parsedPage.outLinks().size());
            }
        }
    }

    /**
     * Обрабатывает найденные на странице ссылки:
     * - Дедуплицирует (проверяет наличие в БД)
     * - Сохраняет новые в БД со статусом DISCOVERED
     * - Отправляет их в RabbitMQ для последующего обхода
     */
    private void processOutLinks(java.util.Set<String> outLinks, int currentDepth) {
        // Проверяем ограничение глубины
        if (currentDepth >= MAX_DEPTH) {
            log.info("Достигнута максимальная глубина (" + MAX_DEPTH + "). Новые ссылки не добавляются.");
            return;
        }

        int newDepth = currentDepth + 1;

        for (String link : outLinks) {
            String hash = UrlHash.sha256(link);
            
            // Если ссылки еще нет в БД — сохраняем и отправляем в очередь
            if (!urlRepository.existsByUrlHash(hash)) {
                // В реальном проекте здесь используется saveAll() для батчинга,
                // но для образовательных целей оставим так для наглядности.
                UrlEntity newUrl = UrlEntity.discovered(link, hash);
                urlRepository.save(newUrl); // Получаем ID

                urlProducer.sendToReadyQueue(new UrlMessage(newUrl.getId(), link, newDepth));
            }
        }
    }

    private void updateUrlStatus(UrlEntity urlEntity, UrlStatus status) {
        urlEntity.setStatus(status);
        urlEntity.setCrawledAt(Instant.now());
        urlRepository.save(urlEntity);
    }
}
