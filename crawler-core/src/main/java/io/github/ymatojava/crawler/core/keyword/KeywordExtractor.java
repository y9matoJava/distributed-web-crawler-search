package io.github.ymatojava.crawler.core.keyword;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Извлекатель ключевых слов из текста на основе алгоритма Term Frequency (TF).
 *
 * Определяет тематику страницы путем подсчета наиболее часто встречающихся слов.
 */
public class KeywordExtractor {

    private final TextTokenizer tokenizer;
    private final StopWords stopWords;
    private final int maxKeywords;

    /**
     * Создает извлекатель с дефолтными настройками.
     */
    public KeywordExtractor() {
        this(20); // По умолчанию сохраняем топ-20 слов
    }

    /**
     * Создает извлекатель с кастомным лимитом.
     *
     * @param maxKeywords Максимальное количество ключевых слов
     */
    public KeywordExtractor(int maxKeywords) {
        this.tokenizer = new TextTokenizer();
        this.stopWords = new StopWords();
        this.maxKeywords = maxKeywords;
    }

    /**
     * Извлекает список ключевых слов из сырого текста, отсортированных по частоте (убывание).
     *
     * Алгоритм (Stream API):
     * 1. Токенизация (разбивка, lower-case).
     * 2. Фильтрация стоп-слов.
     * 3. Фильтрация коротких слов (< 3 символов).
     * 4. Группировка и подсчет частоты (Map<String, Long>).
     * 5. Сортировка по значению частоты (по убыванию).
     * 6. Ограничение количества (limit).
     * 7. Извлечение только ключей (слов).
     *
     * @param text Исходный текст страницы
     * @return Отсортированный список ключевых слов
     */
    public List<String> extract(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> tokens = tokenizer.tokenize(text);

        // Подсчитываем частоту каждого слова (Term Frequency)
        Map<String, Long> frequencyMap = tokens.stream()
                .filter(word -> word.length() >= 3)
                .filter(word -> !stopWords.isStopWord(word))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Сортируем по убыванию частоты и берем топ N
        return frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(maxKeywords)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
