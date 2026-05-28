package io.github.ymatojava.crawler.core.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для InvertedIndex.
 */
class InvertedIndexTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
    }

    @Test
    void shouldIndexAndSearchSingleKeyword() {
        index.addDocument(1L, List.of("java", "spring"));
        
        Set<Long> docs = index.search("java");
        assertEquals(1, docs.size());
        assertTrue(docs.contains(1L));
    }

    @Test
    void shouldReturnEmptyForUnknownKeyword() {
        index.addDocument(1L, List.of("java"));
        
        assertTrue(index.search("python").isEmpty());
    }

    @Test
    void shouldSearchWithAndLogic() {
        index.addDocument(1L, List.of("java", "spring", "boot"));
        index.addDocument(2L, List.of("java", "python"));
        index.addDocument(3L, List.of("spring", "boot"));

        Set<Long> result = index.searchAll(List.of("java", "spring"));
        
        // Только документ 1 содержит и java, и spring
        assertEquals(1, result.size());
        assertTrue(result.contains(1L));
    }

    @Test
    void shouldHandleMultipleDocumentsPerKeyword() {
        index.addDocument(1L, List.of("java"));
        index.addDocument(2L, List.of("java"));
        index.addDocument(3L, List.of("java"));

        Set<Long> docs = index.search("java");
        assertEquals(3, docs.size());
        assertTrue(docs.containsAll(List.of(1L, 2L, 3L)));
    }

    @Test
    void shouldBeCaseInsensitive() {
        index.addDocument(1L, List.of("JAVA", "Spring"));

        assertTrue(index.search("java").contains(1L));
        assertTrue(index.search("SPRING").contains(1L));
    }
}
