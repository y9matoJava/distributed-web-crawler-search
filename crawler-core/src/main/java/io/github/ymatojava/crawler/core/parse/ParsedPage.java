package io.github.ymatojava.crawler.core.parse;

import java.util.Set;

/**
 * Неизменяемый объект данных (DTO), представляющий результат парсинга HTML-страницы.
 *
 * Этот класс служит мостом между модулем загрузки/парсинга и модулем обхода (Crawling Engine).
 * Содержит только очищенные и подготовленные данные, извлеченные из сырого HTML.
 *
 * @param url      Оригинальный URL страницы
 * @param title    Содержимое тега &lt;title&gt; (если отсутствует, возвращает пустую строку)
 * @param bodyText Извлеченный видимый текст страницы (без скриптов, стилей и HTML-тегов)
 * @param outLinks Набор уникальных, нормализованных исходящих ссылок на другие страницы
 */
public record ParsedPage(
        String url,
        String title,
        String bodyText,
        Set<String> outLinks
) {
    /**
     * Компактный конструктор с защитным копированием.
     */
    public ParsedPage {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL не может быть null или пустым");
        }
        title = title != null ? title.trim() : "";
        bodyText = bodyText != null ? bodyText.trim() : "";
        // Оборачиваем коллекцию ссылок в неизменяемый Set для thread-safety
        outLinks = outLinks != null ? Set.copyOf(outLinks) : Set.of();
    }
}
