package io.github.ymatojava.crawler.core.engine;

import io.github.ymatojava.crawler.core.parse.ParsedPage;

import java.time.Duration;
import java.util.List;

/**
 * Неизменяемая модель результата сессии обхода (crawl session).
 *
 * <p>Содержит все данные, накопленные за одну сессию работы краулера:
 * <ul>
 *     <li>Список распарсенных страниц</li>
 *     <li>Общее количество обнаруженных URL (включая отфильтрованные и не загруженные)</li>
 *     <li>Длительность обхода</li>
 * </ul>
 *
 * @param pages              список всех успешно загруженных и распарсенных страниц
 * @param totalUrlsDiscovered общее число уникальных URL, обнаруженных фронтиром за сессию
 * @param elapsed            время, затраченное на обход
 */
public record CrawlResult(
        List<ParsedPage> pages,
        int totalUrlsDiscovered,
        Duration elapsed
) {
    /**
     * Компактный конструктор с защитным копированием списка страниц.
     */
    public CrawlResult {
        pages = pages != null ? List.copyOf(pages) : List.of();
        if (elapsed == null) {
            elapsed = Duration.ZERO;
        }
    }
}
