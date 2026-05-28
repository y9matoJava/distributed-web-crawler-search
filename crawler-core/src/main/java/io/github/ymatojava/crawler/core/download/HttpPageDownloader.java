package io.github.ymatojava.crawler.core.download;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Реализация загрузчика веб-страниц на основе {@link java.net.http.HttpClient} (Java 11+).
 *
 * Выбор HttpClient обоснован:
 * - входит в стандартную библиотеку JDK (нет внешних зависимостей);
 * - поддерживает HTTP/2 из коробки;
 * - поддерживает асинхронные запросы (на будущее);
 * - поддерживает автоматическое следование редиректам.
 *
 * Ключевые принципы реализации:
 * 1. Никогда не бросать исключения наружу — все ошибки оборачиваются в {@link DownloadResult.Failure}
 * 2. Уважать таймауты — не блокировать поток бесконечно
 * 3. Проверять Content-Type — краулер обрабатывает только HTML
 * 4. Лимитировать размер ответа — защита от утечки памяти
 * 5. Отправлять User-Agent — идентификация бота (требование robots.txt)
 */
public class HttpPageDownloader implements PageDownloader {

    /**
     * HTTP-клиент, переиспользуемый для всех запросов.
     * HttpClient потокобезопасен и создаёт пул соединений (connection pool),
     * поэтому создание нового клиента на каждый запрос — антипаттерн.
     */
    private final HttpClient httpClient;

    /**
     * Конфигурация краулера с параметрами загрузки.
     */
    private final CrawlerConfig config;

    /**
     * Создаёт загрузчик с указанной конфигурацией.
     *
     * @param config Конфигурация краулера (таймауты, User-Agent, лимиты)
     */
    public HttpPageDownloader(CrawlerConfig config) {
        this.config = config;

        // Строим HTTP-клиент с ограничениями:
        // - NORMAL redirect policy: автоматически следует за 301/302/307/308
        // - connectTimeout: максимальное время на установление TCP-соединения
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(config.downloadTimeout())
                .build();
    }

    /**
     * Загружает содержимое веб-страницы по указанному URL.
     *
     * Последовательность действий:
     * 1. Конструирование HTTP-запроса с нужными заголовками
     * 2. Отправка запроса и получение ответа
     * 3. Валидация HTTP-кода ответа
     * 4. Проверка Content-Type (только text/html)
     * 5. Проверка размера тела ответа
     * 6. Оборачивание результата в Success или Failure
     *
     * @param url Абсолютный URL для загрузки
     * @return Результат загрузки: {@link DownloadResult.Success} или {@link DownloadResult.Failure}
     */
    @Override
    public DownloadResult download(String url) {
        // 0. Базовая валидация входных данных
        if (url == null || url.isBlank()) {
            return new DownloadResult.Failure(url, "URL равен null или пуст");
        }

        try {
            // 1. Конструируем HTTP-запрос.
            // GET — единственный метод, который краулер должен использовать.
            // Отправка POST/PUT/DELETE может модифицировать данные на стороннем сервере.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", config.userAgent())
                    .header("Accept", "text/html,application/xhtml+xml")
                    .header("Accept-Language", "en-US,en;q=0.9,ru;q=0.8")
                    .timeout(config.downloadTimeout())
                    .GET()
                    .build();

            // 2. Отправляем запрос синхронно.
            // BodyHandlers.ofString() считывает весь ответ в строку.
            // Для очень больших страниц это может потребовать много памяти,
            // но для HTML-страниц (обычно < 1 МБ) это приемлемо.
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 3. Проверяем HTTP-код ответа.
            // 2xx — успешные ответы, всё остальное — ошибки.
            int statusCode = response.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                return new DownloadResult.Failure(url,
                        "HTTP-ошибка: код ответа " + statusCode);
            }

            // 4. Проверяем Content-Type.
            // Краулер должен обрабатывать только HTML-страницы.
            // Скачивание PDF, изображений, видео — бессмысленная трата ресурсов.
            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("");

            if (!isHtmlContentType(contentType)) {
                return new DownloadResult.Failure(url,
                        "Неподдерживаемый Content-Type: " + contentType + " (ожидается text/html)");
            }

            // 5. Проверяем размер тела ответа.
            // Защита от утечки памяти при скачивании огромных файлов
            // (например, дамп базы данных по HTTP).
            String body = response.body();
            if (body != null && body.length() > config.maxBodySizeBytes()) {
                return new DownloadResult.Failure(url,
                        "Превышен лимит размера ответа: " + body.length()
                                + " байт (макс: " + config.maxBodySizeBytes() + ")");
            }

            // 6. Все проверки пройдены — возвращаем успешный результат.
            return new DownloadResult.Success(url, statusCode, contentType, body);

        } catch (IOException e) {
            // IOException — сетевая ошибка (DNS, таймаут, разрыв соединения).
            return new DownloadResult.Failure(url, "Сетевая ошибка: " + e.getMessage());
        } catch (InterruptedException e) {
            // InterruptedException — поток был прерван во время ожидания ответа.
            // Восстанавливаем флаг прерывания, чтобы вышестоящий код мог корректно завершиться.
            Thread.currentThread().interrupt();
            return new DownloadResult.Failure(url, "Загрузка прервана: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException — невалидный URI (некорректные символы, схема и т.п.).
            return new DownloadResult.Failure(url, "Невалидный URL: " + e.getMessage());
        }
    }

    /**
     * Определяет, является ли Content-Type HTML-документом.
     *
     * Проверяем вхождение "text/html" или "xhtml", потому что
     * серверы могут возвращать Content-Type в разных форматах:
     * - "text/html"
     * - "text/html; charset=utf-8"
     * - "application/xhtml+xml"
     *
     * @param contentType Значение заголовка Content-Type
     * @return true, если контент является HTML-документом
     */
    private boolean isHtmlContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            // Некоторые серверы не отправляют Content-Type — допускаем такие ответы,
            // так как большинство из них всё равно отдают HTML.
            return true;
        }
        String lower = contentType.toLowerCase();
        return lower.contains("text/html") || lower.contains("xhtml");
    }
}
