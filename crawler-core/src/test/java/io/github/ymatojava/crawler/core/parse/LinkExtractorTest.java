package io.github.ymatojava.crawler.core.parse;

import io.github.ymatojava.crawler.core.url.UrlCanonicalizer;
import io.github.ymatojava.crawler.core.url.UrlFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для LinkExtractor.
 */
class LinkExtractorTest {

    private LinkExtractor extractor;

    @BeforeEach
    void setUp() {
        // Используем реальные зависимости, так как они детерминированы и быстры
        extractor = new LinkExtractor(new UrlCanonicalizer(), new UrlFilter());
    }

    @Test
    void shouldExtractAbsoluteLinks() {
        String html = "<html><body><a href=\"https://example.com/page1\">Link</a></body></html>";
        Set<String> links = extractor.extractLinks(html, "https://example.com");

        assertEquals(1, links.size());
        assertTrue(links.contains("https://example.com/page1"));
    }

    @Test
    void shouldResolveRelativeLinks() {
        String html = "<html><body><a href=\"/about\">About</a></body></html>";
        Set<String> links = extractor.extractLinks(html, "https://example.com/home");

        assertEquals(1, links.size());
        assertTrue(links.contains("https://example.com/about"));
    }

    @Test
    void shouldIgnoreFragmentOnlyLinks() {
        String html = "<html><body><a href=\"#top\">Top</a></body></html>";
        Set<String> links = extractor.extractLinks(html, "https://example.com/page");

        // Якоря фильтруются в UrlFilter
        assertTrue(links.isEmpty(), "Ссылки-якоря должны игнорироваться");
    }

    @Test
    void shouldIgnoreJavascriptLinks() {
        String html = "<html><body><a href=\"javascript:void(0)\">Click</a></body></html>";
        Set<String> links = extractor.extractLinks(html, "https://example.com");

        assertTrue(links.isEmpty(), "JavaScript-ссылки должны игнорироваться");
    }

    @Test
    void shouldIgnoreMailtoLinks() {
        String html = "<html><body><a href=\"mailto:test@example.com\">Mail</a></body></html>";
        Set<String> links = extractor.extractLinks(html, "https://example.com");

        assertTrue(links.isEmpty(), "Mailto-ссылки должны игнорироваться");
    }

    @Test
    void shouldNormalizeExtractedLinks() {
        String html = "<html><body><a href=\"HTTP://EXAMPLE.COM/Page\">Link</a></body></html>";
        Set<String> links = extractor.extractLinks(html, "https://example.com");

        assertEquals(1, links.size());
        assertTrue(links.contains("http://example.com/Page")); // Хост в нижний регистр, путь остается
    }

    @Test
    void shouldRemoveDuplicateLinks() {
        String html = "<html><body>" +
                "<a href=\"https://site.com/a\">Link 1</a>" +
                "<a href=\"https://site.com/a\">Link 2</a>" +
                "<a href=\"/a\">Link 3</a>" + // тот же URL, но относительный
                "</body></html>";
        Set<String> links = extractor.extractLinks(html, "https://site.com");

        assertEquals(1, links.size(), "Дубликаты должны быть удалены");
        assertTrue(links.contains("https://site.com/a"));
    }

    @Test
    void shouldHandleEmptyHtml() {
        Set<String> links = extractor.extractLinks("", "https://example.com");
        assertTrue(links.isEmpty(), "Пустой HTML должен возвращать пустой Set");
        
        Set<String> linksNull = extractor.extractLinks(null, "https://example.com");
        assertTrue(linksNull.isEmpty(), "Null HTML должен возвращать пустой Set");
    }
}
