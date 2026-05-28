package io.github.ymatojava.crawler.core.search;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для компонента {@link InvertedIndex}.
 * Покрывают сценарии: индексация и поиск по одному ключевому слову,
 * AND-логика, множественные документы, регистронезависимость, несуществующие ключи.
 */
class InvertedIndexTest {

    private final InvertedIndex index = new InvertedIndex();

    /**
     * Проверяет базовый сценарий: после индексации документа с ключевым словом,
     * поиск по этому слову должен вернуть ID документа.
     */
    @Test
    void shouldIndexAndSearchSingleKeyword() {
        index.addDocument(1L, List.of("java", "programming"));

        Set<Long> result = index.search("java");

        assertTrue(result.contains(1L), "Документ 1 должен быть найден по слову 'java'");
        assertEquals(1, result.size(), "По слову 'java' проиндексирован только один документ");
    }

    /**
     * Проверяет, что поиск по несуществующему ключевому слову возвращает
     * пустое множество, а не null или исключение.
     */
    @Test
    void shouldReturnEmptyForUnknownKeyword() {
        index.addDocument(1L, List.of("java"));

        Set<Long> result = index.search("python");

        assertTrue(result.isEmpty(), "Поиск по несуществующему слову должен вернуть пустое множество");
    }

    /**
     * Проверяет AND-логику: searchAll должен возвращать только те документы,
     * которые содержат ВСЕ указанные ключевые слова.
     */
    @Test
    void shouldSearchWithAndLogic() {
        // Документ 1 содержит оба слова
        index.addDocument(1L, List.of("java", "programming"));
        // Документ 2 содержит только "java"
        index.addDocument(2L, List.of("java", "language"));

        // Поиск по обоим словам: только документ 1 содержит и "java", и "programming"
        Set<Long> result = index.searchAll(List.of("java", "programming"));

        assertTrue(result.contains(1L), "Документ 1 содержит оба слова");
        assertFalse(result.contains(2L), "Документ 2 не содержит слово 'programming'");
    }

    /**
     * Проверяет, что одно ключевое слово может ссылаться на несколько документов.
     */
    @Test
    void shouldHandleMultipleDocumentsPerKeyword() {
        index.addDocument(1L, List.of("java"));
        index.addDocument(2L, List.of("java"));
        index.addDocument(3L, List.of("java"));

        Set<Long> result = index.search("java");

        assertEquals(3, result.size(), "Слово 'java' должно ссылаться на 3 документа");
        assertTrue(result.containsAll(Set.of(1L, 2L, 3L)));
    }

    /**
     * Проверяет, что поиск является регистронезависимым:
     * индексация в нижнем регистре, поиск — в произвольном.
     */
    @Test
    void shouldBeCaseInsensitive() {
        index.addDocument(1L, List.of("Java", "Programming"));

        // Поиск в разном регистре должен находить тот же документ
        assertFalse(index.search("JAVA").isEmpty(), "Поиск 'JAVA' должен найти документ");
        assertFalse(index.search("java").isEmpty(), "Поиск 'java' должен найти документ");
        assertFalse(index.search("Java").isEmpty(), "Поиск 'Java' должен найти документ");
        assertFalse(index.search("programming").isEmpty(), "Поиск 'programming' должен найти документ");
    }

    /**
     * Проверяет, что searchAll корректно обрабатывает пустой список ключевых слов.
     */
    @Test
    void shouldReturnEmptyForEmptyKeywordsList() {
        index.addDocument(1L, List.of("java"));

        Set<Long> result = index.searchAll(List.of());

        assertTrue(result.isEmpty(), "Пустой список ключевых слов должен вернуть пустое множество");
    }
}
