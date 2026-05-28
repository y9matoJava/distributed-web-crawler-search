package io.github.ymatojava.crawler.core.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для {@link InMemoryUrlFrontier}.
 *
 * <p>Покрывают базовые операции фронтира: добавление, извлечение,
 * дедупликация, отслеживание обнаруженных URL и пакетное добавление.</p>
 */
class InMemoryUrlFrontierTest {

    private InMemoryUrlFrontier frontier;

    @BeforeEach
    void setUp() {
        frontier = new InMemoryUrlFrontier();
    }

    /**
     * Проверяет базовый сценарий: добавление URL в фронтир
     * и последующее извлечение в порядке FIFO.
     */
    @Test
    void shouldAddAndPollUrl() {
        frontier.add("https://example.com/page1");
        frontier.add("https://example.com/page2");

        assertEquals(2, frontier.size(), "Очередь должна содержать 2 URL");
        assertFalse(frontier.isEmpty(), "Очередь не должна быть пустой");

        // Проверяем FIFO-порядок: первым добавлен — первым извлечён
        Optional<String> first = frontier.poll();
        assertTrue(first.isPresent());
        assertEquals("https://example.com/page1", first.get(),
                "Первый добавленный URL должен быть извлечён первым (FIFO)");

        Optional<String> second = frontier.poll();
        assertTrue(second.isPresent());
        assertEquals("https://example.com/page2", second.get());
    }

    /**
     * Проверяет дедупликацию: повторное добавление одного и того же URL
     * не должно увеличивать очередь.
     * Дедупликация основана на SHA-256 хеше URL.
     */
    @Test
    void shouldDeduplicateUrls() {
        frontier.add("https://example.com/page");
        frontier.add("https://example.com/page");
        frontier.add("https://example.com/page");

        assertEquals(1, frontier.size(),
                "Повторные URL не должны попадать в очередь");
        assertEquals(1, frontier.totalDiscovered(),
                "Общее число обнаруженных должно быть 1");
    }

    /**
     * Проверяет, что poll() из пустого фронтира возвращает Optional.empty(),
     * а не бросает исключение. Это важно для корректного завершения цикла обхода.
     */
    @Test
    void shouldReturnEmptyWhenEmpty() {
        assertTrue(frontier.isEmpty(), "Новый фронтир должен быть пустым");
        assertEquals(0, frontier.size(), "Размер нового фронтира должен быть 0");

        Optional<String> result = frontier.poll();
        assertTrue(result.isEmpty(),
                "poll() из пустого фронтира должен вернуть Optional.empty()");
    }

    /**
     * Проверяет, что totalDiscovered() учитывает все добавленные URL,
     * включая уже извлечённые из очереди.
     * size() показывает только оставшиеся в очереди, а totalDiscovered() — все.
     */
    @Test
    void shouldTrackTotalDiscovered() {
        frontier.add("https://example.com/1");
        frontier.add("https://example.com/2");
        frontier.add("https://example.com/3");

        // Извлекаем один URL из очереди
        frontier.poll();

        assertEquals(2, frontier.size(),
                "В очереди должно остаться 2 URL после poll()");
        assertEquals(3, frontier.totalDiscovered(),
                "Общее число обнаруженных должно быть 3 (включая извлечённый)");
    }

    /**
     * Проверяет метод alreadySeen(): должен возвращать true
     * для URL, которые уже проходили через фронтир.
     */
    @Test
    void shouldReportAlreadySeen() {
        String url = "https://example.com/page";

        assertFalse(frontier.alreadySeen(url),
                "До добавления URL не должен быть отмечен как виденный");

        frontier.add(url);

        assertTrue(frontier.alreadySeen(url),
                "После добавления URL должен быть отмечен как виденный");

        // Даже после извлечения из очереди, URL остаётся «виденным»
        frontier.poll();
        assertTrue(frontier.alreadySeen(url),
                "После poll() URL всё ещё должен быть виденным (для предотвращения повторного обхода)");
    }

    /**
     * Проверяет пакетное добавление URL через addAll().
     * Метод должен добавить только уникальные URL, пропуская дубликаты.
     */
    @Test
    void shouldAddAllUrls() {
        List<String> urls = List.of(
                "https://example.com/1",
                "https://example.com/2",
                "https://example.com/3",
                "https://example.com/1" // дубликат
        );

        frontier.addAll(urls);

        assertEquals(3, frontier.size(),
                "addAll должен добавить только уникальные URL");
        assertEquals(3, frontier.totalDiscovered(),
                "Общее число обнаруженных должно быть 3");
    }
}
