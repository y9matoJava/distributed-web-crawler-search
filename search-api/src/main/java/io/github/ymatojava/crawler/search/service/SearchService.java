package io.github.ymatojava.crawler.search.service;

import io.github.ymatojava.crawler.search.document.PageDocument;
import io.github.ymatojava.crawler.search.repository.PageDocumentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Бизнес-логика поисковой системы.
 */
@Service
public class SearchService {

    private final PageDocumentRepository repository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    public SearchService(PageDocumentRepository repository, ElasticsearchTemplate elasticsearchTemplate) {
        this.repository = repository;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    /**
     * Выполняет полнотекстовый поиск по Elasticsearch.
     * Использует NativeQuery для более тонкой настройки (например, приоритет title над body).
     *
     * @param query Запрос пользователя
     * @param limit Лимит результатов
     * @return Список найденных документов
     */
    public List<PageDocument> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        // Строим запрос: ищем фразу в полях title и bodyText,
        // причем совпадение в title весит в 2 раза больше (буст).
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields("title^2", "bodyText")
                                .query(query)
                        )
                )
                .withPageable(PageRequest.of(0, limit))
                .build();

        SearchHits<PageDocument> searchHits = elasticsearchTemplate.search(nativeQuery, PageDocument.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * Метод для добавления документа в индекс.
     * (В микросервисной архитектуре это может делаться через Kafka/RabbitMQ напрямую из crawler-worker).
     */
    public void indexDocument(PageDocument document) {
        repository.save(document);
    }
}
