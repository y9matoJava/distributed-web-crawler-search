package io.github.ymatojava.crawler.core.parse;

import io.github.ymatojava.crawler.core.url.NormalizedUrl;
import io.github.ymatojava.crawler.core.url.UrlCanonicalizer;
import io.github.ymatojava.crawler.core.url.UrlFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Компонент для извлечения ссылок (тегов &lt;a href="..."&gt;) из HTML-документа.
 *
 * Использует Jsoup для парсинга DOM-дерева и обработки относительных URL-адресов.
 * Каждая найденная ссылка проходит строгую валидацию:
 * 1. Превращается в абсолютную (с учетом тега &lt;base&gt; или базового URL).
 * 2. Каноникализируется через {@link UrlCanonicalizer}.
 * 3. Фильтруется через {@link UrlFilter} (отбрасываем mailto:, javascript: и т.п.).
 */
public class LinkExtractor {

    private final UrlCanonicalizer canonicalizer;
    private final UrlFilter filter;

    /**
     * Конструктор с инжекцией зависимостей.
     *
     * @param canonicalizer Компонент нормализации URL
     * @param filter        Компонент фильтрации URL
     */
    public LinkExtractor(UrlCanonicalizer canonicalizer, UrlFilter filter) {
        this.canonicalizer = canonicalizer;
        this.filter = filter;
    }

    /**
     * Извлекает все допустимые исходящие ссылки из сырого HTML-кода.
     *
     * @param html    Исходный HTML-код страницы
     * @param baseUrl Базовый URL страницы (для разрешения относительных ссылок)
     * @return Набор (Set) уникальных нормализованных ссылок
     */
    public Set<String> extractLinks(String html, String baseUrl) {
        if (html == null || html.isBlank()) {
            return Set.of();
        }

        Set<String> extractedLinks = new HashSet<>();
        
        try {
            // Парсим HTML через Jsoup.
            // Передача baseUrl критически важна для корректной работы метода absUrl()
            Document doc = Jsoup.parse(html, baseUrl != null ? baseUrl : "");

            // Извлекаем все теги 'a', у которых есть атрибут 'href'
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                // Получаем АБСОЛЮТНЫЙ URL (Jsoup сам разрешает относительные пути)
                String rawUrl = link.absUrl("href");
                
                if (rawUrl.isBlank()) {
                    // Если Jsoup не смог собрать абсолютный URL (например, из-за mailto:),
                    // берем оригинальный атрибут href.
                    rawUrl = link.attr("href");
                }

                String originalHref = link.attr("href");
                if (originalHref.startsWith("#")) {
                    continue; // Игнорируем ссылки, которые являются только якорями на текущей странице
                }

                // Шаг 1: Нормализация
                Optional<NormalizedUrl> normalizedOpt = canonicalizer.canonicalize(rawUrl, baseUrl);
                
                if (normalizedOpt.isPresent()) {
                    String canonicalStr = normalizedOpt.get().value();
                    
                    // Шаг 2: Фильтрация
                    if (filter.isValid(canonicalStr)) {
                        extractedLinks.add(canonicalStr);
                    }
                }
            }
        } catch (Exception e) {
            // Любая ошибка при парсинге конкретной страницы не должна ронять весь процесс.
            // В случае ошибки возвращаем те ссылки, которые успели собрать, либо пустой Set.
            // Логирование будет добавлено позже.
        }

        return extractedLinks;
    }
}
