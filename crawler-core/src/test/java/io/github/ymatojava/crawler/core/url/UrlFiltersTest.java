package io.github.ymatojava.crawler.core.url;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для компонента UrlFilter.
 * Проверяют корректность пропуска валидных веб-ссылок и отсечения невалидных форматов.
 */
class UrlFilterTest {

    private final UrlFilter filter = new UrlFilter();

    /**
     * Проверяет, что стандартные HTTP и HTTPS ссылки успешно проходят фильтрацию.
     */
    @Test
    void testAllowedUrls() {
        assertTrue(filter.isValid("http://example.com"), "HTTP URLs should be allowed");
        assertTrue(filter.isValid("https://example.com/path?q=1"), "HTTPS URLs with path and query should be allowed");
    }

    /**
     * Проверяет, что ссылки с неподдерживаемыми или опасными схемами отклоняются фильтром.
     */
    @Test
    void testRejectedSchemes() {
        assertFalse(filter.isValid("ftp://example.com/file"), "FTP scheme should be rejected");
        assertFalse(filter.isValid("mailto:user@example.com"), "Mailto scheme should be rejected");
        assertFalse(filter.isValid("javascript:alert(1)"), "Javascript scheme should be rejected");
        assertFalse(filter.isValid("file:///C:/path/file.txt"), "File scheme should be rejected");
    }

    /**
     * Проверяет реакцию фильтра на пустые строки и ссылки, состоящие исключительно из якоря (fragment).
     */
    @Test
    void testEmptyAndFragmentOnly() {
        assertFalse(filter.isValid(""), "Empty string should be rejected");
        assertFalse(filter.isValid("   "), "Blank string should be rejected");
        assertFalse(filter.isValid("#fragment"), "Fragment-only URLs should be rejected");
    }
}
