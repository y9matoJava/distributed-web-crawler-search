package io.github.ymatojava.crawler.core.url;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для компонента UrlCanonicalizer.
 * Покрывают требования к нормализации: регистр, порты, сортировка параметров и резолвинг путей.
 */
class UrlCanonicalizerTest {

    private final UrlCanonicalizer canonicalizer = new UrlCanonicalizer();

    /**
     * Проверяет базовую нормализацию:
     * - приведение схемы и хоста к нижнему регистру;
     * - удаление стандартного порта (80);
     * - нормализация пути (удаление /../);
     * - удаление якоря (фрагмента).
     */
    @Test
    void testBasicNormalization() {
        Optional<NormalizedUrl> result = canonicalizer.canonicalize("HTTP://Example.COM:80/path/../file.html#section", null);

        assertTrue(result.isPresent(), "URL should be successfully canonicalized");
        assertEquals("http://example.com/file.html", result.get().value(),
                "URL should be converted to lower case, port 80 and fragment removed, path normalized");
    }

    /**
     * Проверяет корректность обработки query-параметров:
     * - автоматическая сортировка по алфавиту ключа;
     * - удаление маркетинговых и трекинговых меток (utm_source, fbclid).
     */
    @Test
    void testQuerySortingAndTrackingRemoval() {
        String url = "https://example.com/?b=2&utm_source=google&a=1&fbclid=123";
        Optional<NormalizedUrl> result = canonicalizer.canonicalize(url, null);

        assertTrue(result.isPresent());
        assertEquals("https://example.com/?a=1&b=2", result.get().value(),
                "Tracking params should be removed and remaining params should be sorted alphabetically");
    }

    /**
     * Проверяет способность алгоритма корректно преобразовывать
     * относительные ссылки в абсолютные, используя базовый URL страницы.
     */
    @Test
    void testRelativeUrlResolution() {
        Optional<NormalizedUrl> result = canonicalizer.canonicalize("/about-us", "https://example.com/home");

        assertTrue(result.isPresent());
        assertEquals("https://example.com/about-us", result.get().value(),
                "Relative path should be properly resolved against the base URL");
    }

    /**
     * Убеждается, что ссылки с неподдерживаемыми схемами отбрасываются еще на этапе каноникализации.
     */
    @Test
    void testRejectMailto() {
        Optional<NormalizedUrl> result = canonicalizer.canonicalize("mailto:test@example.com", null);
        assertFalse(result.isPresent(), "Mailto links should result in an empty Optional");
    }

    /**
     * Проверяет, что пустой путь (без слэша) корректно нормализуется с добавлением слэша,
     * так как запрос к корню домена всегда неявно обращается к /.
     */
    @Test
    void testEmptyPathBecomesSlash() {
        Optional<NormalizedUrl> result = canonicalizer.canonicalize("http://example.com", null);
        assertTrue(result.isPresent());
        assertEquals("http://example.com/", result.get().value(),
                "URL without explicit path should implicitly get a root '/' path");
    }
}
