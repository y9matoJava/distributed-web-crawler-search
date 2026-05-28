package io.github.ymatojava.crawler.core.engine;

import io.github.ymatojava.crawler.common.url.UrlHash;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Реализация фронтира URL в оперативной памяти.
 *
 * <p>Внутренняя архитектура:
 * <ul>
 *     <li>{@link LinkedList} в качестве FIFO-очереди — обеспечивает порядок BFS (поуровневый обход).
 *         Время операций poll/add — O(1).</li>
 *     <li>{@link HashSet} для хранения SHA-256 хешей уже обнаруженных URL — обеспечивает
 *         дедупликацию за O(1). Хеширование вместо хранения полных URL экономит оперативную
 *         память при обходе больших сайтов (SHA-256 = 64 символа vs средний URL ~100+ символов).</li>
 * </ul>
 *
 * <p><b>Потокобезопасность:</b> данная реализация НЕ является потокобезопасной.
 * Для однопоточного in-memory краулера этого достаточно.
 * Для распределённой версии потребуется реализация на базе Redis.</p>
 */
public class InMemoryUrlFrontier implements UrlFrontier {

    /**
     * FIFO-очередь URL, ожидающих обработки.
     * LinkedList выбран вместо ArrayDeque, так как мы не знаем заранее количество URL.
     */
    private final Queue<String> queue = new LinkedList<>();

    /**
     * Множество SHA-256 хешей всех обнаруженных URL.
     * Используется для мгновенной проверки «видели ли мы этот URL раньше».
     */
    private final Set<String> seenHashes = new HashSet<>();

    /**
     * {@inheritDoc}
     *
     * <p>URL хешируется через {@link UrlHash#sha256(String)} и добавляется в очередь,
     * только если его хеш ещё не встречался. Это предотвращает повторный обход
     * одних и тех же страниц.</p>
     */
    @Override
    public void add(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        // Вычисляем хеш URL для дедупликации.
        // SHA-256 используется вместо простого hashCode() для минимизации коллизий
        // при большом объёме обнаруженных URL.
        String hash = UrlHash.sha256(url);

        // seenHashes.add() возвращает true, если элемент был добавлен (ещё не существовал)
        if (seenHashes.add(hash)) {
            queue.add(url);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Итерирует по коллекции и делегирует в {@link #add(String)},
     * который выполняет дедупликацию для каждого URL.</p>
     */
    @Override
    public void addAll(Collection<String> urls) {
        if (urls == null) {
            return;
        }
        for (String url : urls) {
            add(url);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Извлекает URL из головы FIFO-очереди.
     * Возвращает {@link Optional#empty()}, если очередь исчерпана.</p>
     */
    @Override
    public Optional<String> poll() {
        String url = queue.poll();
        return Optional.ofNullable(url);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @return количество URL в очереди, ожидающих обработки
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Возвращает размер множества хешей — это общее число уникальных URL,
     * которые когда-либо проходили через фронтир (включая уже извлечённые из очереди).</p>
     */
    @Override
    public int totalDiscovered() {
        return seenHashes.size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Проверяет наличие SHA-256 хеша URL в множестве обнаруженных.
     * Операция выполняется за O(1).</p>
     */
    @Override
    public boolean alreadySeen(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return seenHashes.contains(UrlHash.sha256(url));
    }
}
