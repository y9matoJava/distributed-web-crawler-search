# Architecture

## Project Goal

`distributed-web-crawler-search` is a learning backend project that implements a web crawler and a mini search engine.

The crawler starts from one or more seed URLs, downloads HTML pages, extracts links, recursively discovers new pages, avoids duplicate and cyclic crawling, extracts text and keywords, stores crawl state, and provides fast search over indexed pages.

The project is designed as a backend/system-design learning project, not as an aggressive internet scraper.

## Main Features

Final version should support:

- recursive web crawling from seed URLs;
- HTML page fetching;
- HTML parsing;
- link extraction;
- URL normalization;
- URL deduplication;
- cycle protection;
- content deduplication;
- keyword extraction;
- persistent crawl state in PostgreSQL;
- distributed URL queue with RabbitMQ;
- per-host rate limiting with Redis;
- robots.txt support;
- full-text search with Elasticsearch;
- Search API;
- multiple crawler workers;
- retry and dead-letter queue;
- Docker Compose local environment;
- unit and integration tests.

## High-Level Architecture

```text
Seed URLs
   |
   v
URL Discovery / Deduplication
   |
   v
PostgreSQL crawl_url table
   |
   v
RabbitMQ URL Queue
   |
   v
Crawler Worker
   |
   +--> Robots.txt Checker
   +--> Redis Host Rate Limiter
   +--> HTML Fetcher
   +--> HTML Parser
   +--> Link Extractor
   +--> Text Extractor
   +--> Keyword Extractor
   +--> Content Deduplicator
   +--> Document Indexer
   |
   +--> New discovered URLs -> PostgreSQL -> RabbitMQ

Elasticsearch Document Index
   |
   v
Search API