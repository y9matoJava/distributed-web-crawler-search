package io.github.ymatojava.crawler.core.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Set;

/**
 * Парсер HTML-документов.
 *
 * Преобразует сырой HTML-код в структурированный объект {@link ParsedPage},
 * извлекая заголовок, текстовый контент и исходящие ссылки.
 */
public class PageParser {

    private final LinkExtractor linkExtractor;

    /**
     * Создает парсер с внедренным извлекателем ссылок.
     *
     * @param linkExtractor Компонент для извлечения URL
     */
    public PageParser(LinkExtractor linkExtractor) {
        this.linkExtractor = linkExtractor;
    }

    /**
     * Парсит веб-страницу.
     *
     * @param url  Оригинальный URL скачанной страницы
     * @param html Сырой HTML-код
     * @return {@link ParsedPage} с извлеченными данными
     */
    public ParsedPage parse(String url, String html) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL не может быть null или пустым");
        }
        if (html == null || html.isBlank()) {
            return new ParsedPage(url, "", "", Set.of());
        }

        // Парсим DOM через Jsoup
        Document doc = Jsoup.parse(html, url);

        // 1. Извлекаем заголовок
        String title = doc.title();

        // 2. Извлекаем видимый текст.
        // Метод text() автоматически исключает содержимое тегов <script> и <style>,
        // объединяя текстовые ноды через пробел.
        String bodyText = "";
        if (doc.body() != null) {
            bodyText = doc.body().text();
        }

        // 3. Извлекаем исходящие ссылки
        Set<String> outLinks = linkExtractor.extractLinks(html, url);

        return new ParsedPage(url, title, bodyText, outLinks);
    }
}
