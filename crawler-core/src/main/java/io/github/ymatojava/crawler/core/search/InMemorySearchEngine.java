package io.github.ymatojava.crawler.core.search;

import io.github.ymatojava.crawler.core.keyword.KeywordExtractor;
import io.github.ymatojava.crawler.core.keyword.TextTokenizer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация поискового движка, работающая целиком в оперативной памяти.
 * <p>
 * Предназначена для прототипирования и небольших объёмов данных.
 * Для промышленного использования следует заменить на реализацию
 * с использованием специализированных поисковых систем (Elasticsearch, Lucene).
 * <p>
 * Архитектура:
 * <ul>
 *     <li>{@link InvertedIndex} — хранит отображение «ключевое слово → документы».</li>
 *     <li>{@code pageStore} — хранит метаданные страниц (URL, заголовок, текст).</li>
 *     <li>{@link KeywordExtractor} — извлекает ключевые слова из текста страницы.</li>
 *     <li>{@link TextTokenizer} — токенизирует поисковый запрос.</li>
 * </ul>
 * Все структуры данных потокобезопасны (ConcurrentHashMap, AtomicLong).
 */
public class InMemorySearchEngine implements SearchEngine {

    /**
     * Максимальная длина сниппета в символах.
     * 200 символов — это примерно 1–2 предложения, достаточно для предпросмотра.
     */
    private static final int SNIPPET_MAX_LENGTH = 200;

    /** Инвертированный индекс для быстрого поиска по ключевым словам. */
    private final InvertedIndex invertedIndex;

    /**
     * Хранилище метаданных проиндексированных страниц.
     * Ключ — внутренний ID документа, значение — метаданные страницы.
     * ConcurrentHashMap обеспечивает потокобезопасный доступ при параллельной индексации.
     */
    private final Map<Long, PageData> pageStore = new ConcurrentHashMap<>();

    /**
     * Атомарный генератор уникальных ID документов.
     * AtomicLong гарантирует уникальность ID при параллельном вызове из нескольких потоков.
     */
    private final AtomicLong idGenerator = new AtomicLong(0);

    /** Компонент извлечения ключевых слов из текста страницы. */
    private final KeywordExtractor keywordExtractor;

    /** Токенизатор для разбиения поискового запроса на отдельные термины. */
    private final TextTokenizer tokenizer;

    /**
     * Конструктор по умолчанию.
     * Создаёт все внутренние зависимости самостоятельно.
     */
    public InMemorySearchEngine() {
        this.invertedIndex = new InvertedIndex();
        this.keywordExtractor = new KeywordExtractor();
        this.tokenizer = new TextTokenizer();
    }

