package io.github.ymatojava.crawler.common.url;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для утилитного класса UrlHash.
 * Проверяют корректность генерации SHA-256 хешей и обработку граничных случаев.
 */
class UrlHashTest {

    /**
     * Проверяет, что SHA-256 хеш генерируется и имеет правильную длину (64 hex-символа).
     */
    @Test
    void shouldGenerateHashWithCorrectLength() {
        String hash = UrlHash.sha256("https://example.com/");
        assertNotNull(hash, "Хеш не должен быть null");
        assertEquals(64, hash.length(), "SHA-256 хеш должен содержать ровно 64 hex-символа");
    }

    /**
     * Проверяет детерминизм: одинаковый вход всегда даёт одинаковый выход.
     * Это критически важно для дедупликации в БД.
     */
    @Test
    void shouldProduceSameHashForSameInput() {
        String url = "https://example.com/page";
        String hash1 = UrlHash.sha256(url);
        String hash2 = UrlHash.sha256(url);
        assertEquals(hash1, hash2, "Один и тот же URL должен всегда давать одинаковый хеш");
    }

    /**
     * Проверяет, что разные URL дают разные хеши.
     * Вероятность коллизии SHA-256 пренебрежимо мала (~1/2^128).
     */
    @Test
    void shouldProduceDifferentHashesForDifferentUrls() {
        String hash1 = UrlHash.sha256("https://example.com/page1");
        String hash2 = UrlHash.sha256("https://example.com/page2");
        assertNotEquals(hash1, hash2, "Разные URL должны иметь разные хеши");
    }

    /**
     * Проверяет, что null-значение вызывает IllegalArgumentException.
     */
    @Test
    void shouldRejectNullUrl() {
        assertThrows(IllegalArgumentException.class, () -> UrlHash.sha256(null),
                "null должен вызывать IllegalArgumentException");
    }

    /**
     * Проверяет, что пустая строка вызывает IllegalArgumentException.
     */
    @Test
    void shouldRejectEmptyUrl() {
        assertThrows(IllegalArgumentException.class, () -> UrlHash.sha256(""),
                "Пустая строка должна вызывать IllegalArgumentException");
    }

    /**
     * Проверяет, что строка из пробелов вызывает IllegalArgumentException.
     */
    @Test
    void shouldRejectBlankUrl() {
        assertThrows(IllegalArgumentException.class, () -> UrlHash.sha256("   "),
                "Строка из пробелов должна вызывать IllegalArgumentException");
    }

    /**
     * Проверяет, что хеш содержит только hex-символы (0-9, a-f).
     */
    @Test
    void shouldContainOnlyHexCharacters() {
        String hash = UrlHash.sha256("https://example.com/");
        assertTrue(hash.matches("^[0-9a-f]+$"),
                "Хеш должен содержать только hex-символы нижнего регистра");
    }
}
