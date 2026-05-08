# Testing Guide

## What this file is for

This file describes how the Web Crawler project should be tested.

The goal of testing is to make sure that the crawler:

* works correctly;
* does not crawl duplicates;
* does not loop forever;
* handles errors safely;
* can be changed without breaking core logic.

---

## Testing Principles

Tests should be added step by step.

The first tests should check the core crawler logic without PostgreSQL, RabbitMQ, Redis, or Elasticsearch.

Infrastructure tests should be added later.

Main rule:

```text
Test the crawler algorithm first.
Test infrastructure later.
```

---

## Test Levels

The project should have different kinds of tests:

```text
Unit tests
Integration tests
End-to-end smoke tests
```

Each test type has a different purpose.

---

## Unit Tests

Unit tests check one small class or function.

They should be fast and should not require Docker or internet access.

Examples:

```text
UrlCanonicalizerTest
UrlFilterTest
LinkExtractorTest
TextExtractorTest
KeywordExtractorTest
InMemoryCrawlerTest
```

Unit tests should be the first testing layer.

---

## Integration Tests

Integration tests check how several parts work together.

Examples:

```text
PostgreSQL repository tests
RabbitMQ producer/consumer tests
Elasticsearch indexing tests
Search API tests
Crawler worker pipeline tests
```

Integration tests may use:

```text
Testcontainers
Docker
Spring Boot test context
```

These tests are slower than unit tests, but they verify real infrastructure behavior.

---

## End-to-End Smoke Tests

End-to-end smoke tests check the project from a user point of view.

Example flow:

```text
1. Start local infrastructure.
2. Start crawler worker.
3. Start search API.
4. Submit seed URL.
5. Wait for crawling.
6. Search by keyword.
7. Verify that results are returned.
```

This type of test should be small and stable.

---

## No Real Internet in Tests

Tests should not depend on real external websites.

Bad approach:

```text
test crawler on https://example.com
test crawler on random public websites
```

Good approach:

```text
use local HTML fixtures
use fake HTTP server
use WireMock or MockWebServer
```

Real websites can change, become unavailable, block requests, or respond slowly.

Tests should be predictable.

---

## HTML Fixtures

HTML fixtures should be stored in test resources.

Example location:

```text
crawler-core/src/test/resources/html/
```

Example files:

```text
simple-page.html
relative-links.html
duplicate-links.html
cyclic-a.html
cyclic-b.html
cyclic-c.html
page-with-script.html
page-with-headings.html
```

Fixtures make parser tests easy to understand.

---

## UrlCanonicalizer Tests

`UrlCanonicalizer` should have tests for URL normalization.

Test examples:

```text
https://Example.com/Page
=> https://example.com/Page
```

```text
https://example.com/page#section
=> https://example.com/page
```

```text
https://example.com:443/page
=> https://example.com/page
```

```text
http://example.com:80/page
=> http://example.com/page
```

```text
https://example.com/page?utm_source=telegram
=> https://example.com/page
```

It should also reject unsupported schemes:

```text
mailto:test@example.com
tel:+123456789
javascript:alert(1)
```

---

## LinkExtractor Tests

`LinkExtractor` should check how links are extracted from HTML.

It should test:

* absolute links;
* relative links;
* duplicate links;
* empty links;
* fragment-only links;
* unsupported schemes;
* anchor text;
* base URL handling.

Example:

```html
<a href="/about">About</a>
```

With base URL:

```text
https://example.com/
```

Expected result:

```text
https://example.com/about
```

---

## TextExtractor Tests

`TextExtractor` should check text extraction from HTML.

It should extract:

```text
title
meta description
h1
h2
h3
visible body text
```

It should not include:

```text
script content
style content
noscript noise
```

Important test:

```text
JavaScript code should not appear in extracted page text.
```

---

## KeywordExtractor Tests

`KeywordExtractor` should check keyword extraction.

It should test:

* lowercase conversion;
* punctuation removal;
* stop word removal;
* short word filtering;
* word frequency;
* title boosting;
* heading boosting;
* deterministic results.

Example text:

```text
Java crawler extracts links. Crawler stores pages.
```

Expected keywords can include:

```text
crawler
java
links
pages
```

The exact order should be deterministic.

---

## InMemoryCrawler Tests

`InMemoryCrawler` should prove that the core crawler algorithm works.

It should test:

* seed URL processing;
* queue behavior;
* discovered URL set;
* duplicate protection;
* cyclic links;
* max depth;
* max pages;
* allowed hosts.

Important cyclic test:

```text
/a -> /b
/b -> /c
/c -> /a
```

Expected behavior:

