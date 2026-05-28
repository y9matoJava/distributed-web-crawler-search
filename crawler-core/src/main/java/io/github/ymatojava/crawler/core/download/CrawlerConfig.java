package io.github.ymatojava.crawler.core.download;

import java.time.Duration;

/**
 * Конфигурация краулера, определяющая параметры загрузки и обхода страниц.
 *
 * Использование record обеспечивает неизменяемость (immutability),
 * что позволяет безопасно разделять объект конфигурации между потоками.
 *
 * Пример использования:
 * <pre>
 *   // Конфигурация по умолчанию
 *   CrawlerConfig config = CrawlerConfig.defaults();
 *
 *   // Кастомная конфигурация для агрессивного краулинга (только для тестов!)
 *   CrawlerConfig testConfig = new CrawlerConfig(
 *       Duration.ofSeconds(5), "TestBot/1.0", 1_000_000, 3, Duration.ZERO, 10, 3
 *   );
 * </pre>
 *
 * @param downloadTimeout   Максимальное время ожидания ответа от сервера
 * @param userAgent         Строка User-Agent, отправляемая в HTTP-заголовке
 * @param maxBodySizeBytes  Максимальный размер тела ответа (защита от загрузки огромных файлов)
 * @param maxRedirects      Максимальное количество HTTP-редиректов (защита от бесконечных цепочек)
 * @param politenessDelay   Задержка между запросами к одному домену (ответственный краулинг)
 * @param maxPages          Максимальное количество страниц для скачивания за один запуск
 * @param maxDepth          Максимальная глубина BFS-обхода от стартовой страницы
 */
public record CrawlerConfig(
        Duration downloadTimeout,
        String userAgent,
        long maxBodySizeBytes,
        int maxRedirects,
        Duration politenessDelay,
        int maxPages,
        int maxDepth
) {

    /**
     * Создаёт конфигурацию с разумными значениями по умолчанию.
     *
     * Параметры подобраны для ответственного краулинга:
     * - 10 секунд таймаут — достаточно для большинства серверов;
     * - 5 МБ лимит — HTML-страницы редко превышают этот размер;
     * - 1 секунда задержка — стандартная вежливость для краулеров;
     * - 100 страниц и глубина 5 — безопасные лимиты для тестирования.
     *
     * @return Объект конфигурации с дефолтными значениями
     */
    public static CrawlerConfig defaults() {
        return new CrawlerConfig(
                Duration.ofSeconds(10),                                                          // таймаут загрузки
                "DistributedCrawler/0.1 (+https://github.com/y9matoJava/distributed-web-crawler-search)", // User-Agent
                5 * 1024 * 1024,                                                                 // 5 МБ лимит
                5,                                                                               // макс. редиректов
                Duration.ofSeconds(1),                                                           // вежливая задержка
                100,                                                                             // макс. страниц
                5                                                                                // макс. глубина BFS
        );
    }
}
