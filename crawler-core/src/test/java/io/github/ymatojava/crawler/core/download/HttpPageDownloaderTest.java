package io.github.ymatojava.crawler.core.download;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для HttpPageDownloader.
 * Используют MockWebServer от OkHttp для эмуляции HTTP-сервера
 * без необходимости обращения к реальному интернету.
 */
class HttpPageDownloaderTest {

    private MockWebServer mockWebServer;
    private HttpPageDownloader downloader;
    private CrawlerConfig config;

    @BeforeEach
    void setUp() throws IOException {
        // Запускаем локальный веб-сервер перед каждым тестом
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Используем конфигурацию по умолчанию, но с коротким таймаутом для тестов
        config = new CrawlerConfig(
                Duration.ofSeconds(2),
                "TestBot",
                1024 * 1024, // 1MB
                5,
                Duration.ZERO,
                10,
                3
        );
        downloader = new HttpPageDownloader(config);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Останавливаем сервер после каждого теста
        mockWebServer.shutdown();
    }

    @Test
    void shouldDownloadHtmlSuccessfully() {
        // Подготавливаем успешный ответ
        String htmlBody = "<html><body>Hello World</body></html>";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "text/html; charset=utf-8")
                .setBody(htmlBody));

        // Выполняем запрос к mock-серверу
        String url = mockWebServer.url("/test").toString();
        DownloadResult result = downloader.download(url);

        // Проверяем, что результат — Success и содержит правильные данные
        assertInstanceOf(DownloadResult.Success.class, result);
        DownloadResult.Success success = (DownloadResult.Success) result;

        assertEquals(200, success.statusCode());
        assertEquals("text/html; charset=utf-8", success.contentType());
        assertEquals(htmlBody, success.body());
        assertEquals(url, success.url());
    }

    @Test
    void shouldReturnFailureOn404() {
        // Подготавливаем ответ с ошибкой
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        String url = mockWebServer.url("/not-found").toString();
        DownloadResult result = downloader.download(url);

        // Проверяем, что результат — Failure
        assertInstanceOf(DownloadResult.Failure.class, result);
        DownloadResult.Failure failure = (DownloadResult.Failure) result;
        assertTrue(failure.reason().contains("HTTP-ошибка: код ответа 404"), 
                "Должна быть указана правильная причина ошибки");
    }

    @Test
    void shouldReturnFailureForNonHtmlContent() {
        // Подготавливаем ответ с PDF (не HTML)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/pdf")
                .setBody("Fake PDF Content"));

        String url = mockWebServer.url("/doc.pdf").toString();
        DownloadResult result = downloader.download(url);

        assertInstanceOf(DownloadResult.Failure.class, result);
        DownloadResult.Failure failure = (DownloadResult.Failure) result;
        assertTrue(failure.reason().contains("Неподдерживаемый Content-Type"), 
                "Не-HTML контент должен быть отклонен");
    }

    @Test
    void shouldReturnFailureOnTimeout() {
        // Подготавливаем ответ с задержкой, превышающей таймаут (2 сек)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "text/html")
                .setBody("Too late")
                .setHeadersDelay(3, java.util.concurrent.TimeUnit.SECONDS));

        String url = mockWebServer.url("/slow").toString();
        DownloadResult result = downloader.download(url);

        assertInstanceOf(DownloadResult.Failure.class, result);
        DownloadResult.Failure failure = (DownloadResult.Failure) result;
        assertTrue(failure.reason().contains("Сетевая ошибка"), 
                "Таймаут должен обрабатываться как сетевая ошибка");
    }

    @Test
    void shouldReturnFailureOnBodySizeLimitExceeded() {
        // Устанавливаем очень маленький лимит на размер тела (10 байт)
        CrawlerConfig strictConfig = new CrawlerConfig(
                Duration.ofSeconds(2), "TestBot", 10, 5, Duration.ZERO, 10, 3
        );
        HttpPageDownloader strictDownloader = new HttpPageDownloader(strictConfig);

        // Подготавливаем ответ, который больше 10 байт
        String largeBody = "<html><body>This is too large for the 10 bytes limit</body></html>";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "text/html")
                .setBody(largeBody));

        String url = mockWebServer.url("/large").toString();
        DownloadResult result = strictDownloader.download(url);

        assertInstanceOf(DownloadResult.Failure.class, result);
        DownloadResult.Failure failure = (DownloadResult.Failure) result;
        assertTrue(failure.reason().contains("Превышен лимит размера ответа"), 
                "Ответ, превышающий лимит, должен быть отклонен");
    }
}
