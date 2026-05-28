package io.github.ymatojava.crawler.core.keyword;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для компонента {@link KeywordExtractor}.
 * Покрывают сценарии: частотный анализ, фильтрация стоп-слов и коротких слов,
 * ограничение количества результатов, регистронезависимость, пустой ввод.
 */
class KeywordExtractorTest {

    private final KeywordExtractor extractor = new KeywordExtractor(5);

    /**
     * Проверяет, что слова, встречающиеся чаще других, стоят в начале списка.
     * Слово «java» повторяется 3 раза и должно быть первым.
     */
    @Test
    void shouldExtractMostFrequentWords() {
        String text = "java programming java language java framework spring framework";
        List<String> keywords = extractor.extract(text);

        assertFalse(keywords.isEmpty(), "Должен быть хотя бы один ключевой слово");
        assertEquals("java", keywords.get(0),
                "Наиболее частотное слово 'java' (3 раза) должно быть первым");
    }

    /**
     * Проверяет, что стоп-слова (the, is, and, for, etc.) отфильтровываются
     * и не попадают в список ключевых слов.
     */
    @Test
    void shouldFilterStopWords() {
        String text = "the java programming language is the best and the most popular for development";
        List<String> keywords = extractor.extract(text);

        assertFalse(keywords.contains("the"), "'the' — стоп-слово, не должно присутствовать");
        assertFalse(keywords.contains("is"), "'is' — стоп-слово, не должно присутствовать");
        assertFalse(keywords.contains("and"), "'and' — стоп-слово, не должно присутствовать");
        assertFalse(keywords.contains("for"), "'for' — стоп-слово, не должно присутствовать");
        assertTrue(keywords.contains("java"), "'java' — значимое слово, должно присутствовать");
    }

    /**
     * Проверяет, что слова короче 3 символов (например, «go», «to», «do»)
     * отфильтровываются, поскольку они не несут достаточной смысловой нагрузки.
     */
    @Test
    void shouldFilterShortWords() {
        String text = "go to do it my programming language development";
        List<String> keywords = extractor.extract(text);

        // Все слова длиной < 3 должны быть отфильтрованы
        assertTrue(keywords.stream().allMatch(kw -> kw.length() >= 3),
                "Все ключевые слова должны быть длиной не менее 3 символов");
    }

    /**
     * Проверяет, что для пустого текста возвращается пустой список без исключений.
     */
    @Test
    void shouldReturnEmptyForEmptyText() {
        assertTrue(extractor.extract(null).isEmpty(), "null должен давать пустой список");
        assertTrue(extractor.extract("").isEmpty(), "Пустая строка должна давать пустой список");
        assertTrue(extractor.extract("   ").isEmpty(), "Строка из пробелов должна давать пустой список");
    }

    /**
     * Проверяет, что количество возвращённых ключевых слов не превышает maxKeywords.
     */
    @Test
    void shouldLimitToMaxKeywords() {
        // Создаём экстрактор с лимитом 3
        KeywordExtractor limitedExtractor = new KeywordExtractor(3);
        String text = "alpha beta gamma delta epsilon zeta alpha beta gamma delta epsilon zeta";
        List<String> keywords = limitedExtractor.extract(text);

        assertTrue(keywords.size() <= 3,
                "Количество ключевых слов не должно превышать maxKeywords (3)");
    }

    /**
     * Проверяет, что «Java» и «java» считаются одним и тем же словом
     * при подсчёте частоты (регистронезависимость).
     */
    @Test
    void shouldBeCaseInsensitive() {
        String text = "Java java JAVA programming Programming";
        List<String> keywords = extractor.extract(text);

        // «java» в трёх вариантах регистра должно быть самым частотным словом
        assertEquals("java", keywords.get(0),
                "'Java'/'java'/'JAVA' должны объединяться в одно слово 'java'");
    }
}
