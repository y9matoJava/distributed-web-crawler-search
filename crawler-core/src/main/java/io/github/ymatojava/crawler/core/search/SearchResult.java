package io.github.ymatojava.crawler.core.search;

import java.util.List;

/**
 * Неизменяемая запись (record), представляющая один результат поискового запроса.
 * <p>
 * Использование Java Record обеспечивает:
 * <ul>
 *     <li>Неизменяемость (immutability) — безопасно передавать между потоками без синхронизации.</li>
 *     <li>Автоматическую генерацию equals/hashCode/toString — корректное сравнение результатов.</li>
 *     <li>Компактный синтаксис без boilerplate-кода.</li>
 * </ul>
 *
 * @param url            URL-адрес проиндексированной страницы
 * @param title          Заголовок страницы (тег &lt;title&gt;)
 * @param snippet        Краткий фрагмент содержимого страницы (первые 200 символов тела),
 *                       используемый для предварительного просмотра в результатах поиска
 * @param keywords       Список ключевых слов, извлечённых из текста страницы
 * @param relevanceScore Оценка релевантности (0.0–1.0), вычисленная как отношение
 *                       найденных терминов запроса к общему числу терминов запроса
 */
public record SearchResult(
        String url,
        String title,
        String snippet,
        List<String> keywords,
        double relevanceScore
) {
}
