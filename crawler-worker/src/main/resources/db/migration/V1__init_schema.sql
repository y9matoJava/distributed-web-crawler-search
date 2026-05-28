-- V1__init_schema.sql
-- Инициализация схемы базы данных для краулера

-- 1. Таблица для хранения всех обнаруженных URL-адресов
-- Используется для предотвращения повторного скачивания (URL Frontier + Deduplication)
CREATE TABLE crawl_urls (
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    url_hash VARCHAR(64) NOT NULL UNIQUE, -- SHA-256 хеш для быстрого поиска и уникальности
    status VARCHAR(20) NOT NULL,          -- DISCOVERED, CRAWLED, FAILED, SKIPPED
    discovered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    crawled_at TIMESTAMP WITH TIME ZONE
);

-- Индекс по статусу для быстрого поиска необработанных URL (DISCOVERED)
CREATE INDEX idx_crawl_urls_status ON crawl_urls(status);


-- 2. Таблица для хранения распарсенных страниц
-- Содержит текстовые данные и ключевые слова для последующего экспорта в Elasticsearch
CREATE TABLE crawl_pages (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL REFERENCES crawl_urls(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    title TEXT,
    body_text TEXT,
    crawled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Уникальный индекс, гарантирующий, что одна страница скачана только один раз
CREATE UNIQUE INDEX idx_crawl_pages_url_id ON crawl_pages(url_id);


-- 3. Таблица для хранения извлеченных ключевых слов (связь многие-ко-многим)
CREATE TABLE page_keywords (
    page_id BIGINT NOT NULL REFERENCES crawl_pages(id) ON DELETE CASCADE,
    keyword VARCHAR(255) NOT NULL,
    PRIMARY KEY (page_id, keyword)
);

-- Индекс для поиска страниц по ключевому слову (In-Database Search)
CREATE INDEX idx_page_keywords_keyword ON page_keywords(keyword);
