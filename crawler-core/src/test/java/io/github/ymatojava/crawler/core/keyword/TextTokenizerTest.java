package io.github.ymatojava.crawler.core.keyword;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для TextTokenizer.
 */
class TextTokenizerTest {

    private final TextTokenizer tokenizer = new TextTokenizer();

    @Test
    void shouldTokenizeSimpleText() {
        List<String> tokens = tokenizer.tokenize("Hello world");
        assertEquals(List.of("hello", "world"), tokens);
    }

    @Test
    void shouldConvertToLowerCase() {
        List<String> tokens = tokenizer.tokenize("JAVA Spring BOOT");
        assertEquals(List.of("java", "spring", "boot"), tokens);
    }

    @Test
    void shouldHandleEmptyText() {
        assertTrue(tokenizer.tokenize("").isEmpty());
        assertTrue(tokenizer.tokenize(null).isEmpty());
        assertTrue(tokenizer.tokenize("   ").isEmpty());
    }

    @Test
    void shouldSplitOnSpecialCharacters() {
        List<String> tokens = tokenizer.tokenize("email@test.com! 123 apples, and (oranges).");
        // '123' отбрасывается, спецсимволы используются как разделители
        assertEquals(List.of("email", "test", "com", "apples", "and", "oranges"), tokens);
    }

    @Test
    void shouldHandleRussianText() {
        List<String> tokens = tokenizer.tokenize("Привет, мир! Как дела?");
        assertEquals(List.of("привет", "мир", "как", "дела"), tokens);
    }
}