```text
/a processed once
/b processed once
/c processed once
crawler stops
```

The crawler must not loop forever.

---

## PostgreSQL Tests

PostgreSQL tests should verify persistent crawler state.

They should test:

* `canonical_url` uniqueness;
* insert-if-not-exists behavior;
* URL status transitions;
* document persistence;
* page keywords;
* out links;
* repeated discovery of the same URL.

Important rule:

```text
canonical_url must be UNIQUE
```

This protects the crawler from duplicates when multiple workers run.

---

## RabbitMQ Tests

RabbitMQ tests should verify URL job flow.

They should test:

* producer publishes `UrlJob`;
* consumer receives `UrlJob`;
* message format is correct;
* retry queue works;
* dead-letter queue works;
* duplicate messages are handled safely.

Important rule:

```text
RabbitMQ can deliver messages more than once.
```

Therefore, the worker must be idempotent.

---

## Redis Tests

Redis tests should verify temporary distributed state.

They should test:

* per-host lock;
* lock TTL;
* rate limit behavior;
* lock expiration after worker crash;
* robots.txt cache behavior.

Important rule:

```text
Redis lock must have TTL.
```

Without TTL, a crashed worker could block a host forever.

---

## Elasticsearch Tests

Elasticsearch tests should verify indexing and search.

They should test:

* document indexing;
* deterministic document ID;
* repeated indexing does not create duplicates;
* search by title;
* search by headings;
* search by keywords;
* search by content;
* score exists;
* snippet or highlight exists.

Example search:

```text
GET /api/search?q=crawler
```

Expected result:

```text
title
url
score
snippet
```

---

## Search API Tests

Search API tests should verify HTTP behavior.

They should test:

* successful search;
* empty query;
* pagination;
* no results;
* invalid parameters;
* JSON response format;
* error response format.

Example endpoint:

```http
GET /api/v1/search?q=crawler
```

Expected response fields:

```text
query
results
title
url
score
```

---

## Crawler Worker Pipeline Tests

Crawler worker tests should verify the full processing flow.

Happy path:

```text
UrlJob received
  -> URL loaded from PostgreSQL
  -> HTML fetched
  -> links extracted
  -> text extracted
  -> keywords extracted
  -> document saved
  -> new URLs discovered
  -> document indexed
  -> URL marked completed
```

Error cases:

```text
timeout
HTTP 404
HTTP 500
robots.txt disallow
rate limit busy
duplicate content
invalid HTML
```

Each error should have a clear expected behavior.

---

## Testcontainers

Testcontainers can be used for integration tests with real services.

Possible containers:

```text
PostgreSQL
RabbitMQ
Redis
Elasticsearch
```

This helps test real behavior without requiring manually installed services.

Example use cases:

```text
PostgreSQL migration tests
RabbitMQ queue tests
Redis lock tests
Elasticsearch indexing tests
```

---

## Maven Test Commands

Run all tests:

```bash
mvn clean test
```

Run tests for one module:

```bash
mvn -pl crawler-core test
```

Run one test class:

```bash
mvn -pl crawler-core -Dtest=UrlCanonicalizerTest test
```

Run worker tests:

```bash
mvn -pl crawler-worker test
```

Run search API tests:

```bash
mvn -pl search-api test
```

---

## Test Naming

Test names should describe behavior.

Good examples:

```text
shouldRemoveFragmentFromUrl
shouldRejectUnsupportedScheme
shouldExtractRelativeLinksAsAbsoluteUrls
shouldSkipDuplicateUrls
shouldStopOnCyclicLinks
shouldReturnPagesForKeyword
```

Bad examples:

```text
test1
urlTest
crawlerWorks
check
```

Readable test names make the project easier to maintain.

---

## What Not To Test Too Early

Do not test everything at once.

In the first version, do not focus on:

```text
RabbitMQ
Redis
Elasticsearch
multiple workers
real websites
performance
large-scale crawling
```

First, test:

```text
URL normalization
HTML parsing
link extraction
text extraction
keyword extraction
in-memory crawler
cycle protection
```

---

## Definition of Done for Tests

A feature is not finished until:

* unit tests exist;
* important edge cases are covered;
* tests are readable;
* tests are deterministic;
* tests do not require real internet;
* `mvn test` passes;
* failures are easy to understand.

---

## Summary

Testing should protect the project from broken crawler behavior.

The most important things to test first are:

* URL canonicalization;
* duplicate detection;
* cyclic links;
* max depth;
* allowed hosts;
* link extraction;
* text extraction;
* keyword extraction;
* in-memory search.

The main testing rule:

```text
Make the core algorithm reliable before adding distributed infrastructure.
```