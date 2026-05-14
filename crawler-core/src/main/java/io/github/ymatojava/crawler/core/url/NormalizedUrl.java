package io.github.ymatojava.crawler.core.url;


/**
 * Представляет собой нормализованный и валидированный URL-адрес.
 * Использование Record обеспечивает неизменяемость объекта,
 * что критически важно в многопоточной среде распределенно краулера
 *
 * @param value Строковое представление нормализованного URL
 */
public record NormalizedUrl(String value) {
    public NormalizedUrl {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Normalized URL содержит null или пустой");
        }
    }
}
