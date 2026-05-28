package io.github.ymatojava.crawler.core.parse;

import io.github.ymatojava.crawler.core.url.UrlCanonicalizer;
import io.github.ymatojava.crawler.core.url.UrlFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для PageParser.
 */
class PageParserTest {

    private PageParser parser;

    @BeforeEach
    void setUp() {
        LinkExtractor extractor = new LinkExtractor(new UrlCanonicalizer(), new UrlFilter());
        parser = new PageParser(extractor);
    }

    @Test
    void shouldExtractPageTitle() {
        String html = "<html><head><title>Test Page</title></head><body>Hello</body></html>";
        ParsedPage page = parser.parse("https://example.com", html);

        assertEquals("Test Page", page.title());
    }

    @Test
    void shouldExtractVisibleText() {
        String html = "<html><head><style>body { color: red; }</style></head>" +
                "<body><h1>Header</h1><p>Paragraph text.</p><script>alert(1);</script></body></html>";
        ParsedPage page = parser.parse("https://example.com", html);

        assertEquals("Header Paragraph text.", page.bodyText(), 
                "Должен извлекаться только видимый текст (без скриптов и стилей)");
    }

    @Test
    void shouldExtractLinksFromPage() {
        String html = "<html><body><a href=\"/link1\">L1</a> <a href=\"https://external.com\">L2</a></body></html>";
        ParsedPage page = parser.parse("https://example.com", html);

        assertEquals(2, page.outLinks().size());
        assertTrue(page.outLinks().contains("https://example.com/link1"));
        assertTrue(page.outLinks().contains("https://external.com/"));
    }

    @Test
    void shouldHandlePageWithoutTitle() {
        String html = "<html><body>Just content</body></html>";
        ParsedPage page = parser.parse("https://example.com", html);

        assertEquals("", page.title(), "Если title нет, возвращается пустая строка");
        assertEquals("Just content", page.bodyText());
    }

    @Test
    void shouldHandlePageWithoutBody() {
        String html = "<html><head><title>Title only</title></head></html>";
        ParsedPage page = parser.parse("https://example.com", html);

        assertEquals("Title only", page.title());
        assertEquals("", page.bodyText(), "Если body нет, возвращается пустая строка");
    }
}
