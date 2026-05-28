package io.github.ymatojava.crawler.common.url;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Утилитный класс для генерации детерминированного хеша URL-адресов.
 *
 * Используется для дедупликации URL на уровне базы данных:
 * вместо сравнения полных строк (которые могут быть очень длинными)
 * сравниваются компактные SHA-256 хеши фиксированной длины 64 символа.
 *
 * Выбор SHA-256 обоснован:
 * - детерминизм: одинаковый вход → одинаковый выход (в отличие от hashCode());
 * - низкая вероятность коллизий: 2^256 возможных значений;
 * - стандартизация: поддерживается всеми СУБД и языками;
 * - фиксированная длина: всегда 64 hex-символа, удобно для индексов в БД.
 *
 * Пример использования:
 * <pre>
 *   String hash = UrlHash.sha256("https://example.com/page");
 *   // Результат: "a1b2c3d4..." (64 hex-символа)
 * </pre>
 */
public final class UrlHash {

    /**
     * Закрытый конструктор предотвращает создание экземпляров утилитного класса.
     */
    private UrlHash() {
        throw new UnsupportedOperationException("Утилитный класс не предназначен для создания экземпляров");
    }

    /**
     * Генерирует SHA-256 хеш для переданного каноникализированного URL.
     *
     * Алгоритм:
     * 1. Преобразуем строку URL в массив байтов (кодировка UTF-8)
     * 2. Вычисляем SHA-256 дайджест
     * 3. Конвертируем байтовый массив в hex-строку
     *
     * @param canonicalUrl Нормализованный URL-адрес (должен быть предварительно
     *                     обработан через UrlCanonicalizer)
     * @return Hex-строка SHA-256 хеша длиной 64 символа
     * @throws IllegalArgumentException если URL равен null или пуст
     */
    public static String sha256(String canonicalUrl) {
        if (canonicalUrl == null || canonicalUrl.isBlank()) {
            throw new IllegalArgumentException("URL для хеширования не может быть null или пустым");
        }

        try {
            // MessageDigest — стандартный JDK-класс для криптографических хешей.
            // getInstance() ищет реализацию алгоритма в зарегистрированных провайдерах.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Преобразуем строку в байты с явным указанием кодировки.
            // UTF-8 — единственно правильный выбор для интернет-контента.
            byte[] hashBytes = digest.digest(canonicalUrl.getBytes(StandardCharsets.UTF_8));

            // HexFormat (Java 17+) — современная замена устаревшим подходам
            // через StringBuilder + String.format("%02x"), BigInteger и т.п.
            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 гарантированно поддерживается всеми реализациями JDK (по спецификации),
            // поэтому это исключение никогда не должно возникнуть в рантайме.
            throw new RuntimeException("SHA-256 алгоритм не найден в JVM — это не должно происходить", e);
        }
    }
}
