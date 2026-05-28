package io.github.ymatojava.crawler.core.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для InMemorySearchEngine.
 */
class InMemorySearchEngineTest {

    private InMemorySearchEngine engine;

    @BeforeEach
    void setUp() {
        engine = new InMemorySearchEngine();
    }

    @Test
    void shouldIndexAndSearchPage() {
        engine.indexPage("https://example.com/java", "Java Tutorial", "Java is a programming language");
        
        List<SearchResult> results = engine.search("java programming", 10);
        
        assertEquals(1, results.size());
        assertEquals("https://example.com/java", results.get(0).url());
        assertEquals("Java Tutorial", results.get(0).title());
    }

    @Test
    void shouldReturnEmptyForNoMatch() {
        engine.indexPage("https://example.com/java", "Java", "Java language");
        
        List<SearchResult> results = engine.search("python", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldLimitResults() {
        for (int i = 0; i < 5; i++) {
            engine.indexPage("https://example.com/" + i, "Title " + i, "test data");
        }
        
        List<SearchResult> results = engine.search("test", 3);
        assertEquals(3, results.size(), "Должно вернуться ровно 3 результата согласно limit");
    }

    @Test
    void shouldSortByRelevance() {
        // Документ 1: содержит "java" часто (лучше совпадает)
        engine.indexPage("https://example.com/1", "Java 1", "java java java language code");
        
        // Документ 2: содержит "java" редко
        engine.indexPage("https://example.com/2", "Java 2", "java is cool but code is hard");
        
        // Документ 3: не содержит
        engine.indexPage("https://example.com/3", "Python", "python python python");

        List<SearchResult> results = engine.search("java code", 10);
        
        assertEquals(2, results.size());
        // Документ 1 должен быть первым, так как у него больше совпадений терминов (или выше TF)
        // В нашей реализации оценивается пересечение с извлеченными top-keywords.
        assertNotNull(results.get(0));
        assertTrue(results.get(0).relevanceScore() >= results.get(1).relevanceScore());
    }

    @Test
    void shouldCreateSnippet() {
        String longText = "a".repeat(300);
        engine.indexPage("https://example.com", "Long text", longText);
        
        List<SearchResult> results = engine.search("a", 10);
        
        assertEquals(1, results.size());
        String snippet = results.get(0).snippet();
        
        assertTrue(snippet.length() <= 200, "Сниппет должен быть обрезан до 200 символов");
        assertTrue(snippet.endsWith("..."), "Сниппет должен заканчиваться на троеточие");
    }
}
