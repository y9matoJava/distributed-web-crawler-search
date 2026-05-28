package io.github.ymatojava.crawler.worker.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA сущность, описывающая скачанную и проиндексированную страницу.
 *
 * Маппится на таблицу "crawl_pages".
 * Содержит извлеченный текст, заголовок и связь один-к-одному с оригинальным URL.
 * Также управляет связью один-ко-многим с извлеченными ключевыми словами.
 */
@Entity
@Table(name = "crawl_pages")
public class PageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Связь с таблицей URL. Каждая страница обязана иметь исходный URL.
     * Используется FetchType.LAZY для избежания N+1 проблемы.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "url_id", unique = true, nullable = false)
    private UrlEntity urlEntity;

    @Column(name = "url", nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    /**
     * Коллекция ключевых слов (маппинг на таблицу page_keywords).
     * ElementCollection идеальна для простых типов вроде String, когда нет
     * необходимости создавать отдельную сущность для ключевого слова.
     */
    @ElementCollection
    @CollectionTable(
            name = "page_keywords",
            joinColumns = @JoinColumn(name = "page_id")
    )
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @Column(name = "crawled_at", nullable = false, updatable = false)
    private Instant crawledAt;

    protected PageEntity() {
    }

    public PageEntity(UrlEntity urlEntity, String url, String title, String bodyText, List<String> keywords) {
        this.urlEntity = urlEntity;
        this.url = url;
        this.title = title;
        this.bodyText = bodyText;
        if (keywords != null) {
            this.keywords.addAll(keywords);
        }
        this.crawledAt = Instant.now();
    }

    // Геттеры

    public Long getId() {
        return id;
    }

    public UrlEntity getUrlEntity() {
        return urlEntity;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getBodyText() {
        return bodyText;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Instant getCrawledAt() {
        return crawledAt;
    }
}
