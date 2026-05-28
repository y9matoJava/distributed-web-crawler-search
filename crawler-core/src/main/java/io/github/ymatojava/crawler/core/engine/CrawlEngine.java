package io.github.ymatojava.crawler.core.engine;

import io.github.ymatojava.crawler.core.download.DownloadResult;
import io.github.ymatojava.crawler.core.download.PageDownloader;
import io.github.ymatojava.crawler.core.parse.PageParser;
import io.github.ymatojava.crawler.core.parse.ParsedPage;
import io.github.ymatojava.crawler.core.url.NormalizedUrl;
import io.github.ymatojava.crawler.core.url.UrlCanonicalizer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Движок in-memory краулера (BFS алгоритм).
 *
 * Управляет циклом: Извлечь URL из Frontier -> Скачать -> Распарсить -> Добавить новые ссылки во Frontier.
 */
public class CrawlEngine {

    private static final Logger log = Logger.getLogger(CrawlEngine.class.getName());

    private final PageDownloader downloader;
    private final PageParser parser;
    private final UrlFrontier frontier;
    private final UrlCanonicalizer canonicalizer;
    private final int maxPages;

    /**
     * Создает движок обхода.
     *
     * @param downloader Компонент для скачивания HTML
     * @param parser     Компонент для парсинга HTML
     * @param frontier   Очередь URL
     * @param maxPages   Максимальное количество страниц, после которого обход остановится
     */
    public CrawlEngine(PageDownloader downloader, PageParser parser, UrlFrontier frontier, int maxPages) {
        this.downloader = downloader;
        this.parser = parser;
        this.frontier = frontier;
        this.canonicalizer = new UrlCanonicalizer(); // Упрощение: создаем внутри
        this.maxPages = maxPages;
    }

    /**
     * Запускает процесс краулинга с указанного стартового URL.
     *
     * @param seedUrl Начальная страница обхода
     * @return {@link CrawlResult} Итог обхода
     */
    public CrawlResult crawl(String seedUrl) {
        Instant start = Instant.now();
        List<ParsedPage> successfulPages = new ArrayList<>();

        // 1. Нормализация и добавление стартового URL
        Optional<NormalizedUrl> normalizedOpt = canonicalizer.canonicalize(seedUrl, null);
        if (normalizedOpt.isEmpty()) {
            log.warning("Invalid seed URL: " + seedUrl);
            return new CrawlResult(successfulPages, 0, Duration.between(start, Instant.now()));
        }
        
        frontier.add(normalizedOpt.get().value());

        // 2. Основной цикл BFS
        while (!frontier.isEmpty() && successfulPages.size() < maxPages) {
            String currentUrl = frontier.poll().orElseThrow();

            log.info("Downloading: " + currentUrl);
            DownloadResult result = downloader.download(currentUrl);

            // 3. Обработка результата скачивания (Pattern Matching for switch - Java 21)
            switch (result) {
                case DownloadResult.Success s -> {
                    // Парсинг успешного ответа
                    ParsedPage page = parser.parse(s.url(), s.body());
                    successfulPages.add(page);
                    
                    // Добавление новых ссылок в очередь
                    frontier.addAll(page.outLinks());
                    log.info("Parsed " + s.url() + " | Outlinks: " + page.outLinks().size());
                }
                case DownloadResult.Failure f -> {
                    // Логирование ошибки
                    log.warning("Failed to download " + f.url() + " | Reason: " + f.reason());
                }
            }
        }

        Instant end = Instant.now();
        log.info(String.format("Crawl completed. Pages: %d, Discovered URLs: %d, Time: %d ms",
                successfulPages.size(), frontier.totalDiscovered(), Duration.between(start, end).toMillis()));

        return new CrawlResult(successfulPages, frontier.totalDiscovered(), Duration.between(start, end));
    }
}
