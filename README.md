# Distributed Web Crawler Search

## English

Distributed Web Crawler Search is an educational backend project focused on building a web crawler step by step.

The project starts with a simple in-memory crawler and gradually evolves into a distributed search system using Java, Spring Boot, PostgreSQL, RabbitMQ, Redis, Elasticsearch, and Docker Compose.

The main goal of the project is to understand how a crawler works internally:

```text
URL -> HTML -> links -> queue -> dedup -> repeat
```

And then extend it with search:

```text
page text -> keywords -> index -> search results
```

---

## Project Goals

The project should demonstrate:

* URL crawling from seed URLs;
* HTML downloading and parsing;
* link extraction;
* URL normalization;
* duplicate URL protection;
* cycle protection;
* keyword extraction;
* page indexing;
* search by keywords and full text;
* persistent crawler state;
* distributed URL processing;
* responsible crawling with rate limits and robots.txt;
* clean documentation and tests.

---

## Technology Stack

Planned stack:

```text
Java 21
Maven
Spring Boot
PostgreSQL
RabbitMQ
Redis
Elasticsearch
Docker Compose
JUnit 5
Testcontainers
```

---

## Planned Architecture

The final system will include:

* `crawler-core` — core crawler logic without infrastructure;
* `crawler-worker` — background worker that processes URL jobs;
* `search-api` — HTTP API for search and crawler inspection;
* PostgreSQL — persistent state and URL deduplication;
* RabbitMQ — distributed URL queue;
* Redis — rate limiting and temporary cache;
* Elasticsearch — full-text search index.

---

## Documentation

Project documentation is stored in the `docs/` folder.

Start here:

```text
docs/README.md
```

Important documents:

```text
docs/architecture.md
docs/roadmap.md
docs/api.md
docs/local-dev.md
docs/web-crawler-notes.md
docs/responsible-crawling.md
docs/testing.md
```

---

## Development Approach

The project is developed step by step.

Recommended order:

```text
1. Documentation
2. Maven multi-module skeleton
3. URL canonicalization
4. HTML parsing
5. Link extraction
6. In-memory crawler
7. Keyword extraction
8. In-memory search
9. PostgreSQL persistence
10. RabbitMQ URL queue
11. Crawler worker pipeline
12. Search API
13. Elasticsearch full-text search
14. Redis rate limiting
15. robots.txt support
16. Multiple workers
17. Tests, CI, and documentation polish
```

---

## Current Status

The project is currently in the documentation and planning stage.

Implemented documentation:

```text
docs/architecture.md
docs/roadmap.md
docs/api.md
docs/local-dev.md
docs/web-crawler-notes.md
docs/responsible-crawling.md
docs/testing.md
```

Next major step:

```text
Create Maven multi-module skeleton
```

---

## Main Learning Rule

This project is written as a learning project.

The goal is not only to get a working crawler, but also to understand every layer:

* algorithms;
* data structures;
* persistence;
* queues;
* search;
* distributed workers;
* testing;
* system design.

---

# Русская версия

## О проекте

Distributed Web Crawler Search — это учебный backend-проект, в котором Web Crawler разрабатывается постепенно: от простой версии в памяти до распределённой системы поиска.

Проект начинается с базового алгоритма crawler:

```text
URL -> HTML -> links -> queue -> dedup -> repeat
```

Затем он расширяется до поисковой системы:

```text
текст страницы -> ключевые слова -> индекс -> результаты поиска
```

Главная цель проекта — не просто написать код, а понять, как устроен crawler изнутри.

---

## Цели проекта

Проект должен показать:

* обход страниц от стартового URL;
* скачивание и парсинг HTML;
* извлечение ссылок;
* нормализацию URL;
* защиту от дублей;
* защиту от циклических ссылок;
* извлечение ключевых слов;
* индексацию страниц;
* поиск по ключевым словам и full-text query;
* хранение состояния crawler;
* распределённую обработку URL;
* responsible crawling через rate limiting и robots.txt;
* документацию и тесты.

---

## Технологии

Планируемый стек:

```text
Java 21
Maven
Spring Boot
PostgreSQL
RabbitMQ
Redis
Elasticsearch
Docker Compose
JUnit 5
Testcontainers
```

---

## Планируемая архитектура

Финальная система будет состоять из:

* `crawler-core` — чистая логика crawler без инфраструктуры;
* `crawler-worker` — worker, который обрабатывает URL-задачи;
* `search-api` — HTTP API для поиска и просмотра результатов crawler;
* PostgreSQL — хранение состояния и защита от дублей;
* RabbitMQ — очередь URL-задач;
* Redis — rate limiting и временный cache;
* Elasticsearch — полнотекстовый поисковый индекс.

---

## Документация

Документация проекта находится в папке:

```text
docs/
```

Начинать лучше с файла:

```text
docs/README.md
```

Основные документы:

```text
docs/architecture.md
docs/roadmap.md
docs/api.md
docs/local-dev.md
docs/web-crawler-notes.md
docs/responsible-crawling.md
docs/testing.md
```

---

## Подход к разработке

Проект разрабатывается поэтапно.

Рекомендуемый порядок:

```text
1. Документация
2. Maven multi-module skeleton
3. URL canonicalization
4. HTML parsing
5. Link extraction
6. In-memory crawler
7. Keyword extraction
8. In-memory search
9. PostgreSQL persistence
10. RabbitMQ URL queue
11. Crawler worker pipeline
12. Search API
13. Elasticsearch full-text search
14. Redis rate limiting
15. robots.txt support
16. Multiple workers
17. Tests, CI, documentation polish
```

---

## Текущий статус

Сейчас проект находится на этапе документации и планирования.

Уже подготовлены документы:

```text
docs/architecture.md
docs/roadmap.md
docs/api.md
docs/local-dev.md
docs/web-crawler-notes.md
docs/responsible-crawling.md
docs/testing.md
```

Следующий крупный шаг:

```text
Создать Maven multi-module skeleton
```

---

## Главный учебный принцип

Этот проект делается как учебный проект.

Цель — не только получить рабочий crawler, но и понять каждый слой:

* алгоритмы;
* структуры данных;
* хранение состояния;
* очереди;
* поиск;
* распределённые workers;
* тестирование;
* system design.
