
# Web Crawler Notes

## What this file is for

This file contains simple learning notes about how a Web Crawler works.

This is not the final architecture document.  
Architecture is described in:

```text
docs/architecture.md
````

This is not the full development roadmap.
Roadmap is described in:

```text
docs/roadmap.md
```

---

## Core Idea

A Web Crawler starts from a seed URL, downloads the HTML page, extracts links from it, and then visits newly discovered pages.

Basic flow:

```text
Seed URL
  -> Download HTML
  -> Parse HTML
  -> Extract links
  -> Normalize links
  -> Skip duplicates
  -> Add new links to queue
  -> Repeat
```

The main goal is to discover pages and collect useful information from them.

---

## Simple Example

Start URL:

```text
https://example.com/
```

The page contains links:

```text
/about
/articles
/contact
```

The crawler converts them to absolute URLs:

```text
https://example.com/about
https://example.com/articles
https://example.com/contact
```

Then the crawler visits these pages too.

---

## Why Cycles Are a Problem

The web is a graph.

Pages can link to each other in cycles.

Example:

```text
/a -> /b
/b -> /c
/c -> /a
```

Without protection, crawler will visit pages forever:

```text
/a
/b
/c
/a
/b
/c
...
```

To prevent this, crawler must remember already discovered URLs.

---

## Discovered URLs

The crawler needs two main data structures in the first version:

```text
Queue<String> urlsToVisit
Set<String> discoveredUrls
```

The queue stores URLs that should be processed.

The set stores URLs that were already discovered.

Important rule:

```text
Mark URL as discovered when it is added to the queue, not after it is fetched.
```

Bad approach:

```text
fetch URL -> mark as visited
```

Good approach:

```text
discover URL -> normalize -> if not seen -> mark as discovered -> enqueue
```

This prevents the same URL from being added many times.

---

## URL Normalization

Different URLs can point to the same page.

Examples:

```text
https://example.com/page
https://example.com/page#section
https://example.com/page?utm_source=telegram
https://example.com:443/page
```

The crawler should convert URLs to a stable canonical form.

Example:

```text
https://Example.com/Page#top
=> https://example.com/Page
```

This is called:

```text
URL canonicalization
```

It helps with deduplication.

---

## URL Deduplication

URL deduplication means:

```text
Do not process the same canonical URL more than once.
```

In the first version, this can be done in memory:

```text
Set<String> discoveredUrls
```

Later, this will be moved to PostgreSQL:

```text
canonical_url UNIQUE
```

PostgreSQL will make deduplication persistent and safe for multiple workers.

---

## Queue

A crawler should use a queue instead of direct recursion.

Simple version:

```text
Queue<String> urlsToVisit
```

Later version:

```text
RabbitMQ URL Queue
```

The queue helps to control crawling and later allows multiple workers.

---

## In-Memory Crawler First

Before adding PostgreSQL, RabbitMQ, Redis, and Elasticsearch, the first crawler should work in memory.

The first version should prove:

* seed URL is processed;
* links are extracted;
* duplicate URLs are skipped;
* cyclic links do not create an infinite loop;
* max depth works;
* allowed hosts work.

Only after this should the project move to persistent and distributed architecture.

---

## Keywords

The crawler should extract keywords from every page.

Simple keyword extraction algorithm:

```text
1. Extract title.
2. Extract headings.
3. Extract body text.
4. Convert text to lowercase.
5. Remove punctuation.
6. Split text into words.
7. Remove stop words.
8. Ignore short words.
9. Count word frequency.
10. Boost words from title.
11. Boost words from headings.
12. Return top keywords.
```

Example text:

```text
Java crawler extracts links from pages. Crawler stores pages.
```

Possible keywords:

```text
crawler
java
pages
links
extracts
```

---

## Search

The simplest search version can be in memory:

```text
keyword -> pages
```

Example:

```text
java -> https://example.com/java-crawler
crawler -> https://example.com/java-crawler
search -> https://example.com/search-engine
```

Later, this will be replaced or extended with:

```text
Elasticsearch
```

Elasticsearch will allow full-text search by:

* title;
* headings;
* keywords;
* description;
* content.

---

## Project Evolution

The project should grow in this order:

```text
1. In-memory crawler
2. URL canonicalization
3. Link extraction
4. Text extraction
5. Keyword extraction
6. In-memory search
7. PostgreSQL persistent state
8. RabbitMQ URL queue
9. Crawler worker pipeline
10. Elasticsearch full-text search
11. Redis rate limiting
12. robots.txt support
13. Multiple workers
14. Tests, CI, documentation
```

---

## Summary

A Web Crawler is not just a program that downloads pages.

A good crawler should:

* discover pages;
* extract links;
* avoid cycles;
* avoid duplicates;
* respect limits;
* extract useful text;
* extract keywords;
* store state;
* support search;
* be safe and predictable.

The first goal of this project is to understand and implement the core algorithm:

```text
URL -> HTML -> links -> queue -> dedup -> repeat
