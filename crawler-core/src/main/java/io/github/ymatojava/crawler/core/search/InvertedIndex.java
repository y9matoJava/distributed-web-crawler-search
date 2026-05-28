package io.github.ymatojava.crawler.core.search;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Инвертированный индекс (Inverted Index).
 *
 * Ключевая структура данных любого поисковика.
 * Вместо того чтобы искать слово в каждом документе (O(N)),
 * индекс хранит маппинг: "Слово" -> "Список ID документов, где оно встречается".
 * Это позволяет находить документы за O(1).
 */
public class InvertedIndex {

    // ConcurrentHashMap используется для потокобезопасности, 
    // если индексация будет идти параллельно
    private final Map<String, Set<Long>> index = new ConcurrentHashMap<>();

    /**
     * Индексирует документ.
     *
     * @param docId    Внутренний идентификатор документа
     * @param keywords Список ключевых слов, описывающих документ
     */
    public void addDocument(long docId, Collection<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        for (String keyword : keywords) {
            String lowerKw = keyword.toLowerCase();
            // Вычисляем Set (или создаем новый ConcurrentHashMap.newKeySet()),
            // и добавляем в него docId
            index.computeIfAbsent(lowerKw, k -> ConcurrentHashMap.newKeySet()).add(docId);
        }
    }

    /**
     * Ищет документы, содержащие ОДНО конкретное ключевое слово.
     *
     * @param keyword Искомое слово
     * @return Набор ID документов (может быть пустым)
     */
    public Set<Long> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptySet();
        }
        return index.getOrDefault(keyword.toLowerCase(), Collections.emptySet());
    }

    /**
     * Ищет документы, содержащие ВСЕ переданные ключевые слова (AND-логика).
     *
     * @param keywords Список искомых слов
     * @return Набор ID документов, в которых встречаются ВСЕ слова из списка
     */
    public Set<Long> searchAll(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptySet();
        }

        // Начинаем с документов, содержащих первое слово
        Set<Long> result = new HashSet<>(search(keywords.get(0)));

        // Пересекаем (retainAll) с документами, содержащими остальные слова
        for (int i = 1; i < keywords.size(); i++) {
            if (result.isEmpty()) break; // Оптимизация: если пусто, дальше искать нет смысла
            result.retainAll(search(keywords.get(i)));
        }

        return result;
    }
}
