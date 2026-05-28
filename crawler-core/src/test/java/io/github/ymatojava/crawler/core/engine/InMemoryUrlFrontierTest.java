package io.github.ymatojava.crawler.core.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для InMemoryUrlFrontier.
 */
class InMemoryUrlFrontierTest {

    private InMemoryUrlFrontier frontier;

    @BeforeEach
    void setUp() {
        frontier = new InMemoryUrlFrontier();
    }

    @Test
    void shouldAddAndPollUrl() {
        frontier.add("https://example.com");

        assertFalse(frontier.isEmpty());
        assertEquals(1, frontier.size());

        String url = frontier.poll().orElse(null);
        assertEquals("https://example.com", url);
        assertTrue(frontier.isEmpty());
    }

    @Test
    void shouldDeduplicateUrls() {
        // Добавляем один и тот же URL трижды
        frontier.add("https://example.com");
        frontier.add("https://example.com");
        frontier.add("https://example.com");

        // Очередь должна содержать только 1 элемент
        assertEquals(1, frontier.size());
        assertEquals(1, frontier.totalDiscovered());
    }

    @Test
    void shouldReturnEmptyWhenEmpty() {
        assertTrue(frontier.isEmpty());
        assertFalse(frontier.poll().isPresent());
    }

    @Test
    void shouldTrackTotalDiscovered() {
        frontier.addAll(List.of(
                "https://example.com/1",
                "https://example.com/2",
                "https://example.com/1" // Дубликат
        ));

        assertEquals(2, frontier.size());
        assertEquals(2, frontier.totalDiscovered());

        // После извлечения размер уменьшается, но totalDiscovered остается
        frontier.poll();
        assertEquals(1, frontier.size());
        assertEquals(2, frontier.totalDiscovered());
    }

    @Test
    void shouldReportAlreadySeen() {
        assertFalse(frontier.alreadySeen("https://example.com"));

        frontier.add("https://example.com");

        assertTrue(frontier.alreadySeen("https://example.com"));
    }
}
