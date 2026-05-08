# Local Development

## What this file is for

This file explains how to run the project locally during development.

It is a practical guide for developers.

Architecture is described in:

```text
docs/architecture.md
```

Project roadmap is described in:

```text
docs/roadmap.md
```

Web crawler learning notes are described in:

```text
docs/web-crawler-notes.md
```

---

## Requirements

To work with this project locally, you need:

```text
Java 21
Maven
Git
Docker
Docker Compose
```

Recommended tools:

```text
IntelliJ IDEA
Postman or curl
DBeaver
```

---

## Clone the Repository

```bash
git clone <repository-url>
cd <project-directory>
```

---

## Build the Project

Use Maven:

```bash
mvn clean package
```

For a faster local check without packaging:

```bash
mvn clean test
```

---

## Run the Application

If the project has a Spring Boot entry point, run:

```bash
mvn spring-boot:run
```

Or run the main class from IntelliJ IDEA.

---

## Local Configuration

Local configuration should be stored in:

```text
src/main/resources/application-local.yml
```

Example:

```yaml
server:
  port: 8080

spring:
  application:
    name: web-crawler
```

The local profile can be enabled with:

```bash
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

---

## Environment Variables

Local environment variables can be stored in a `.env` file.

Example:

```env
SPRING_PROFILES_ACTIVE=local
APP_BASE_URL=http://localhost:8080
```

Do not commit secrets to Git.

Files with local secrets should be ignored by Git:

```text
.env
application-local-secret.yml
```

---

## Docker Compose

Later the project may use infrastructure services:

```text
PostgreSQL
RabbitMQ
Redis
Elasticsearch
```

These services should be started with Docker Compose.

Example command:

```bash
docker compose up -d
```

To stop services:

```bash
docker compose down
```

To see running containers:

```bash
docker compose ps
```

To see logs:

```bash
docker compose logs -f
```

---

## PostgreSQL

PostgreSQL will be used for persistent crawler state.

It will store:

```text
pages
canonical URLs
crawl status
metadata
timestamps
```

Example local connection values:

```text
Host: localhost
Port: 5432
Database: crawler
User: crawler
Password: crawler
```

Example JDBC URL:

```text
jdbc:postgresql://localhost:5432/crawler
```

---

## RabbitMQ

RabbitMQ will be used as a URL queue.

It will help crawler workers process URLs asynchronously.

Example local values:

```text
Host: localhost
Port: 5672
Management UI: http://localhost:15672
User: guest
Password: guest
```

---

## Redis

Redis will be used later for:

```text
rate limiting
temporary cache
host crawl delays
short-lived crawler state
```

Example local values:

```text
Host: localhost
Port: 6379
```

---

## Elasticsearch

Elasticsearch will be used later for full-text search.

It will allow searching by:

```text
title
headings
keywords
description
content
```

Example local value:

```text
http://localhost:9200
```

---

## First Development Version

The first version of the project should work without external infrastructure.

It should use in-memory data structures:

```text
Queue<String> urlsToVisit
Set<String> discoveredUrls
Map<String, PageData> pages
Map<String, Set<String>> keywordIndex
```

This makes development easier and allows testing the core crawler algorithm first.

---

## Recommended Development Order

The local development order should be:

```text
1. Run empty Spring Boot application
2. Add seed URL input
3. Add simple HTTP page downloader
4. Add HTML parsing
5. Add link extraction
6. Add URL normalization
7. Add duplicate protection
8. Add max depth
9. Add allowed hosts
10. Add text extraction
11. Add keyword extraction
12. Add simple in-memory search
13. Add tests
14. Add PostgreSQL
15. Add RabbitMQ
16. Add Elasticsearch
17. Add Redis
```

---

## Useful Maven Commands

Run tests:

```bash
mvn test
```

Build project:

```bash
mvn clean package
```

Run Spring Boot app:

```bash
mvn spring-boot:run
```

Skip tests during build:

```bash
mvn clean package -DskipTests
```

---

## Useful Git Commands

Check changes:

```bash
git status
```

Add one file:

```bash
git add docs/local-dev.md
```

Commit changes:

```bash
git commit -m "docs: add local development guide"
```

Push changes:

```bash
git push
```

---

## Local Development Rules

During local development:

* keep the first version simple;
* do not add distributed systems too early;
* make the in-memory crawler work first;
* write tests for core logic;
* keep configuration separate from code;
* do not commit secrets;
* document important commands.

---

## Summary

Local development should be simple and repeatable.

A developer should be able to:

* clone the repository;
* build the project;
* run tests;
* start the application;
* understand required tools;
* start local infrastructure when needed.

The main goal is to make development predictable and easy to reproduce.