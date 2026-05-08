
# Roadmap

## What this file is for

This file describes the step-by-step development roadmap for the `distributed-web-crawler-search` project.

The goal is to build a Web Crawler and mini search engine gradually: from a simple in-memory crawler to a distributed crawler with PostgreSQL, RabbitMQ, Redis, Elasticsearch, robots.txt support, multiple workers, tests, CI, and documentation.

## What should be added to this file

Add here:

- project development versions;
- goals for each version;
- tasks for each stage;
- files and modules that should be created;
- commands for checking progress;
- Definition of Done for each stage;
- recommended branch names;
- recommended commit messages;
- notes about what should not be done too early.

## What should NOT be added to this file

Do not add here:

- full architecture explanation;
- full database schema details;
- all AI prompts;
- local setup details;
- long theory notes;
- source code implementation.

Use other files for those topics:

- `docs/architecture.md` — architecture and system design;
- `docs/web-crawler-notes.md` — learning notes and theory;
- `docs/local-dev.md` — local setup commands;
- `docs/responsible-crawling.md` — safe crawling rules;
- `docs/testing.md` — testing strategy;
- `docs/git-workflow.md` — Git and GitHub workflow;
- `docs/ai-prompts.md` — prompts for AI assistants.

---

# Project Goal

The goal of this project is to build a distributed Web Crawler and mini search engine.

The final project should be able to:

- start crawling from seed URLs;
- download HTML pages;
- parse HTML;
- extract links from pages;
- recursively discover new pages;
- avoid duplicate URLs;
- avoid infinite crawling caused by cyclic links;
- extract text and keywords from pages;
- associate keywords with pages;
- store crawl state in PostgreSQL;
- use RabbitMQ as a distributed URL queue;
- use Redis for rate limiting and caching;
- respect robots.txt;
- index documents in Elasticsearch;
- provide Search API;
- support multiple crawler workers;
- handle retry and dead-letter queue;
- include unit and integration tests;
- run locally with Docker Compose;
- be documented well enough for portfolio/demo.

---

# Main Development Principle

The final goal is complex, but the project must be built in small steps.

Do not start with RabbitMQ, Elasticsearch, Redis, and multiple workers immediately.

Correct order:

```text
1. Understand crawler algorithm.
2. Implement URL normalization.
3. Implement HTML parsing and link extraction.
4. Implement in-memory crawler.
5. Add keyword extraction.
6. Add simple in-memory search.
7. Add PostgreSQL persistent state.
8. Add RabbitMQ queue.
9. Add crawler worker pipeline.
10. Add Elasticsearch search.
11. Add Redis rate limiting and robots.txt.
12. Add multiple workers.
13. Add tests, CI, documentation, and demo.