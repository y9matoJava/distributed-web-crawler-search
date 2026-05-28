package io.github.ymatojava.crawler.core.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Set;

/**
 * Компонент для полного парсинга HTML-страницы.
 *
 * <p>Извлекает из HTML-документа все данные, необходимые для поисковой индексации:
 * <ul>
 *     <li>Заголовок страницы (тег {@code <title>})</li>
 *     <li>Видимый текст из {@code <body>} (без скриптов, стилей и HTML-тегов)</li>
 *     <li>Исходящие ссылки для продолжения обхода</li>
 * </ul>
 *
 * <p>Используется на этапе обработки скачанного HTML в конвейере краулера.
 * Результат парсинга упаковывается в неизменяемый объект {@link ParsedPage}.</p>
 */
public class PageParser {

    /**
     * Компонент для извлечения ссылок из HTML.
     * Вынесен в отдельный класс для соблюдения принципа единой ответственности (SRP).
     */
    private final LinkExtractor linkExtractor;

    /**
     * Создаёт парсер с экстрактором ссылок по умолчанию.
     */
    public PageParser() {
        this(new LinkExtractor());
    }

    /**
     * Создаёт парсер с явно указанным экстрактором ссылок.
     * Позволяет подставить мок или кастомную реализацию в тестах.
     *
     * @param linkExtractor экстрактор ссылок для делегирования
     */
    public PageParser(LinkExtractor linkExtractor) {
        this.linkExtractor = linkExtractor;
    }

    /**
     * Выполняет полный парсинг HTML-документа.
     *
     * <p>Алгоритм:
     * <ol>
     *     <li>Парсинг HTML через Jsoup (толерантный парсер, не падает на кривом HTML)</li>
     *     <li>Извлечение заголовка через {@code doc.title()}</li>
     *     <li>Извлечение видимого текста из {@code <body>}: Jsoup автоматически отбрасывает
     *         скрипты, стили и HTML-теги, оставляя только человекочитаемый текст</li>
     *     <li>Извлечение ссылок через {@link LinkExtractor}</li>
     * </ol>
     *
     * @param url  URL страницы (используется как идентификатор и для разрешения относительных ссылок)
     * @param html HTML-содержимое страницы
     * @return объект {@link ParsedPage} с извлечёнными данными
     */
    public ParsedPage parse(String url, String html) {
        if (html == null || html.isBlank()) {
            // Если HTML пуст, возвращаем «пустую» страницу без данных.
            // Это лучше, чем бросать исключение, т.к. краулер должен продолжать обход.
            return new ParsedPage(url, "", "", Set.of());
        }

        // Парсинг HTML. baseUri передаётся для корректного разрешения относительных путей.
        Document doc = Jsoup.parse(html, url);

        // Извлечение заголовка. doc.title() возвращает пустую строку, если тег <title> отсутствует.
        String title = doc.title();

        // Извлечение видимого текста из body.
        // Метод text() рекурсивно обходит DOM-дерево и собирает только текстовые узлы,
        // игнорируя HTML-теги, но сохраняя порядок слов.
        String bodyText = "";
        Element body = doc.body();
        if (body != null) {
            bodyText = body.text();
        }

        // Извлечение и нормализация исходящих ссылок
        Set<String> outLinks = linkExtractor.extractLinks(html, url);

        return new ParsedPage(url, title, bodyText, outLinks);
    }
}
