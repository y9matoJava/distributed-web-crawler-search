package io.github.ymatojava.crawler.core.keyword;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для KeywordExtractor.
 */
class KeywordExtractorTest {

    private KeywordExtractor extractor;

    @BeforeEach
    void setUp() {
        // Лимит 3 для удобства тестирования
        extractor = new KeywordExtractor(3);
    }

    @Test
    void shouldExtractMostFrequentWords() {
        String text = "java java java spring spring boot";
        List<String> keywords = extractor.extract(text);

        assertEquals(3, keywords.size());
        assertEquals("java", keywords.get(0)); // 3 раза
        assertEquals("spring", keywords.get(1)); // 2 раза
        assertEquals("boot", keywords.get(2)); // 1 раз
    }

    @Test
    void shouldFilterStopWords() {
        // "the", "and", "is" - стоп-слова
        String text = "the java and the spring is good";
        List<String> keywords = extractor.extract(text);

        assertFalse(keywords.contains("the"));
        assertFalse(keywords.contains("and"));
        assertTrue(keywords.contains("java"));
        assertTrue(keywords.contains("spring"));
        assertTrue(keywords.contains("good"));
    }

    @Test
    void shouldFilterShortWords() {
        // "go", "js", "c" - меньше 3 символов
        String text = "go js c java python";
        List<String> keywords = extractor.extract(text);

        assertTrue(keywords.contains("java"));
        assertTrue(keywords.contains("python"));
        assertFalse(keywords.contains("go"));
        assertFalse(keywords.contains("js"));
    }

    @Test
    void shouldReturnEmptyForEmptyText() {
        assertTrue(extractor.extract("").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
    }

    @Test
    void shouldLimitToMaxKeywords() {
        String text = "one one two two three three four four five five";
        List<String> keywords = extractor.extract(text);

        assertEquals(3, keywords.size(), "Должно быть ровно 3 (согласно лимиту в конструкторе)");
    }
}
