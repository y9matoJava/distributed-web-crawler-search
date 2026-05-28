package io.github.ymatojava.crawler.worker.entity;

import io.github.ymatojava.crawler.common.model.UrlStatus;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA сущность, описывающая URL-адрес в базе данных PostgreSQL.
 *
 * Маппится на таблицу "crawl_urls". Эта таблица служит Persistent URL Frontier —
 * распределенной очередью обхода (состояние "DISCOVERED") и реестром всех
 * когда-либо встреченных ссылок для предотвращения циклов обхода.
 */
@Entity
@Table(name = "crawl_urls")
public class UrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Каноникализированный URL. Хранится как TEXT, так как URL могут быть длинными.
     */
    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    /**
     * SHA-256 хеш нормализованного URL.
     * Используется для быстрого O(1) поиска по базе (индекс) и обеспечения уникальности.
     * Длина ровно 64 символа.
     */
    @Column(name = "url_hash", nullable = false, unique = true, length = 64)
    private String urlHash;

    /**
     * Текущий статус обработки URL.
     * Хранится как строка для совместимости и читаемости в БД.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UrlStatus status;

    @Column(name = "discovered_at", nullable = false, updatable = false)
    private Instant discoveredAt;

    @Column(name = "crawled_at")
    private Instant crawledAt;

    // Конструктор по умолчанию для JPA (требование спецификации)
    protected UrlEntity() {
    }

    /**
     * Фабричный метод для создания новой записи при первичном обнаружении URL.
     */
    public static UrlEntity discovered(String url, String urlHash) {
        UrlEntity entity = new UrlEntity();
        entity.url = url;
        entity.urlHash = urlHash;
        entity.status = UrlStatus.DISCOVERED;
        entity.discoveredAt = Instant.now();
        return entity;
    }

    // Геттеры и Сеттеры

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlHash() {
        return urlHash;
    }

    public UrlStatus getStatus() {
        return status;
    }

    public void setStatus(UrlStatus status) {
        this.status = status;
    }

    public Instant getDiscoveredAt() {
        return discoveredAt;
    }

    public Instant getCrawledAt() {
        return crawledAt;
    }

    public void setCrawledAt(Instant crawledAt) {
        this.crawledAt = crawledAt;
    }
}
