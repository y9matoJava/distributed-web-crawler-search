package io.github.ymatojava.crawler.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Точка входа для микросервиса Search API.
 */
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "io.github.ymatojava.crawler.search.repository")
@EnableScheduling
public class SearchApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchApiApplication.class, args);
    }
}
