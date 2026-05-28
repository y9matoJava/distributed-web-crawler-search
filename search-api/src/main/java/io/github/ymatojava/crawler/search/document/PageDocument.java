package io.github.ymatojava.crawler.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Сущность (Документ) Elasticsearch, представляющая проиндексированную страницу.
 * 
 * В отличие от JPA PageEntity, эта сущность оптимизирована для полнотекстового поиска
 * и содержит настройки анализаторов.
 */
@Document(indexName = "crawler_pages")
public class PageDocument {

    @Id
    private String id; // Может быть строковым представлением docId из базы данных

    /**
     * URL страницы (не индексируем для полнотекстового поиска, только как Keyword для точного совпадения).
     */
    @Field(type = FieldType.Keyword)
    private String url;

    /**
     * Заголовок страницы. Индексируется как текст с поддержкой русского и английского.
     * Вес заголовка обычно выше при ранжировании.
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    /**
     * Полный текст страницы.
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String bodyText;

    /**
     * Ключевые слова, извлеченные на этапе парсинга (опционально, так как ES
     * сам отлично справляется с токенизацией, но можно использовать для фасетной фильтрации).
     */
    @Field(type = FieldType.Keyword)
    private List<String> keywords;

    public PageDocument() {
    }

    public PageDocument(String id, String url, String title, String bodyText, List<String> keywords) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.bodyText = bodyText;
        this.keywords = keywords;
    }

    public String getId() {
        return id;
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
}
