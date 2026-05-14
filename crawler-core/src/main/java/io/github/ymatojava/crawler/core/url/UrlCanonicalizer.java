package io.github.ymatojava.crawler.core.url;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Компонент для каноникализации URL-адресов.
 * Приводит различные формы написания одного и того же URL к единому унифицированному виду.
 * Это необходимо для предотвращения дублирования страниц в хранилище (проблема зеркальных ссылок)
 * и избежания бесконечных циклов обхода (spider traps).
 */
public class UrlCanonicalizer {

    /**
     * Схемы, которые отбрасываются еще до попытки полного парсинга,
     * чтобы сэкономить ресурсы процессора на заведомо мусорных ссылках.
     */
    private static final Set<String> REJECTED_SCHEMES = Set.of("mailto", "tel", "javascript");

    /**
     * Основной метод нормализации URL.
     *
     * @param urlStr     Исходный URL (может быть абсолютным или относительным)
     * @param baseUrlStr Базовый URL страницы, на которой найдена ссылка (используется для разрешения относительных путей)
     * @return Optional с нормализованным URL, либо Optional.empty(), если URL некорректен
     */
    public Optional<NormalizedUrl> canonicalize(String urlStr, String baseUrlStr) {
        if (urlStr == null || urlStr.isBlank()) {
            return Optional.empty();
        }

        try {
            // 1. Быстрая предварительная фильтрация без тяжелого конструирования объекта URI
            String lowerUrl = urlStr.trim().toLowerCase();
            for (String scheme : REJECTED_SCHEMES) {
                if (lowerUrl.startsWith(scheme + ":")) {
                    return Optional.empty();
                }
            }

            // 2. Парсинг исходного URL
            URI uri = new URI(urlStr.trim());

            // 3. Разрешение (resolving) относительных путей.
            // Если ссылка вида "/about", а базовая страница "https://site.com",
            // резолвер корректно соберет абсолютный путь: "https://site.com/about".
            if (baseUrlStr != null && !baseUrlStr.isBlank()) {
                URI baseUri = new URI(baseUrlStr.trim());
                uri = baseUri.resolve(uri);
            }

            // 4. Нормализация пути (устранение навигационных сегментов "." и "..")
            // Превращает "https://site.com/a/../b" в "https://site.com/b"
            uri = uri.normalize();

            // 5. Проверка схемы и приведение к нижнему регистру
            String scheme = uri.getScheme();
            if (scheme != null) {
                scheme = scheme.toLowerCase();
                if (REJECTED_SCHEMES.contains(scheme)) {
                    return Optional.empty();
                }
            }

            // 6. Приведение хоста к нижнему регистру (по стандарту RFC домен нечувствителен к регистру)
            String host = uri.getHost();
            if (host != null) {
                host = host.toLowerCase();
            }

            // 7. Удаление стандартных портов протоколов.
            // "http://site.com" и "http://site.com:80" — это одна и та же сущность.
            int port = uri.getPort();
            if (("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) {
                port = -1; // Значение -1 в java.net.URI означает отсутствие явно заданного порта
            }

            // 8. Обработка пустого пути (добавление '/' в конец, если пути нет)
            // Web-сервера трактуют "site.com" и "site.com/" одинаково.
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }

            // 9. Обработка query-параметров: удаление трекинговых меток и их лексикографическая сортировка
            String newQuery = processQuery(uri.getRawQuery());

            // 10. Сборка очищенного URI.
            // Фрагмент (якорь #) намеренно передается как null, так как он обрабатывается только
            // на стороне клиента (в браузере) и сервер всегда отдает одну и ту же страницу.
            URI baseWithoutQuery = new URI(
                    scheme,
                    uri.getUserInfo(),
                    host,
                    port,
                    path,
                    null, // Query добавляем отдельно во избежание проблем с двойным кодированием
                    null  // Фрагмент отбрасывается
            );

            // 11. Добавление обработанных параметров обратно
            String canonicalStr = baseWithoutQuery.toString();
            if (newQuery != null && !newQuery.isEmpty()) {
                canonicalStr += "?" + newQuery;
            }

            // Финальная валидация строки путем создания итогового URI
            return Optional.of(new NormalizedUrl(new URI(canonicalStr).toString()));

        } catch (Exception e) {
            // В случае URISyntaxException ссылка считается не подлежащей парсингу
            return Optional.empty();
        }
    }

    /**
     * Обрабатывает строку параметров (query): декодирует, фильтрует мусорные ключи,
     * сортирует и кодирует обратно.
     *
     * @param rawQuery Сырая строка параметров из URI
     * @return Отсортированная и отфильтрованная строка, либо null
     */
    private String processQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return null;
        }

        // TreeMap автоматически сортирует элементы по ключу при вставке (Red-Black Tree).
        // Это гарантирует, что параметры "?b=1&a=2" и "?a=2&b=1" превратятся в одинаковый URL.
        Map<String, String> params = new TreeMap<>();
        String[] pairs = rawQuery.split("&");

        for (String pair : pairs) {
            if (pair.isBlank()) continue;

            int idx = pair.indexOf('=');
            // Декодируем ключ и значение из URL-формата
            String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : URLDecoder.decode(pair, StandardCharsets.UTF_8);
            String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : "";

            // Фильтруем параметры, не влияющие на контент
            String lowerKey = key.toLowerCase();
            if (!isTrackingParam(lowerKey)) {
                params.put(key, value);
            }
        }

        if (params.isEmpty()) {
            return null;
        }

        // Собираем параметры обратно в строку, применяя корректный URL-encoding
        return params.entrySet().stream()
                .map(e -> encodeParam(e.getKey()) + (e.getValue().isEmpty() ? "" : "=" + encodeParam(e.getValue())))
                .collect(Collectors.joining("&"));
    }

    /**
     * Определяет, является ли параметр маркетинговой меткой (например, от Google Analytics),
     * которая ведет на ту же самую страницу, создавая дубли в базе краулера.
     */
    private boolean isTrackingParam(String paramName) {
        return paramName.startsWith("utm_") || paramName.equals("fbclid") || paramName.equals("gclid");
    }

    /**
     * Кодирует строковый параметр в стандартный формат URL.
     * Стандартный класс URLEncoder кодирует пробелы как '+', что подходит для форм,
     * но по стандарту URI (RFC 3986) пробелы должны кодироваться как '%20'.
     */
    private String encodeParam(String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8).replace("+", "%20");
    }
}

