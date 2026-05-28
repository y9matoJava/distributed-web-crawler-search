package io.github.ymatojava.crawler.core.download;

/**
 * Интерфейс загрузчика веб-страниц.
 *
 * Абстрагирует механизм загрузки HTML от остальной логики краулера.
 * Это позволяет:
 * - легко подменять реализацию в тестах (mock-объект вместо реального HTTP);
 * - изолировать сетевой слой от бизнес-логики (принцип Dependency Inversion);
 * - поддерживать разные стратегии загрузки (HttpClient, OkHttp, Selenium для JS-рендеринга).
 *
 * Основная реализация: {@link HttpPageDownloader}.
 */
public interface PageDownloader {

    /**
     * Загружает содержимое веб-страницы по указанному URL.
     *
     * Контракт реализации:
     * - метод НЕ бросает исключений — все ошибки оборачиваются в {@link DownloadResult.Failure};
     * - должен устанавливать User-Agent заголовок;
     * - должен соблюдать таймаут соединения;
     * - должен проверять Content-Type (только text/html).
     *
     * @param url Абсолютный URL для загрузки (должен начинаться с http:// или https://)
     * @return {@link DownloadResult.Success} с телом ответа, либо {@link DownloadResult.Failure} с причиной
     */
    DownloadResult download(String url);
}
