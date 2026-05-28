package io.github.ymatojava.crawler.core.search;

import java.util.List;

/**
 * Интерфейс поискового движка.
 *
 * Абстрагирует механизм поиска (In-Memory, Elasticsearch, PostgreSQL FTS и т.д.).
 */
public interface SearchEngine {

    /**
     * Выполняет полнотекстовый поиск по проиндексированным документам.
     *
     * @param query Поисковый запрос (введенный пользователем)
     * @param limit Максимальное количество результатов
     * @return Список результатов, отсортированных по релевантности (убывание)
     */
    List<SearchResult> search(String query, int limit);
}