    /**
     * Индексирует веб-страницу: извлекает ключевые слова и добавляет их в поисковый индекс.
     * <p>
     * Процесс:
     * <ol>
     *     <li>Генерация уникального ID для документа (AtomicLong.incrementAndGet).</li>
     *     <li>Извлечение ключевых слов из текста страницы через {@link KeywordExtractor}.</li>
     *     <li>Сохранение метаданных страницы в хранилище ({@code pageStore}).</li>
     *     <li>Добавление ключевых слов в инвертированный индекс.</li>
     * </ol>
     *
     * @param url      URL-адрес проиндексированной страницы
     * @param title    Заголовок страницы (содержимое тега &lt;title&gt;)
     * @param bodyText Полный текст тела страницы (без HTML-тегов)
     */
    public void indexPage(String url, String title, String bodyText) {
        // 1. Генерация уникального ID; incrementAndGet — атомарная операция
        long docId = idGenerator.incrementAndGet();

        // 2. Извлечение ключевых слов из текста страницы
        List<String> keywords = keywordExtractor.extract(bodyText);

        // 3. Сохранение метаданных страницы для последующего формирования результатов поиска
        pageStore.put(docId, new PageData(url, title, bodyText, keywords));

        // 4. Добавление документа в инвертированный индекс
        invertedIndex.addDocument(docId, keywords);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Алгоритм поиска:
     * <ol>
     *     <li>Токенизация запроса — разбиение на отдельные термины.</li>
     *     <li>Поиск документов, содержащих ВСЕ термины (AND-логика).</li>
     *     <li>Для каждого найденного документа вычисляется оценка релевантности:
     *         {@code (количество совпавших терминов / общее количество терминов запроса)}.</li>
     *     <li>Формирование сниппета — первые {@value #SNIPPET_MAX_LENGTH} символов текста.</li>
     *     <li>Сортировка по убыванию релевантности и ограничение по {@code limit}.</li>
     * </ol>
     */
    @Override
    public List<SearchResult> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        // 1. Токенизация поискового запроса
        List<String> queryTerms = tokenizer.tokenize(query);
        if (queryTerms.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Конъюнктивный поиск: находим документы, содержащие ВСЕ термины запроса
        Set<Long> matchingDocIds = invertedIndex.searchAll(queryTerms);

        if (matchingDocIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3–5. Для каждого найденного документа: вычисляем релевантность, формируем сниппет,
        // сортируем и ограничиваем количество результатов
        return matchingDocIds.stream()
                .map(docId -> {
                    PageData page = pageStore.get(docId);
                    if (page == null) {
                        return null;
                    }

                    // Оценка релевантности: доля терминов запроса, найденных среди ключевых слов страницы
                    double score = calculateRelevance(queryTerms, page.keywords());

                    // Сниппет: первые SNIPPET_MAX_LENGTH символов текста страницы
                    String snippet = createSnippet(page.bodyText());

                    return new SearchResult(page.url(), page.title(), snippet, page.keywords(), score);
                })
                .filter(result -> result != null)
                .sorted((a, b) -> Double.compare(b.relevanceScore(), a.relevanceScore()))
                .limit(limit)
                .toList();
    }

    /**
     * Вычисляет оценку релевантности документа для данного запроса.
     * <p>
     * Формула: {@code совпавшие_термины / всего_терминов_запроса}.
     * Это упрощённая метрика, дающая значение от 0.0 до 1.0.
     * При AND-логике поиска все документы будут иметь score = 1.0,
     * но метрика полезна при возможном переходе к OR-логике.
     *
     * @param queryTerms Термины поискового запроса
     * @param keywords   Ключевые слова документа
     * @return Оценка релевантности от 0.0 до 1.0
     */
    private double calculateRelevance(List<String> queryTerms, List<String> keywords) {
        if (queryTerms.isEmpty()) {
            return 0.0;
        }

        // Подсчёт количества терминов запроса, присутствующих в ключевых словах документа
        long matchCount = queryTerms.stream()
                .map(String::toLowerCase)
                .filter(term -> keywords.stream()
                        .map(String::toLowerCase)
                        .anyMatch(kw -> kw.equals(term)))
                .count();

        return (double) matchCount / queryTerms.size();
    }

    /**
     * Формирует сниппет (краткое описание) из текста страницы.
     * Берёт первые {@value #SNIPPET_MAX_LENGTH} символов текста.
     *
     * @param bodyText Полный текст страницы
     * @return Строка длиной не более {@value #SNIPPET_MAX_LENGTH} символов
     */
    private String createSnippet(String bodyText) {
        if (bodyText == null || bodyText.isEmpty()) {
            return "";
        }
        // Ограничиваем длину текста для компактного отображения в результатах поиска
        if (bodyText.length() <= SNIPPET_MAX_LENGTH) {
            return bodyText;
        }
        return bodyText.substring(0, SNIPPET_MAX_LENGTH);
    }

    /**
     * Внутренняя запись для хранения метаданных проиндексированной страницы.
     * Использование record обеспечивает неизменяемость данных.
     *
     * @param url      URL-адрес страницы
     * @param title    Заголовок страницы
     * @param bodyText Полный текст тела страницы
     * @param keywords Извлечённые ключевые слова
     */
    private record PageData(String url, String title, String bodyText, List<String> keywords) {
    }
}
