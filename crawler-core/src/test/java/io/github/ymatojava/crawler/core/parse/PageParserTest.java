package io.github.ymatojava.crawler.core.parse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для компонента {@link PageParser}.
 *
 * <p>Покрывают основные сценарии парсинга HTML-страниц:
 * извлечение заголовка, видимого текста, ссылок,
 * а также крайние случаи (отсутствие title, отсутствие body).</p>
 */
class PageParserTest {

    private final PageParser parser = new PageParser();

    /**
     * Проверяет корректное извлечение содержимого тега {@code <title>}.
     * Title — ключевой элемент для поисковой выдачи.
     */
    @Test
    void shouldExtractPageTitle() {
        String html = """
                <html>
                <head><title>Тестовая страница</title></head>
                <body><p>Контент</p></body>
                </html>
                """;

        ParsedPage page = parser.parse("https://example.com/", html);

        assertEquals("Тестовая страница", page.title(),
                "Заголовок должен быть извлечён из тега <title>");
    }

    /**
     * Проверяет извлечение видимого текста из {@code <body>}.
     * Текст из скриптов и стилей НЕ должен попадать в результат,
     * так как он не виден пользователю и не релевантен для поиска.
     */
    @Test
    void shouldExtractVisibleText() {
        String html = """
                <html>
                <head><title>Test</title></head>
                <body>
                    <h1>Заголовок</h1>
                    <p>Параграф текста</p>
                    <script>var x = 1;</script>
                    <style>.hidden { display: none; }</style>
                    <div>Ещё текст</div>
                </body>
                </html>
                """;

        ParsedPage page = parser.parse("https://example.com/", html);

        // Jsoup метод body().text() автоматически исключает скрипты и стили
        assertTrue(page.bodyText().contains("Заголовок"),
                "Текст из <h1> должен быть в bodyText");
        assertTrue(page.bodyText().contains("Параграф текста"),
                "Текст из <p> должен быть в bodyText");
        assertTrue(page.bodyText().contains("Ещё текст"),
                "Текст из <div> должен быть в bodyText");
        assertFalse(page.bodyText().contains("var x"),
                "Содержимое <script> не должно попасть в bodyText");
        assertFalse(page.bodyText().contains(".hidden"),
                "Содержимое <style> не должно попасть в bodyText");
    }

    /**
     * Проверяет, что ссылки корректно извлекаются из HTML-страницы
     * и передаются в поле outLinks объекта ParsedPage.
     */
    @Test
    void shouldExtractLinksFromPage() {
        String html = """
                <html>
                <head><title>Links Page</title></head>
                <body>
                    <a href="https://example.com/page1">Page 1</a>
                    <a href="/page2">Page 2</a>
                </body>
                </html>
                """;

        ParsedPage page = parser.parse("https://example.com/", html);

        assertFalse(page.outLinks().isEmpty(),
                "Ссылки должны быть извлечены со страницы");
        assertTrue(page.outLinks().contains("https://example.com/page1"),
                "Абсолютная ссылка должна быть в outLinks");
        assertTrue(page.outLinks().contains("https://example.com/page2"),
                "Относительная ссылка должна быть разрешена и добавлена в outLinks");
    }

    /**
     * Проверяет, что страница без тега {@code <title>} не вызывает ошибку.
     * В этом случае title должен быть пустой строкой.
     */
    @Test
    void shouldHandlePageWithoutTitle() {
        String html = """
                <html>
                <head></head>
                <body><p>Контент без заголовка</p></body>
                </html>
                """;

        ParsedPage page = parser.parse("https://example.com/", html);

        assertEquals("", page.title(),
                "Если тег <title> отсутствует, title должен быть пустой строкой");
        assertTrue(page.bodyText().contains("Контент без заголовка"),
                "Текст из body должен быть извлечён даже без title");
    }

    /**
     * Проверяет, что парсер корректно обрабатывает пустой или null HTML.
     * Это крайний случай: сервер мог вернуть пустое тело ответа.
     * Парсер не должен падать — вместо этого возвращает «пустую» страницу.
     */
    @Test
    void shouldHandlePageWithoutBody() {
        // Случай 1: null HTML
        ParsedPage pageNull = parser.parse("https://example.com/", null);
        assertEquals("", pageNull.title());
        assertEquals("", pageNull.bodyText());
        assertTrue(pageNull.outLinks().isEmpty());

        // Случай 2: пустой HTML
        ParsedPage pageEmpty = parser.parse("https://example.com/", "");
        assertEquals("", pageEmpty.title());
        assertEquals("", pageEmpty.bodyText());
        assertTrue(pageEmpty.outLinks().isEmpty());

        // Случай 3: HTML только с head, без body
        String htmlNoBody = "<html><head><title>No Body</title></head></html>";
        ParsedPage pageNoBody = parser.parse("https://example.com/", htmlNoBody);
        assertEquals("No Body", pageNoBody.title());
        // Jsoup может создать пустой body автоматически, поэтому bodyText будет пустой строкой
        assertNotNull(pageNoBody.bodyText(),
                "bodyText не должен быть null даже при отсутствии <body>");
    }
}
