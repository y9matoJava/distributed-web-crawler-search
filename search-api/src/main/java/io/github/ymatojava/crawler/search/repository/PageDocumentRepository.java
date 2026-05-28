package io.github.ymatojava.crawler.search.repository;

import io.github.ymatojava.crawler.search.document.PageDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий Elasticsearch для выполнения поисковых запросов.
 */
@Repository
public interface PageDocumentRepository extends ElasticsearchRepository<PageDocument, String> {

    /**
     * Ищет документы, в которых title или bodyText содержат переданный запрос.
     * Это базовый метод Spring Data, генерируемый по имени.
     *
     * @param title запрос для заголовка
     * @param bodyText запрос для текста
     * @return список найденных документов
     */
    List<PageDocument> findByTitleContainingOrBodyTextContaining(String title, String bodyText);
}
