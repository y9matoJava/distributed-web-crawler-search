package io.github.ymatojava.crawler.search.controller;

import io.github.ymatojava.crawler.search.document.PageDocument;
import io.github.ymatojava.crawler.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API контроллер для поисковой системы.
 * Предоставляет эндпоинты для веб-интерфейса или других сервисов.
 */
@RestController
@RequestMapping("/api/v1/search")
@CrossOrigin(origins = "*") // Разрешаем CORS для локального фронтенда
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Эндпоинт для поиска.
     *
     * @param q     Поисковый запрос
     * @param limit Ограничение на количество результатов (по умолчанию 10)
     * @return Список результатов
     */
    @GetMapping
    public ResponseEntity<List<PageDocument>> search(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        
        List<PageDocument> results = searchService.search(q, limit);
        return ResponseEntity.ok(results);
    }
}
