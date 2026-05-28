package io.github.ymatojava.crawler.core.search;

import io.github.ymatojava.crawler.common.model.CrawlPage;
import io.github.ymatojava.crawler.core.keyword.KeywordExtractor;
import io.github.ymatojava.crawler.core.keyword.TextTokenizer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Локальная in-memory реализация поискового движка.
 *
 * Совмещает в себе хранилище страниц (Document Store) и инвертированный индекс.
 */
public class InMemorySearchEngine implements SearchEngine {

    private final InvertedIndex index = new InvertedIndex();
    // Хранилище: docId -> CrawlPage
    private final Map<Long, CrawlPage> documentStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    private final KeywordExtractor keywordExtractor = new KeywordExtractor();
    private final TextTokenizer tokenizer = new TextTokenizer();

    /**
     * Индексирует новую скачанную страницу.
     *
     * 1. Извлекает ключевые слова.
     * 2. Сохраняет страницу в Document Store.
     * 3. Добавляет в инвертированный индекс.
     *
     * @param url      URL страницы
     * @param title    Заголовок страницы
     * @param bodyText Текст страницы
     */
    public void indexPage(String url, String title, String bodyText) {
        if (url == null || bodyText == null) return;

        // Извлекаем ключевые слова для индексации
        List<String> keywords = keywordExtractor.extract(bodyText);

        long docId = idGenerator.getAndIncrement();
        CrawlPage page = new CrawlPage(docId, url, title, bodyText, keywords, Instant.now());

        documentStore.put(docId, page);
        index.addDocument(docId, keywords);
    }

    @Override
    public List<SearchResult> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        // 1. Токенизируем запрос пользователя
        List<String> queryTerms = tokenizer.tokenize(query);
        if (queryTerms.isEmpty()) {
            return List.of();
        }

        // 2. Ищем документы, содержащие ВСЕ слова из запроса (AND)
        Set<Long> matchedDocIds = index.searchAll(queryTerms);

        // 3. Вычисляем релевантность и формируем результат
        List<SearchResult> results = new ArrayList<>();
        for (Long docId : matchedDocIds) {
            CrawlPage page = documentStore.get(docId);
            if (page != null) {
                // В in-memory версии используем базовую релевантность:
                // Доля слов из запроса, которые попали в ТОП ключевых слов страницы
                double score = calculateScore(queryTerms, page.keywords());
                
                String snippet = createSnippet(page.bodyText());
                results.add(new SearchResult(page.url(), page.title(), snippet, page.keywords(), score));
            }
        }

        // 4. Сортируем по убыванию релевантности и обрезаем по лимиту
        return results.stream()
                .sorted(Comparator.comparingDouble(SearchResult::relevanceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Базовый алгоритм оценки релевантности.
     */
    private double calculateScore(List<String> queryTerms, List<String> pageKeywords) {
        if (queryTerms.isEmpty() || pageKeywords.isEmpty()) return 0.0;
        
        long matches = queryTerms.stream()
                .filter(pageKeywords::contains)
                .count();
                
        return (double) matches / queryTerms.size();
    }

    /**
     * Создает короткий сниппет текста (до 200 символов) для вывода в результатах поиска.
     */
    private String createSnippet(String bodyText) {
        if (bodyText == null) return "";
        if (bodyText.length() <= 200) return bodyText;
        return bodyText.substring(0, 197) + "...";
    }
}
