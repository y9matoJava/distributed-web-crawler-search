package io.github.ymatojava.crawler.core.engine;

import io.github.ymatojava.crawler.core.download.DownloadResult;
import io.github.ymatojava.crawler.core.download.PageDownloader;
import io.github.ymatojava.crawler.core.parse.PageParser;
import io.github.ymatojava.crawler.core.parse.ParsedPage;
import io.github.ymatojava.crawler.core.url.NormalizedUrl;
import io.github.ymatojava.crawler.core.url.UrlCanonicalizer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Основной движок BFS-обхода (Breadth-First Search) веб-страниц.
 *
 * <p>Реализует классический алгоритм поуровневого обхода графа ссылок:
 * <ol>
 *     <li>Каноникализация и добавление начального URL (seed) в фронтир</li>
 *     <li>Пока фронтир не пуст и лимит страниц не достигнут:
 *         <ul>
 *             <li>Извлечение URL из головы очереди</li>
 *             <li>Загрузка HTML через {@link PageDownloader}</li>
 *             <li>Парсинг страницы через {@link PageParser}</li>
 *             <li>Добавление обнаруженных ссылок в фронтир</li>
 *         </ul>
 *     </li>
 *     <li>Формирование и возврат {@link CrawlResult}</li>
 * </ol>
 *
 * <p>BFS выбран вместо DFS, потому что он обходит страницы «вширь»,
 * начиная с ближайших к seed. Это даёт более релевантные результаты
 * и предотвращает «проваливание» вглубь одной ветки сайта.</p>
 *
 * <p><b>Потокобезопасность:</b> не является потокобезопасным. Однопоточная версия.</p>
 */
public class CrawlEngine {

    private static final Logger LOG = Logger.getLogger(CrawlEngine.class.getName());

    /**
     * Компонент для загрузки HTML-страниц по URL.
     */
    private final PageDownloader downloader;

    /**
     * Компонент для парсинга HTML: извлечение текста, заголовка и ссылок.
     */
    private final PageParser parser;

    /**
     * Фронтир: FIFO-очередь + дедупликация для управления порядком обхода.
     */
    private final UrlFrontier frontier;

    /**
     * Максимальное количество страниц, которые краулер загрузит за одну сессию.
     * Ограничение необходимо для контроля ресурсов (память, сеть, время).
     */
    private final int maxPages;

    /**
     * Максимальная глубина обхода (зарезервировано для будущего использования).
     * В текущей реализации не используется, так как BFS-глубина не отслеживается.
     */
    private final int maxDepth;

    /**
     * Каноникализатор URL для приведения seed URL к единому формату.
     */
    private final UrlCanonicalizer canonicalizer;

    /**
     * Создаёт экземпляр BFS-движка краулера.
     *
     * @param downloader загрузчик страниц
     * @param parser     парсер HTML
     * @param frontier   фронтир URL (очередь + дедупликация)
     * @param maxPages   максимальное число страниц для загрузки
     * @param maxDepth   максимальная глубина обхода (зарезервировано)
     */
    public CrawlEngine(PageDownloader downloader, PageParser parser,
                       UrlFrontier frontier, int maxPages, int maxDepth) {
        this.downloader = downloader;
        this.parser = parser;
        this.frontier = frontier;
        this.maxPages = maxPages;
        this.maxDepth = maxDepth;
        this.canonicalizer = new UrlCanonicalizer();
    }

    /**
     * Запускает BFS-обход, начиная с указанного URL.
     *
     * <p>Алгоритм:
     * <ol>
     *     <li>Каноникализация seedUrl и добавление в фронтир</li>
     *     <li>Цикл обхода: пока есть URL в очереди и лимит не достигнут</li>
     *     <li>Для каждого URL:
     *         <ul>
     *             <li>Загрузка страницы</li>
     *             <li>При успехе: парсинг и добавление обнаруженных ссылок в фронтир</li>
     *             <li>При ошибке: логирование и переход к следующему URL</li>
     *         </ul>
     *     </li>
     *     <li>Формирование CrawlResult с накопленными данными</li>
     * </ol>
     *
     * @param seedUrl начальный URL обхода
     * @return результат обхода, содержащий все успешно загруженные страницы
     */
    public CrawlResult crawl(String seedUrl) {
        Instant startTime = Instant.now();
        List<ParsedPage> pages = new ArrayList<>();

        // 1. Каноникализация начального URL.
        // Seed URL может прийти от пользователя в произвольном формате
        // (с лишними пробелами, без схемы и т.д.), поэтому его нужно нормализовать.
        Optional<NormalizedUrl> normalizedSeed = canonicalizer.canonicalize(seedUrl, null);
        if (normalizedSeed.isEmpty()) {
            LOG.warning("Seed URL не прошёл каноникализацию: " + seedUrl);
            return new CrawlResult(pages, frontier.totalDiscovered(),
                    Duration.between(startTime, Instant.now()));
        }

        // Добавляем нормализованный seed в фронтир
        frontier.add(normalizedSeed.get().value());

        // 2. Основной цикл BFS-обхода.
        // Условие выхода: очередь пуста ИЛИ достигнут лимит страниц.
        while (!frontier.isEmpty() && pages.size() < maxPages) {
            // Извлекаем следующий URL из головы FIFO-очереди
            Optional<String> nextUrl = frontier.poll();

            // Теоретически poll() не должен вернуть empty, если isEmpty() == false,
            // но проверяем на всякий случай для защиты от гонок в будущем
            if (nextUrl.isEmpty()) {
                break;
            }

            String currentUrl = nextUrl.get();
            LOG.info("Обход URL: " + currentUrl);

            // 3. Загрузка HTML через PageDownloader
            DownloadResult downloadResult = downloader.download(currentUrl);

            // 4. Обработка результата загрузки через pattern matching (Java 21)
            switch (downloadResult) {
                case DownloadResult.Success success -> {
                    // Парсинг успешно загруженной страницы
                    ParsedPage page = parser.parse(success.url(), success.body());
                    pages.add(page);

                    // Добавляем обнаруженные ссылки в фронтир.
                    // Фронтир сам отфильтрует уже виденные URL.
                    frontier.addAll(page.outLinks());

                    LOG.fine("Извлечено " + page.outLinks().size()
                            + " ссылок с " + currentUrl);
                }

                case DownloadResult.Failure failure -> {
                    // Ошибка загрузки не должна прерывать обход —
                    // просто логируем и переходим к следующему URL
                    LOG.log(Level.WARNING,
                            "Ошибка загрузки URL {0}: {1}",
                            new Object[]{failure.url(), failure.reason()});
                }
            }
        }

        // 5. Формирование итогового результата
        Duration elapsed = Duration.between(startTime, Instant.now());
        LOG.info("Обход завершён: загружено " + pages.size()
                + " страниц, обнаружено " + frontier.totalDiscovered()
                + " URL за " + elapsed.toMillis() + " мс");

        return new CrawlResult(pages, frontier.totalDiscovered(), elapsed);
    }
}
