package io.github.ymatojava.crawler.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения Crawler Worker.
 * 
 * Точка входа для микросервиса, отвечающего за скачивание страниц.
 */
@SpringBootApplication
public class CrawlerWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerWorkerApplication.class, args);
    }
}
