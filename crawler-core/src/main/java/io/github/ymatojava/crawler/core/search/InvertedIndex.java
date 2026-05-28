package io.github.ymatojava.crawler.core.search;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Инвертированный индекс — основная структура данных для полнотекстового поиска.
 * <p>
 * Принцип работы: для каждого ключевого слова хранится множество идентификаторов документов,
 * в которых это слово встречается. Это позволяет за O(1) по ключевому слову получить
 * все документы, содержащие данное слово.
 * <p>
 * Потокобезопасность обеспечивается за счёт использования {@link ConcurrentHashMap}
 * и {@link ConcurrentHashMap#newKeySet()}, что позволяет нескольким потокам краулера
 * одновременно индексировать документы без внешней синхронизации.
 */
public class InvertedIndex {

    /**
     * Основная структура индекса: отображение «ключевое слово → множество ID документов».
     * ConcurrentHashMap выбрана для обеспечения безблокировочного параллельного доступа:
     * операции чтения не блокируются, а операции записи блокируют только отдельные сегменты.
     */
    private final ConcurrentHashMap<String, Set<Long>> index = new ConcurrentHashMap<>();

    /**
     * Добавляет документ в индекс, связывая каждое ключевое слово с ID документа.
     * <p>
     * Для каждого ключевого слова из списка создаётся (или обновляется) запись в индексе.
     * Метод {@code computeIfAbsent} атомарно создаёт новый Set, если для данного слова
     * ещё не существует записи, что предотвращает состояние гонки (race condition).
     *
     * @param docId    Уникальный идентификатор документа
     * @param keywords Список ключевых слов документа (ожидается в нижнем регистре)
     */
    public void addDocument(long docId, List<String> keywords) {
        for (String keyword : keywords) {
            // Приведение к нижнему регистру для регистронезависимого поиска
            String normalizedKeyword = keyword.toLowerCase();
            // computeIfAbsent атомарно создаёт ConcurrentHashSet при первом обращении к ключу,
            // а затем добавляет ID документа в потокобезопасный Set
            index.computeIfAbsent(normalizedKeyword, k -> ConcurrentHashMap.newKeySet())
                    .add(docId);
        }
    }

    /**
     * Ищет документы, содержащие указанное ключевое слово.
     *
     * @param keyword Ключевое слово для поиска
     * @return Неизменяемое множество ID документов, содержащих данное слово.
     *         Пустое множество, если слово не найдено в индексе.
     */
    public Set<Long> search(String keyword) {
        if (keyword == null) {
            return Collections.emptySet();
        }
        // Приведение к нижнему регистру для согласованности с индексацией
        Set<Long> result = index.get(keyword.toLowerCase());
        // Возвращаем неизменяемую копию, чтобы вызывающий код не мог повредить внутренний индекс
        return result != null ? Collections.unmodifiableSet(result) : Collections.emptySet();
    }

    /**
     * Ищет документы, содержащие ВСЕ указанные ключевые слова (логика AND).
     * <p>
     * Алгоритм:
     * <ol>
     *     <li>Для каждого ключевого слова извлекается множество ID документов.</li>
     *     <li>Выполняется пересечение (intersection) всех множеств.</li>
     *     <li>Результат — документы, содержащие каждое слово из запроса.</li>
     * </ol>
     * Оптимизация: если хотя бы одно слово отсутствует в индексе, результат сразу пуст,
     * т.к. пересечение с пустым множеством всегда даёт пустое множество.
     *
     * @param keywords Список ключевых слов для конъюнктивного поиска
     * @return Множество ID документов, содержащих ВСЕ указанные слова.
     *         Пустое множество, если совпадений нет или список пуст.
     */
    public Set<Long> searchAll(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptySet();
        }

        // Начинаем с множества документов для первого ключевого слова
        Set<Long> result = null;

        for (String keyword : keywords) {
            Set<Long> docs = search(keyword);

            if (docs.isEmpty()) {
                // Оптимизация раннего выхода: если хотя бы для одного слова
                // нет документов, пересечение заведомо пустое
                return Collections.emptySet();
            }

            if (result == null) {
                // Первая итерация: создаём изменяемую копию для дальнейших пересечений
                result = ConcurrentHashMap.newKeySet();
                result.addAll(docs);
            } else {
                // Последующие итерации: пересечение (retainAll удаляет элементы,
                // отсутствующие в переданной коллекции)
                result.retainAll(docs);

                // Если после пересечения множество пусто — дальше пересекать бессмысленно
                if (result.isEmpty()) {
                    return Collections.emptySet();
                }
            }
        }

        return result != null ? Collections.unmodifiableSet(result) : Collections.emptySet();
    }
}
