package io.github.ymatojava.crawler.common.model;

import java.time.Instant;

/**
 * Неизменяемый объект данных (DTO), представляющий URL в системе краулера.
 *
 * Каждый URL, обнаруженный при обходе страниц, регистрируется как CrawlUrl
 * и проходит через состояния жизненного цикла {@link UrlStatus}.
 *
 * Использование Java Record гарантирует:
 * - неизменяемость полей (immutability) — критично для многопоточного краулера;
 * - автоматическую генерацию equals(), hashCode(), toString();
 * - компактность кода без потери выразительности.
 *
 * @param id           Уникальный идентификатор записи в БД (null для ещё не сохранённых)
 * @param url          Каноникализированный URL-адрес
 * @param urlHash      SHA-256 хеш каноникализированного URL (используется для дедупликации)
 * @param status       Текущий статус обработки URL
 * @param discoveredAt Момент времени, когда URL был впервые обнаружен
 * @param crawledAt    Момент времени, когда URL был обработан (null, если ещё не обработан)
 */
public record CrawlUrl(
        Long id,
        String url,
        String urlHash,
        UrlStatus status,
        Instant discoveredAt,
        Instant crawledAt
) {

    /**
     * Компактный конструктор с валидацией обязательных полей.
     * Вызывается автоматически при создании экземпляра record.
     */
    public CrawlUrl {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL не может быть null или пустым");
        }
        if (urlHash == null || urlHash.isBlank()) {
            throw new IllegalArgumentException("Хеш URL не может быть null или пустым");
        }
        if (status == null) {
            throw new IllegalArgumentException("Статус URL не может быть null");
        }
    }

    /**
     * Фабричный метод для создания нового CrawlUrl в состоянии DISCOVERED.
     * Используется при первом обнаружении URL во время краулинга.
     *
     * @param url     Каноникализированный URL
     * @param urlHash SHA-256 хеш URL
     * @return Новый объект CrawlUrl со статусом DISCOVERED
     */
    public static CrawlUrl discovered(String url, String urlHash) {
        return new CrawlUrl(null, url, urlHash, UrlStatus.DISCOVERED, Instant.now(), null);
    }
}
