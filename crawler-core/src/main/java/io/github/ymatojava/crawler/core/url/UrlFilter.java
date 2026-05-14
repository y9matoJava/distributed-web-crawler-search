package io.github.ymatojava.crawler.core.url;


import java.net.URI;
import java.util.Set;

/**
 * Класс для фильтрации URL адресов
 * Отсеивает адреса, которые краулер не должен или не может обрабатывать
 */
public class UrlFilter {

    /**
     * Набор схем, которые 100% игнорируем.
     * Используется Set для обеспечения времени поиска O(1)
     */
    private static final Set<String> REJECTED_SCHEMES = Set.of(
            "mailto", "ftp", "file", "javascript", "tel"
    );

    /**
     * Проверяет, является ли переданный URL допустимым для обхода
     *
     * @param urlStr строковое представление URL для проверки
     * @return true, если URL разрешен, иначе false
     */

    public boolean isValid(String urlStr) {
        // 1. Отбрасываем пустые строки на входе
        if (urlStr == null || urlStr.isBlank()) {
            return false;
        }

        // 2. Отбрасываем ссылки, состоящие только из якоря
        // т.к. они ссылаются на ту же страницу и не требуют сетевого запроса.
        if (urlStr.trim().startsWith("#")) {
            return false;
        }

        try {
            // 3. Пытаемся распарсить строку в объект URI.
            // Если строка нарушает синтаксис RFC 2396, будет выброшено исключение
            URI uri = new URI(urlStr.trim());
            String scheme = uri.getScheme();

            // если схема отсутствует, это относительный URL, который к этому этапу
            // уже должен был быть разрешен в абсолютный в UrlCanonicalizer

            if (scheme == null) {
                return false;
            }

            // приводим схему к нижнему регистру
            scheme = scheme.toLowerCase();

            // 4. Отсекаем схемы из черного списка
            if (REJECTED_SCHEMES.contains(scheme)) {
                return false;
            }

            // 5. Разрешаем исключительно веб протоколы
            return scheme.equals("http") || scheme.equals("https");
        } catch (Exception e) {
            // Любое исключение при парсинге, означает что URL синтаксически сломан
            // Краулер должен игнорировать такие ссылки, чтобы не прерывать работу
            return false;
        }
    }
}
