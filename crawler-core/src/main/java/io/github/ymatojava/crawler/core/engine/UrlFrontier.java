package io.github.ymatojava.crawler.core.engine;

import java.util.Collection;
import java.util.Optional;

/**
 * Интерфейс URL Frontier (очереди обхода).
 *
 * Отвечает за хранение URL, которые нужно посетить,
 * а также за предотвращение повторного посещения одних и тех же ссылок.
 */
public interface UrlFrontier {

    /**
     * Добавляет URL в очередь, если он не был добавлен ранее.
     *
     * @param url Нормализованный URL
     */
    void add(String url);

    /**
     * Добавляет пакет URL в очередь.
     *
     * @param urls Коллекция нормализованных URL
     */
    void addAll(Collection<String> urls);

    /**
     * Извлекает следующий URL для обхода.
     *
     * @return Optional с URL, либо empty, если очередь пуста
     */
    Optional<String> poll();

    /**
     * Проверяет, пуста ли очередь ожидания.
     *
     * @return true, если больше нет URL для обхода
     */
    boolean isEmpty();

    /**
     * Возвращает текущий размер очереди (ожидающих URL).
     *
     * @return Количество URL в очереди
     */
    int size();

    /**
     * Возвращает общее количество уникальных URL, пропущенных через Frontier
     * с момента его создания (включая уже обработанные).
     *
     * @return Общее количество обнаруженных URL
     */
    int totalDiscovered();

    /**
     * Проверяет, был ли URL уже добавлен в очередь (или обработан) ранее.
     *
     * @param url Нормализованный URL
     * @return true, если URL уже известен системе
     */
    boolean alreadySeen(String url);
}
