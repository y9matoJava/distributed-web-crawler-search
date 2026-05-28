package io.github.ymatojava.crawler.core.engine;

import io.github.ymatojava.crawler.common.url.UrlHash;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

/**
 * Локальная in-memory реализация URL Frontier.
 *
 * Использует {@link LinkedList} как очередь FIFO (First In - First Out) для BFS обхода,
 * и {@link HashSet} для хранения SHA-256 хешей уже встреченных URL, чтобы
 * избежать циклов и повторной обработки.
 *
 * ВНИМАНИЕ: Эта реализация НЕ потокобезопасна и предназначена
 * только для локального однопоточного краулинга (Шаг 6).
 */
public class InMemoryUrlFrontier implements UrlFrontier {

    private final Queue<String> queue = new LinkedList<>();
    private final Set<String> seenHashes = new HashSet<>();
    private int totalDiscovered = 0;

    @Override
    public void add(String url) {
        if (url == null || url.isBlank()) return;

        String hash = UrlHash.sha256(url);
        
        // Добавляем хеш в Set. Метод add() возвращает true,
        // если такого элемента еще не было в Set.
        if (seenHashes.add(hash)) {
            queue.offer(url);
            totalDiscovered++;
        }
    }

    @Override
    public void addAll(Collection<String> urls) {
        if (urls == null) return;
        for (String url : urls) {
            add(url);
        }
    }

    @Override
    public Optional<String> poll() {
        return Optional.ofNullable(queue.poll());
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public int totalDiscovered() {
        return totalDiscovered;
    }

    @Override
    public boolean alreadySeen(String url) {
        if (url == null || url.isBlank()) return false;
        return seenHashes.contains(UrlHash.sha256(url));
    }
}
