package io.github.ymatojava.crawler.core.parse;

import java.util.Set;

/**
 * Неизменяемая модель результата парсинга HTML-страницы.
 * Содержит все ключевые данные, извлечённые из документа, необходимые
 * для дальнейшей индексации и обхода (crawl) ссылок.
 *
 * <p>Использование Java Record гарантирует неизменяемость (immutability),
 * что критично при многопоточном конвейере обработки страниц.</p>
 *
 * @param url      URL страницы, с которой был получен HTML
 * @param title    Содержимое тега {@code <title>} (может быть пустой строкой)
 * @param bodyText Видимый текст из {@code <body>} без скриптов и стилей
 * @param outLinks Множество нормализованных исходящих ссылок, извлечённых со страницы
 */
public record ParsedPage(
        String url,
        String title,
        String bodyText,
        Set<String> outLinks
) {
    /**
     * Компактный конструктор с валидацией обязательных полей.
     * URL не может быть null или пустым, так как является идентификатором страницы.
     */
    public ParsedPage {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL страницы не может быть null или пустым");
        }
        // Защитное копирование множества ссылок для гарантии неизменяемости
        outLinks = outLinks != null ? Set.copyOf(outLinks) : Set.of();
        title = title != null ? title : "";
        bodyText = bodyText != null ? bodyText : "";
    }
}
