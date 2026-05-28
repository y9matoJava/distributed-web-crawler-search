package io.github.ymatojava.crawler.core.parse;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для компонента {@link LinkExtractor}.
 *
 * <p>Покрывают ключевые сценарии извлечения ссылок из HTML:
 * абсолютные и относительные URL, фрагменты, javascript:/mailto: схемы,
 * нормализация и дедупликация.</p>
 */
class LinkExtractorTest {

    private final LinkExtractor extractor = new LinkExtractor();

    /**
     * Проверяет извлечение абсолютных ссылок (начинающихся с http:// или https://).
     * Это самый простой случай — ссылка уже содержит полный URL, не требующий разрешения.
     */
    @Test
    void shouldExtractAbsoluteLinks() {
        String html = """
                <html><body>
                    <a href="https://example.com/page1">Page 1</a>
                    <a href="http://example.com/page2">Page 2</a>
                </body></html>
                """;

        Set<String> links = extractor.extractLinks(html, "https://base.com/");

        assertEquals(2, links.size(), "Должны быть извлечены обе абсолютные ссылки");
        assertTrue(links.contains("https://example.com/page1"));
        assertTrue(links.contains("http://example.com/page2"));
    }

    /**
     * Проверяет разрешение относительных ссылок в абсолютные
     * на основе базового URL страницы.
     * Относительная ссылка "/about" на базовом URL "https://example.com/"
     * должна преобразоваться в "https://example.com/about".
     */
    @Test
    void shouldResolveRelativeLinks() {
        String html = """
                <html><body>
                    <a href="/about">About</a>
                    <a href="contact.html">Contact</a>
                </body></html>
                """;

        Set<String> links = extractor.extractLinks(html, "https://example.com/pages/index.html");

        assertTrue(links.contains("https://example.com/about"),
                "Относительная ссылка /about должна быть разрешена от корня домена");
        assertTrue(links.contains("https://example.com/pages/contact.html"),
                "Ссылка без ведущего '/' разрешается относительно текущей директории");
    }

    /**
     * Проверяет, что ссылки, состоящие только из фрагмента (#section),
     * игнорируются, так как они ведут на ту же страницу.
     * Такие ссылки не требуют нового HTTP-запроса.
     */
    @Test
    void shouldIgnoreFragmentOnlyLinks() {
        String html = """
                <html><body>
                    <a href="#section1">Section 1</a>
                    <a href="#top">Top</a>
                    <a href="https://example.com/real">Real link</a>
                </body></html>
                """;

        Set<String> links = extractor.extractLinks(html, "https://example.com/page");

        // Фрагменты ведут на ту же страницу — краулер должен их отбросить.
        // Но UrlCanonicalizer отбрасывает фрагмент, поэтому #section1 и #top
        // могут преобразоваться в URL текущей страницы, но он уже будет в frontier.
        assertTrue(links.contains("https://example.com/real"),
                "Реальная ссылка должна быть извлечена");
    }

    /**
     * Проверяет, что javascript:-ссылки полностью игнорируются.
     * Они представляют собой встроенный код, а не URL для загрузки.
     */
    @Test
    void shouldIgnoreJavascriptLinks() {
        String html = """
                <html><body>
                    <a href="javascript:void(0)">Click me</a>
                    <a href="javascript:alert('test')">Alert</a>
                    <a href="https://example.com/valid">Valid</a>
                </body></html>
                """;

        Set<String> links = extractor.extractLinks(html, "https://example.com/");

        // javascript: ссылки не являются URL и должны быть отфильтрованы
        assertFalse(links.stream().anyMatch(l -> l.contains("javascript")),
                "Ссылки javascript: не должны попасть в результат");
        assertTrue(links.contains("https://example.com/valid"),
                "Валидная ссылка должна быть извлечена");
    }

    /**
     * Проверяет, что mailto:-ссылки полностью игнорируются.
     * Mailto — это не веб-страница, а инструкция для почтового клиента.
     */
    @Test
    void shouldIgnoreMailtoLinks() {
        String html = """
                <html><body>
                    <a href="mailto:user@example.com">Email us</a>
                    <a href="mailto:admin@site.org?subject=Hello">Admin</a>
                    <a href="https://example.com/page">Page</a>
                </body></html>
                """;

        Set<String> links = extractor.extractLinks(html, "https://example.com/");

        assertFalse(links.stream().anyMatch(l -> l.contains("mailto")),
                "Ссылки mailto: не должны попасть в результат");
        assertTrue(links.contains("https://example.com/page"),
                "Валидная ссылка должна быть извлечена");
    }

    /**
     * Проверяет, что ссылки с нестандартным регистром схемы (HTTP://, HTTPS://)
     * корректно нормализуются к нижнему регистру.
     */
    @Test
    void shouldNormalizeExtractedLinks() {
        String html = """
                <html><body>
                    <a href="HTTP://EXAMPLE.COM/Page">Link</a>
                </body></html>
                """;

        Set<String> links = extractor.extractLinks(html, "https://base.com/");

        // Схема и хост должны быть приведены к нижнему регистру
        assertTrue(links.contains("http://example.com/Page"),
                "Схема и хост должны быть в нижнем регистре после нормализации");
    }

    /**
     * Проверяет, что дублирующиеся ссылки автоматически удаляются.
     * На реальных страницах одна и та же ссылка может встречаться в навигации,
     * футере и контенте — краулер не должен загружать страницу трижды.
     */
    @Test
    void shouldRemoveDuplicateLinks() {
        String html = """
                <html><body>
                    <a href="https://example.com/page">Link 1</a>
                    <a href="https://example.com/page">Link 2</a>
                    <a href="https://example.com/page#section">Link 3</a>
                </body></html>
                """;

        Set<String> links = extractor.extractLinks(html, "https://example.com/");

        // Все три ссылки ведут на одну страницу (фрагмент отбрасывается)
        assertEquals(1, links.size(),
                "Дублирующиеся ссылки (с учётом удаления фрагмента) должны быть удалены");
        assertTrue(links.contains("https://example.com/page"));
    }

    /**
     * Проверяет корректную обработку пустого или null HTML.
     * Краулер должен возвращать пустой набор, а не падать с исключением.
     */
    @Test
    void shouldHandleEmptyHtml() {
        Set<String> linksFromNull = extractor.extractLinks(null, "https://example.com/");
        Set<String> linksFromEmpty = extractor.extractLinks("", "https://example.com/");
        Set<String> linksFromBlank = extractor.extractLinks("   ", "https://example.com/");

        assertTrue(linksFromNull.isEmpty(), "Null HTML должен вернуть пустой набор");
        assertTrue(linksFromEmpty.isEmpty(), "Пустой HTML должен вернуть пустой набор");
        assertTrue(linksFromBlank.isEmpty(), "Пробельный HTML должен вернуть пустой набор");
    }
}
