package io.github.ymatojava.crawler.search.service;

import io.github.ymatojava.crawler.search.document.PageDocument;
import io.github.ymatojava.crawler.search.repository.PageDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Фоновый планировщик, который синхронизирует сохраненные страницы из
 * реляционной БД PostgreSQL (которую пополняет crawler-worker)
 * в полнотекстовый индекс Elasticsearch (в котором ищет search-api).
 */
@Service
public class PostgresToElasticSyncService {

    private static final Logger log = LoggerFactory.getLogger(PostgresToElasticSyncService.class);

    private final JdbcTemplate jdbcTemplate;
    private final PageDocumentRepository elasticsearchRepository;
    
    // Храним ID последней обработанной записи в памяти.
    // При перезапуске сервиса мы выкачаем базу заново (Elasticsearch сделает UPSERT по ID).
    private long lastProcessedId = 0L;

    public PostgresToElasticSyncService(JdbcTemplate jdbcTemplate, PageDocumentRepository elasticsearchRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.elasticsearchRepository = elasticsearchRepository;
    }

    @Scheduled(fixedDelay = 5000)
    public void syncData() {
        String sql = "SELECT id, url, title, body_text, keywords FROM crawled_pages WHERE id > ? ORDER BY id ASC LIMIT 500";
        
        List<PageDocument> newPages = jdbcTemplate.query(sql, (rs, rowNum) -> {
            long id = rs.getLong("id");
            String url = rs.getString("url");
            String title = rs.getString("title");
            String bodyText = rs.getString("body_text");
            
            Array keywordsArray = rs.getArray("keywords");
            List<String> keywords = null;
            if (keywordsArray != null) {
                String[] strArray = (String[]) keywordsArray.getArray();
                keywords = Arrays.asList(strArray);
            }
            
            // В Elasticsearch ID хранится как String
            return new PageDocument(String.valueOf(id), url, title, bodyText, keywords);
        }, lastProcessedId);
        
        if (!newPages.isEmpty()) {
            elasticsearchRepository.saveAll(newPages);
            lastProcessedId = Long.parseLong(newPages.get(newPages.size() - 1).getId());
            log.info("Успешно синхронизировано {} новых страниц из PostgreSQL в Elasticsearch. Текущий максимальный ID: {}", newPages.size(), lastProcessedId);
        }
    }
}
