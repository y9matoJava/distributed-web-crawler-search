package io.github.ymatojava.crawler.worker.config;

import io.github.ymatojava.crawler.core.download.CrawlerConfig;
import io.github.ymatojava.crawler.core.download.HttpPageDownloader;
import io.github.ymatojava.crawler.core.download.PageDownloader;
import io.github.ymatojava.crawler.core.parse.LinkExtractor;
import io.github.ymatojava.crawler.core.parse.PageParser;
import io.github.ymatojava.crawler.core.url.UrlCanonicalizer;
import io.github.ymatojava.crawler.core.url.UrlFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Spring для интеграции компонентов из модуля crawler-core.
 * 
 * Классы из crawler-core написаны как чистая Java (без Spring аннотаций @Service/@Component),
 * поэтому мы создаем их как Spring Beans (бины) вручную. Это правильный подход 
 * с точки зрения архитектуры: ядро ничего не знает о фреймворке.
 */
@Configuration
public class CoreBeansConfig {

    @Bean
    public UrlCanonicalizer urlCanonicalizer() {
        return new UrlCanonicalizer();
    }

    @Bean
    public UrlFilter urlFilter() {
        return new UrlFilter();
    }

    @Bean
    public LinkExtractor linkExtractor(UrlCanonicalizer canonicalizer, UrlFilter filter) {
        return new LinkExtractor(canonicalizer, filter);
    }

    @Bean
    public PageParser pageParser(LinkExtractor linkExtractor) {
        return new PageParser(linkExtractor);
    }

    @Bean
    public CrawlerConfig crawlerConfig() {
        // Используем базовые настройки. В реальном проекте они загружались бы из application.yml
        return CrawlerConfig.defaults();
    }

    @Bean
    public PageDownloader pageDownloader(CrawlerConfig crawlerConfig) {
        return new HttpPageDownloader(crawlerConfig);
    }
}
