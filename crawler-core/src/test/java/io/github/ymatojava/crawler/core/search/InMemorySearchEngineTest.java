package io.github.ymatojava.crawler.core.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для компонента {@link InMemorySearchEngine}.
 * Покрывают сценарии: индексация и поиск, обработка пустых результатов,
 * ограничение количества результатов, сортировка по релевантности, формирование сниппетов.
 */
class InMemorySearchEngineTest {

    private final InMemorySearchEngine engine = new InMemorySearchEngine();

    /**
     * Проверяет базовый сценарий: после индексации страницы
     * поиск по ключевому слову из её текста должен вернуть результат.
     */
    @Test
    void shouldIndexAndSearchPage() {
        engine.indexPage("https://example.com", "Java Tutorial",
                "java programming language tutorial java development java framework");

        List<SearchResult> results = engine.search("java", 10);

        assertFalse(results.isEmpty(), "Должен быть хотя бы один результат");
        assertEquals("https://example.com", results.get(0).url());
        assertEquals("Java Tutorial", results.get(0).title());
    }

    /**
     * Проверяет, что поиск по слову, отсутствующему в индексе,
     * возвращает пустой список без исключений.
     */
    @Test
    void shouldReturnEmptyForNoMatch() {
        engine.indexPage("https://example.com", "Java Tutorial",
                "java programming language");

        List<SearchResult> results = engine.search("python", 10);

        assertTrue(results.isEmpty(), "Поиск по несуществующему слову должен вернуть пустой список");
    }

    /**
     * Проверяет, что количество результатов не превышает заданный лимит.
     */
    @Test
    void shouldLimitResults() {
        // Индексируем 5 страниц, все содержат слово «programming»
        for (int i = 0; i < 5; i++) {
            engine.indexPage("https://example.com/" + i, "Page " + i,
                    "programming language development programming framework programming");
        }

        List<SearchResult> results = engine.search("programming", 3);

        assertTrue(results.size() <= 3,
                "Количество результатов не должно превышать лимит (3)");
    }

    /**
     * Проверяет, что результаты отсортированы по убыванию релевантности.
     * Страница, содержащая больше терминов запроса, должна быть выше.
     */
    @Test
    void shouldSortByRelevance() {
        // Страница 1: содержит «java» и «programming»
        engine.indexPage("https://example.com/both", "Both Keywords",
                "java programming java programming java programming development");
        // Страница 2: содержит только «java»
        engine.indexPage("https://example.com/java-only", "Java Only",
                "java java java development development development language");

        // Поиск по обоим терминам: страница 1 содержит оба, страница 2 — нет
        List<SearchResult> results = engine.search("java programming", 10);

        // Только страница 1 содержит оба термина (AND-логика)
        assertFalse(results.isEmpty(), "Должен быть хотя бы один результат");
        assertEquals("https://example.com/both", results.get(0).url(),
                "Страница с обоими ключевыми словами должна быть первой");
    }

    /**
     * Проверяет, что сниппет формируется как первые 200 символов текста страницы.
     */
    @Test
    void shouldCreateSnippet() {
        // Создаём текст длиной более 200 символов
        String longText = "java ".repeat(100); // 500 символов
        engine.indexPage("https://example.com", "Test", longText);

        List<SearchResult> results = engine.search("java", 10);

        assertFalse(results.isEmpty());
        String snippet = results.get(0).snippet();
        assertTrue(snippet.length() <= 200,
                "Сниппет не должен превышать 200 символов, фактическая длина: " + snippet.length());
    }

    /**
     * Проверяет, что поиск по пустому запросу возвращает пустой список.
     */
    @Test
    void shouldReturnEmptyForEmptyQuery() {
        engine.indexPage("https://example.com", "Test", "java programming");

        assertTrue(engine.search("", 10).isEmpty(), "Пустой запрос должен вернуть пустой список");
        assertTrue(engine.search(null, 10).isEmpty(), "null-запрос должен вернуть пустой список");
    }
}
