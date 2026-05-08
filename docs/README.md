# Project Documentation

## English

This folder contains documentation for the Distributed Web Crawler Search project.

The purpose of this documentation is to keep the project structured, understandable, and easy to develop step by step.

---

## Documents

### Architecture

File:

```text
architecture.md
```

Describes the planned system architecture:

* crawler core;
* crawler worker;
* search API;
* PostgreSQL;
* RabbitMQ;
* Redis;
* Elasticsearch;
* URL deduplication;
* content deduplication;
* responsible crawling;
* multiple workers.

Read this file when you need to understand how the final system should work.

---

### Roadmap

File:

```text
roadmap.md
```

Describes the development plan from simple to complex.

It explains the order of implementation:

* documentation;
* Maven skeleton;
* URL canonicalization;
* HTML parsing;
* in-memory crawler;
* keyword extraction;
* PostgreSQL;
* RabbitMQ;
* Elasticsearch;
* Redis;
* tests and CI.

Read this file when you need to understand what to build next.

---

### API Documentation

File:

```text
api.md
```

Describes the planned HTTP API.

It includes endpoints for:

* starting a crawl;
* checking crawl status;
* listing crawled pages;
* viewing page details;
* searching pages;
* cancelling a crawl;
* checking application health.

Read this file when you need to understand the API contract.

---

### Local Development

File:

```text
local-dev.md
```

Explains how to run the project locally.

It contains:

* required tools;
* Maven commands;
* Docker Compose commands;
* local configuration;
* environment variables;
* PostgreSQL notes;
* RabbitMQ notes;
* Redis notes;
* Elasticsearch notes.

Read this file when you need to start or test the project on your machine.

---

### Web Crawler Notes

File:

```text
web-crawler-notes.md
```

Contains simple learning notes about how a Web Crawler works.

It explains:

* seed URLs;
* HTML downloading;
* link extraction;
* queues;
* duplicate protection;
* cyclic links;
* keyword extraction;
* simple search.

Read this file when you need to review the core idea of a crawler.

---

### Responsible Crawling

File:

```text
responsible-crawling.md
```

Explains how the crawler should behave safely and predictably.

It covers:

* robots.txt;
* robots.txt cache;
* per-host rate limiting;
* Redis locks;
* max depth;
* allowed hosts;
* retry behavior;
* dead-letter queue;
* safe fetching rules.

Read this file when you need to understand how to avoid aggressive or unsafe crawling.

---

### Testing Guide

File:

```text
testing.md
```

Explains how the project should be tested.

It covers:

* unit tests;
* integration tests;
* end-to-end smoke tests;
* HTML fixtures;
* fake HTTP servers;
* Testcontainers;
* PostgreSQL tests;
* RabbitMQ tests;
* Redis tests;
* Elasticsearch tests;
* crawler worker pipeline tests.

Read this file when you need to understand how to verify project behavior.

---

## Recommended Reading Order

If you are new to the project, read the documents in this order:

```text
1. web-crawler-notes.md
2. roadmap.md
3. architecture.md
4. local-dev.md
5. api.md
6. responsible-crawling.md
7. testing.md
```

---

## Main Project Idea

The core crawler flow is:

```text
URL -> HTML -> links -> queue -> dedup -> repeat
```

The search flow is:

```text
page text -> keywords -> index -> search results
```

The project should grow from a simple in-memory crawler to a distributed search system.

---

# Документация проекта

## Русская версия

Эта папка содержит документацию проекта Distributed Web Crawler Search.

Задача документации — сохранить проект понятным, структурированным и удобным для поэтапной разработки.

---

## Документы

### Архитектура

Файл:

```text
architecture.md
```

Описывает планируемую архитектуру системы:

* crawler core;
* crawler worker;
* search API;
* PostgreSQL;
* RabbitMQ;
* Redis;
* Elasticsearch;
* deduplication URL;
* deduplication контента;
* responsible crawling;
* multiple workers.

Этот файл нужно читать, когда нужно понять, как должна работать финальная система.

---

### Roadmap

Файл:

```text
roadmap.md
```

Описывает план разработки проекта от простого к сложному.

В нём указан порядок реализации:

* документация;
* Maven skeleton;
* URL canonicalization;
* HTML parsing;
* in-memory crawler;
* keyword extraction;
* PostgreSQL;
* RabbitMQ;
* Elasticsearch;
* Redis;
* tests and CI.

Этот файл нужно читать, когда нужно понять, что делать дальше.

---

### API Documentation

Файл:

```text
api.md
```

Описывает планируемый HTTP API.

В нём есть endpoints для:

* запуска crawling;
* проверки статуса crawling;
* просмотра найденных страниц;
* просмотра деталей страницы;
* поиска страниц;
* отмены crawling;
* проверки состояния приложения.

Этот файл нужно читать, когда нужно понять API contract проекта.

---

### Local Development

Файл:

```text
local-dev.md
```

Объясняет, как запускать проект локально.

В нём есть:

* необходимые инструменты;
* Maven-команды;
* Docker Compose-команды;
* локальная конфигурация;
* environment variables;
* заметки по PostgreSQL;
* заметки по RabbitMQ;
* заметки по Redis;
* заметки по Elasticsearch.

Этот файл нужно читать, когда нужно запустить или проверить проект на своей машине.

---

### Web Crawler Notes

Файл:

```text
web-crawler-notes.md
```

Содержит учебные заметки о том, как работает Web Crawler.

В нём объясняются:

* seed URLs;
* скачивание HTML;
* извлечение ссылок;
* очередь URL;
* защита от дублей;
* циклические ссылки;
* извлечение ключевых слов;
* простой поиск.

Этот файл нужно читать, когда нужно повторить основную идею crawler.

---

### Responsible Crawling

Файл:

```text
responsible-crawling.md
```

Объясняет, как crawler должен вести себя безопасно и предсказуемо.

В нём описаны:

* robots.txt;
* cache для robots.txt;
* per-host rate limiting;
* Redis locks;
* max depth;
* allowed hosts;
* retry behavior;
* dead-letter queue;
* safe fetching rules.

Этот файл нужно читать, когда нужно понять, как избежать агрессивного или небезопасного crawling.

---

### Testing Guide

Файл:

```text
testing.md
```

Объясняет, как тестировать проект.

В нём описаны:

* unit tests;
* integration tests;
* end-to-end smoke tests;
* HTML fixtures;
* fake HTTP servers;
* Testcontainers;
* PostgreSQL tests;
* RabbitMQ tests;
* Redis tests;
* Elasticsearch tests;
* crawler worker pipeline tests.

Этот файл нужно читать, когда нужно понять, как проверять поведение проекта.

---

## Рекомендуемый порядок чтения

Если ты впервые открываешь проект, читай документы в таком порядке:

```text
1. web-crawler-notes.md
2. roadmap.md
3. architecture.md
4. local-dev.md
5. api.md
6. responsible-crawling.md
7. testing.md
```

---

## Главная идея проекта

Основной flow crawler:

```text
URL -> HTML -> links -> queue -> dedup -> repeat
```

Flow поиска:

```text
текст страницы -> ключевые слова -> индекс -> результаты поиска
```

Проект должен постепенно вырасти от простой in-memory версии crawler до распределённой поисковой системы.

---

## Summary

This documentation folder is the main knowledge base for the project.

It should help answer three questions:

```text
1. What are we building?
2. Why are we building it this way?
3. What should be implemented next?
```

---

## Итог

Эта папка с документацией — основная база знаний проекта.

Она должна помогать отвечать на три вопроса:

```text
1. Что мы строим?
2. Почему мы строим это именно так?
3. Что нужно реализовать дальше?
```