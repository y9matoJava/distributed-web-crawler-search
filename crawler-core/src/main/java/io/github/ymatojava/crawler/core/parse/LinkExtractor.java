package io.github.ymatojava.crawler.core.parse;

import io.github.ymatojava.crawler.core.url.NormalizedUrl;
import io.github.ymatojava.crawler.core.url.UrlCanonicalizer;
import io.github.ymatojava.crawler.core.url.UrlFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Компонент для извлечения гиперссылок из HTML-документа.
 *
 * <p>Алгоритм работы:
 * <ol>
 *     <li>Парсинг HTML с помощью Jsoup (толерантный к невалидному HTML)</li>
 *     <li>Выборка всех элементов {@code <a>} с атрибутом {@code href}</li>
 *     <li>Разрешение (resolve) относительных URL через Jsoup {@code absUrl("href")}</li>
 *     <li>Каноникализация каждого URL через {@link UrlCanonicalizer}</li>
 *     <li>Фильтрация через {@link UrlFilter} (только http/https протоколы)</li>
 * </ol>
 *
 * <p>Результат возвращается в виде {@link Set}, что обеспечивает автоматическую
 * дедупликацию ссылок (одна страница может содержать несколько ссылок на один URL).</p>
 */
public class LinkExtractor {

    /**
     * Каноникализатор URL для приведения ссылок к единому формату.
     */
    private final UrlCanonicalizer canonicalizer;

    /**
     * Фильтр URL для отсечения неподдерживаемых схем (mailto, javascript и т.д.).
     */
    private final UrlFilter urlFilter;

    /**
     * Создаёт экземпляр LinkExtractor с зависимостями по умолчанию.
     */
    public LinkExtractor() {
        this(new UrlCanonicalizer(), new UrlFilter());
    }

    /**
     * Создаёт экземпляр LinkExtractor с явным указанием зависимостей.
     * Позволяет подставить моки в тестах.
     *
     * @param canonicalizer каноникализатор URL
     * @param urlFilter     фильтр URL
     */
    public LinkExtractor(UrlCanonicalizer canonicalizer, UrlFilter urlFilter) {
        this.canonicalizer = canonicalizer;
        this.urlFilter = urlFilter;
    }

    /**
     * Извлекает и нормализует все исходящие ссылки из HTML-документа.
     *
     * <p>Ссылки с невалидными URL, а также ссылки с неподдерживаемыми схемами
     * (javascript:, mailto:, tel: и т.д.) тихо игнорируются — краулер не должен
     * прерывать обход из-за мусорных ссылок на одной странице.</p>
     *
     * @param html    HTML-содержимое страницы
     * @param baseUrl базовый URL страницы, используется для разрешения относительных ссылок
     * @return множество нормализованных абсолютных URL
     */
    public Set<String> extractLinks(String html, String baseUrl) {
        if (html == null || html.isBlank()) {
            return Set.of();
        }

        // Jsoup требует baseUri для корректного разрешения относительных ссылок.
        // Если baseUrl не задан, используем пустую строку (относительные ссылки не будут разрешены).
        String safeBaseUrl = (baseUrl != null) ? baseUrl : "";

        // Парсинг HTML. Jsoup толерантен к невалидному HTML и не выбрасывает исключений.
        Document doc = Jsoup.parse(html, safeBaseUrl);

        // Выбираем все элементы <a> с атрибутом href.
        // CSS-селектор a[href] пропускает якоря без href, которые используются как точки привязки.
        Elements anchors = doc.select("a[href]");

        // LinkedHashSet сохраняет порядок вставки, что удобно для отладки и тестирования
        Set<String> result = new LinkedHashSet<>();

        for (Element anchor : anchors) {
            // absUrl("href") разрешает относительный href в абсолютный URL
            // на основе baseUri, переданного в Jsoup.parse().
            // Если href уже абсолютный (начинается с http:// или https://), он возвращается как есть.
            String absoluteUrl = anchor.absUrl("href");

            // absUrl() возвращает пустую строку, если не удалось разрешить ссылку
            if (absoluteUrl.isEmpty()) {
                continue;
            }

            // Каноникализация: приведение к единому формату (нижний регистр, удаление фрагментов и т.д.)
            Optional<NormalizedUrl> normalized = canonicalizer.canonicalize(absoluteUrl, null);

            if (normalized.isEmpty()) {
                continue;
            }

            String canonicalUrl = normalized.get().value();

            // Финальная фильтрация: пропускаем только http/https
            if (urlFilter.isValid(canonicalUrl)) {
                result.add(canonicalUrl);
            }
        }

        return result;
    }
}
