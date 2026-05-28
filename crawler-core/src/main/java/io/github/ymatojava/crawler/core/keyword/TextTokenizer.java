package io.github.ymatojava.crawler.core.keyword;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилитный класс для разбиения сплошного текста на отдельные слова (токены).
 *
 * Токенизация — первый шаг подготовки текста к индексации и поиску.
 */
public class TextTokenizer {

    /**
     * Разбивает текст на слова, приводит к нижнему регистру и отбрасывает знаки препинания.
     *
     * Алгоритм:
     * 1. Регулярное выражение "[^a-zA-Zа-яА-ЯёЁ]+" находит любые символы,
     *    НЕ являющиеся буквами английского или русского алфавита (пробелы, цифры, знаки).
     * 2. Текст разбивается по этим символам.
     * 3. Все токены приводятся к нижнему регистру.
     * 4. Пустые строки отбрасываются.
     *
     * @param text Исходный текст (например, "Hello, world! 123")
     * @return Список слов в нижнем регистре (например, ["hello", "world"])
     */
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return Arrays.stream(text.split("[^a-zA-Zа-яА-ЯёЁ]+"))
                .filter(token -> !token.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableList());
    }
}
