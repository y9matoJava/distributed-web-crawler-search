package io.github.ymatojava.crawler.core.keyword;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для компонента {@link TextTokenizer}.
 * Покрывают базовые сценарии: разбиение текста, приведение к нижнему регистру,
 * обработка специальных символов и пустого ввода.
 */
class TextTokenizerTest {

    private final TextTokenizer tokenizer = new TextTokenizer();

    /**
     * Проверяет, что простой текст из нескольких слов корректно разбивается на токены.
     */
    @Test
    void shouldTokenizeSimpleText() {
        List<String> tokens = tokenizer.tokenize("Hello World");

        assertEquals(2, tokens.size(), "Простой текст из двух слов должен дать два токена");
        assertEquals("hello", tokens.get(0));
        assertEquals("world", tokens.get(1));
    }

    /**
     * Проверяет, что все токены приводятся к нижнему регистру,
     * что необходимо для регистронезависимого сравнения при поиске.
     */
    @Test
    void shouldConvertToLowerCase() {
        List<String> tokens = tokenizer.tokenize("Java PROGRAMMING Language");

        assertTrue(tokens.stream().allMatch(t -> t.equals(t.toLowerCase())),
                "Все токены должны быть в нижнем регистре");
        assertTrue(tokens.contains("java"));
        assertTrue(tokens.contains("programming"));
        assertTrue(tokens.contains("language"));
    }

    /**
     * Проверяет, что null и пустая строка возвращают пустой список без исключений.
     */
    @Test
    void shouldHandleEmptyText() {
        assertTrue(tokenizer.tokenize(null).isEmpty(), "null должен давать пустой список");
        assertTrue(tokenizer.tokenize("").isEmpty(), "Пустая строка должна давать пустой список");
        assertTrue(tokenizer.tokenize("   ").isEmpty(), "Строка из пробелов должна давать пустой список");
    }

    /**
     * Проверяет корректное разбиение текста, содержащего спецсимволы:
     * цифры, знаки препинания, дефисы, символы URL и т.д.
     */
    @Test
    void shouldSplitOnSpecialCharacters() {
        List<String> tokens = tokenizer.tokenize("hello-world, foo.bar! test@email 123abc");

        assertTrue(tokens.contains("hello"), "Дефис должен быть разделителем");
        assertTrue(tokens.contains("world"), "Запятая и дефис должны быть разделителями");
        assertTrue(tokens.contains("foo"), "Точка должна быть разделителем");
        assertTrue(tokens.contains("bar"), "Восклицательный знак должен быть разделителем");
        assertTrue(tokens.contains("test"), "@ должен быть разделителем");
        assertTrue(tokens.contains("email"), "@ должен быть разделителем");
        assertTrue(tokens.contains("abc"), "Цифры должны быть разделителями");
    }

    /**
     * Проверяет, что токенизатор корректно обрабатывает кириллический текст.
     */
    @Test
    void shouldTokenizeCyrillicText() {
        List<String> tokens = tokenizer.tokenize("Привет Мир");

        assertEquals(2, tokens.size());
        assertEquals("привет", tokens.get(0));
        assertEquals("мир", tokens.get(1));
    }
}
