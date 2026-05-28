package io.github.ymatojava.crawler.common.model;

import java.time.Instant;
import java.util.List;

/**
 * Неизменяемый объект данных (DTO), представляющий проиндексированную веб-страницу.
 *
 * Содержит все извлечённые данные, которые сохраняются в PostgreSQL
 * и индексируются в Elasticsearch для полнотекстового поиска.
 *
 * Жизненный путь объекта:
 * <pre>
 *   HTML → PageParser → ParsedPage → KeywordExtractor → CrawlPage → PostgreSQL + Elasticsearch
 * </pre>
 *
 * @param id        Уникальный идентификатор в БД (null для ещё не сохранённых)
 * @param url       Каноникализированный URL страницы
 * @param title     Заголовок страницы (содержимое тега {@code <title>})
 * @param bodyText  Видимый текстовый контент страницы (без HTML-тегов, скриптов, стилей)
 * @param keywords  Список извлечённых ключевых слов, отсортированных по частоте
 * @param crawledAt Момент времени, когда страница была скачана и обработана
 */
public record CrawlPage(
        Long id,
        String url,
        String title,
        String bodyText,
        List<String> keywords,
        Instant crawledAt
) {

    /**
     * Компактный конструктор с валидацией и защитным копированием.
     * Список ключевых слов оборачивается в неизменяемый List для thread-safety.
     */
    public CrawlPage {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL страницы не может быть null или пустым");
        }
        // Защитное копирование: оборачиваем в unmodifiable, чтобы внешний код
        // не мог изменить внутреннее состояние record
        keywords = keywords != null ? List.copyOf(keywords) : List.of();
    }
}
