package io.github.ymatojava.crawler.core.keyword;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Компонент для извлечения ключевых слов из текста на основе частотного анализа (Term Frequency).
 * <p>
 * Алгоритм работы:
 * <ol>
 *     <li>Текст разбивается на токены с помощью {@link TextTokenizer}.</li>
 *     <li>Отфильтровываются стоп-слова (служебные слова без самостоятельного значения).</li>
 *     <li>Отфильтровываются слова короче 3 символов (предлоги, артикли, сокращения).</li>
 *     <li>Подсчитывается частота каждого оставшегося слова.</li>
 *     <li>Слова сортируются по убыванию частоты.</li>
 *     <li>Возвращаются topN наиболее частотных слов.</li>
 * </ol>
 * Этот подход является упрощённой версией TF-анализа, достаточной для базового
 * полнотекстового поиска в рамках распределённого краулера.
 */
public class KeywordExtractor {

    /**
     * Значение по умолчанию для максимального количества извлекаемых ключевых слов.
     * Выбрано эмпирически: 20 слов обычно достаточно для адекватного
     * представления тематики веб-страницы в поисковом индексе.
     */
    private static final int DEFAULT_MAX_KEYWORDS = 20;

    /**
     * Минимальная длина слова, которое может быть ключевым.
     * Слова из 1–2 символов (например, «do», «go», «if») обычно не несут
     * достаточной смысловой нагрузки для поискового индекса.
     */
    private static final int MIN_WORD_LENGTH = 3;

    /** Максимальное количество ключевых слов, возвращаемых методом {@link #extract(String)}. */
    private final int maxKeywords;

    /** Токенизатор для разбиения текста на слова. */
    private final TextTokenizer tokenizer;

    /**
     * Конструктор с указанием максимального количества ключевых слов.
     *
     * @param maxKeywords Максимальное число ключевых слов для извлечения. Должно быть положительным.
     */
    public KeywordExtractor(int maxKeywords) {
        if (maxKeywords <= 0) {
            throw new IllegalArgumentException("Максимальное количество ключевых слов должно быть положительным: " + maxKeywords);
        }
        this.maxKeywords = maxKeywords;
        // Зависимости создаются внутри класса, так как они не имеют состояния
        // и не требуют внешней конфигурации
        this.tokenizer = new TextTokenizer();
    }

    /**
     * Конструктор с параметрами по умолчанию.
     * Устанавливает лимит в {@value #DEFAULT_MAX_KEYWORDS} ключевых слов.
     */
    public KeywordExtractor() {
        this(DEFAULT_MAX_KEYWORDS);
    }

    /**
     * Извлекает ключевые слова из переданного текста.
     * <p>
     * Алгоритм:
     * <ol>
     *     <li>Токенизация текста → список слов в нижнем регистре.</li>
     *     <li>Фильтрация стоп-слов через {@link StopWords#isStopWord(String)}.</li>
     *     <li>Фильтрация коротких слов (менее {@value #MIN_WORD_LENGTH} символов).</li>
     *     <li>Подсчёт частоты каждого слова с помощью {@link Collectors#groupingBy} и {@link Collectors#counting}.</li>
     *     <li>Сортировка по частоте в порядке убывания.</li>
     *     <li>Отсечение по лимиту {@code maxKeywords}.</li>
     * </ol>
     *
     * @param text Исходный текст для анализа. Может быть null или пустым.
     * @return Неизменяемый список ключевых слов, отсортированных по убыванию частоты.
     *         Пустой список, если текст пуст или не содержит значимых слов.
     */
    public List<String> extract(String text) {
        // Быстрый выход для пустого ввода
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        // 1. Токенизация: разбиение текста на отдельные слова
        List<String> tokens = tokenizer.tokenize(text);

        // 2–3. Фильтрация стоп-слов и коротких слов одним проходом по потоку данных
        // 4. Подсчёт частоты каждого оставшегося слова (Map<слово, количество>)
        Map<String, Long> frequencyMap = tokens.stream()
                .filter(token -> !StopWords.isStopWord(token))
                .filter(token -> token.length() >= MIN_WORD_LENGTH)
                .collect(Collectors.groupingBy(
                        token -> token,   // Группируем одинаковые слова
                        Collectors.counting() // Считаем количество вхождений
                ));

        // 5–6. Сортировка по частоте (убывание) и отсечение по лимиту.
        // При равной частоте порядок не гарантирован, что допустимо для поискового индекса.
        return frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(maxKeywords)
                .map(Map.Entry::getKey)
                .toList();
    }
}
