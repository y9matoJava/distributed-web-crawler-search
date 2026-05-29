# 📚 Документация проекта

## Distributed Web Crawler Search

Эта папка содержит подробную техническую документацию проекта распределённой поисковой системы.

---

## 📋 Оглавление

| # | Документ | Описание |
|---|---------|----------|
| 1 | [architecture.md](architecture.md) | Архитектура системы, модули и их взаимосвязи |
| 2 | [roadmap.md](roadmap.md) | Дорожная карта: от идеи до production |
| 3 | [api.md](api.md) | REST API спецификация поискового сервиса |
| 4 | [local-dev.md](local-dev.md) | Настройка локального окружения разработчика |
| 5 | [web-crawler-notes.md](web-crawler-notes.md) | Теоретические заметки по алгоритмам веб-краулинга |
| 6 | [responsible-crawling.md](responsible-crawling.md) | Ответственный краулинг: robots.txt, rate limiting, вежливость |
| 7 | [testing.md](testing.md) | Стратегия тестирования и покрытие |

---

## 🏗️ Архитектура (краткий обзор)

Система состоит из **4 Maven-модулей** и **4 инфраструктурных сервисов**:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Приложения                                │
│                                                                  │
│  ┌──────────────────┐              ┌──────────────────┐          │
│  │  crawler-worker   │              │   search-api      │          │
│  │  (Spring Boot)    │              │   (Spring Boot)    │          │
│  │                   │              │   порт: 8081       │          │
│  │  • WorkerPipeline │              │  • SearchService   │          │
│  │  • RabbitConsumer │              │  • PG→ES Sync      │          │
│  │  • Politeness     │              │  • Веб-интерфейс   │          │
│  └────────┬──────────┘              └────────┬──────────┘          │
│           │                                  │                     │
│  ┌────────┴──────────┐              ┌────────┴──────────┐          │
│  │  crawler-core      │              │   common            │          │
│  │  (чистая Java)     │              │   (модели/DTO)      │          │
│  │  • Downloader      │              │   • CrawlPage       │          │
│  │  • Parser (Jsoup)  │              │   • UrlHash          │          │
│  │  • LinkExtractor   │              │   • UrlStatus        │          │
│  │  • Keywords        │              │                      │          │
│  └────────────────────┘              └──────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      Инфраструктура                              │
│                                                                  │
│  🐘 PostgreSQL 15    │  🐰 RabbitMQ 3.12  │  🔍 Elasticsearch 8.11  │  🔴 Redis 7.2  │
│  Хранение данных     │  Очередь URL       │  Поисковый индекс       │  Rate limiting  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Поток данных

1. **Инициализация:** `SeedUrlInitializer` вбрасывает стартовый URL → PostgreSQL + RabbitMQ
2. **Краулинг:** `RabbitUrlConsumer` получает URL → `WorkerPipeline` скачивает, парсит, сохраняет → публикует новые URL
3. **Синхронизация:** `PostgresToElasticSyncService` каждые 5 секунд копирует новые страницы из PostgreSQL → Elasticsearch
4. **Поиск:** Пользователь вводит запрос → `SearchService` ищет в Elasticsearch → возвращает результаты с подсветкой

---

## 🚀 Быстрый старт

Подробная инструкция по запуску: [local-dev.md](local-dev.md)

```bash
# 1. Инфраструктура
docker compose up -d

# 2. Сборка
mvn clean package -DskipTests

# 3. Запуск crawler-worker и search-api
java -jar crawler-worker/target/crawler-worker-0.1.0-SNAPSHOT.jar
java -jar search-api/target/search-api-0.1.0-SNAPSHOT.jar

# 4. Открыть http://localhost:8081
```

---

## 🛠️ Стек технологий

Java 21 • Spring Boot 3.3.5 • PostgreSQL 15 • Elasticsearch 8.11 • RabbitMQ 3.12 • Redis 7.2 • Jsoup • Flyway • Docker Compose • GitHub Actions