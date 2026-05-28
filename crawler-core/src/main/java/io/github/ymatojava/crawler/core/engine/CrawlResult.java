package io.github.ymatojava.crawler.core.engine;

import io.github.ymatojava.crawler.core.parse.ParsedPage;

import java.time.Duration;
import java.util.List;

/**
 * Неизменяемый объект данных, содержащий итоговый результат сессии краулинга.
 *
 * @param pages                 Список всех успешно скачанных и распарсенных страниц
 * @param totalUrlsDiscovered   Общее количество уникальных найденных ссылок (размер графа)
 * @param elapsed               Затраченное время на краулинг
 */
public record CrawlResult(
        List<ParsedPage> pages,
        int totalUrlsDiscovered,
        Duration elapsed
) {
    public CrawlResult {
        pages = pages != null ? List.copyOf(pages) : List.of();
    }
}
