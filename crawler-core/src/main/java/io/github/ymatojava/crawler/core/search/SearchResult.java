package io.github.ymatojava.crawler.core.search;

import java.util.List;

/**
 * Неизменяемый объект данных (DTO), представляющий один результат поиска.
 *
 * Содержит информацию, необходимую для отображения пользователю (SERP - Search Engine Results Page).
 *
 * @param url            URL найденной страницы
 * @param title          Заголовок страницы (title)
 * @param snippet        Фрагмент текста страницы (до 200 символов)
 * @param keywords       Ключевые слова, по которым страница была проиндексирована
 * @param relevanceScore Оценка релевантности (чем выше, тем точнее совпадение)
 */
public record SearchResult(
        String url,
        String title,
        String snippet,
        List<String> keywords,
        double relevanceScore
) {
    public SearchResult {
        keywords = keywords != null ? List.copyOf(keywords) : List.of();
    }
}
