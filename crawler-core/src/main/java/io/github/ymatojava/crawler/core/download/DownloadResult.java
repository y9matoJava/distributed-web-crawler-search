package io.github.ymatojava.crawler.core.download;

/**
 * Герметичный (sealed) интерфейс, описывающий результат загрузки веб-страницы.
 *
 * Sealed interface — это конструкция Java 17+, ограничивающая набор возможных реализаций.
 * Компилятор гарантирует, что все варианты результата обработаны в switch/instanceof,
 * что делает код надёжнее и предотвращает забытые ветки обработки ошибок.
 *
 * Два возможных исхода загрузки:
 * <ul>
 *   <li>{@link Success} — страница успешно скачана, содержит тело ответа</li>
 *   <li>{@link Failure} — скачивание не удалось по какой-либо причине</li>
 * </ul>
 *
 * Пример использования:
 * <pre>
 *   DownloadResult result = downloader.download("https://example.com");
 *   switch (result) {
 *       case DownloadResult.Success s -> processPage(s.body());
 *       case DownloadResult.Failure f -> logError(f.reason());
 *   }
 * </pre>
 */
public sealed interface DownloadResult permits DownloadResult.Success, DownloadResult.Failure {

    /**
     * Успешный результат загрузки страницы.
     *
     * @param url         URL, который был загружен (может отличаться от исходного после редиректов)
     * @param statusCode  HTTP-код ответа (200, 301, 304 и т.д.)
     * @param contentType Тип контента из заголовка Content-Type (например, "text/html; charset=utf-8")
     * @param body        Тело HTTP-ответа (HTML-код страницы)
     */
    record Success(String url, int statusCode, String contentType, String body) implements DownloadResult {
    }

    /**
     * Неуспешный результат загрузки страницы.
     *
     * Причины неудачи могут включать:
     * - сетевой таймаут;
     * - HTTP-ошибку (4xx, 5xx);
     * - неподдерживаемый Content-Type (не text/html);
     * - превышение максимального размера ответа;
     * - исключение при сетевом вызове.
     *
     * @param url    URL, загрузка которого не удалась
     * @param reason Человеко-читаемое описание причины неудачи
     */
    record Failure(String url, String reason) implements DownloadResult {
    }
}
