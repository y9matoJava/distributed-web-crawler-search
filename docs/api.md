# API Documentation

## What this file is for

This file describes the planned HTTP API for the Web Crawler project.

The API will be used to:

* start crawling from a seed URL;
* check crawl status;
* view crawled pages;
* search pages by keywords;
* inspect crawler results.

This document describes the planned API contract.  
Implementation can be added step by step.

---

## Base URL

Local development base URL:

```text
http://localhost:8080
```

Example API prefix:

```text
/api/v1
```

Full local API base URL:

```text
http://localhost:8080/api/v1
```

---

## API Principles

The API should be:

* simple;
* predictable;
* REST-like;
* easy to test with curl or Postman;
* stable enough for future frontend or external clients.

Responses should use JSON.

Errors should also use JSON.

---

## Start Crawl

Starts crawling from a seed URL.

```http
POST /api/v1/crawls
```

### Request body

```json
{
  "seedUrl": "https://example.com",
  "maxDepth": 2,
  "allowedHosts": [
    "example.com"
  ]
}
```

### Fields

```text
seedUrl      - first URL to crawl
maxDepth     - maximum crawl depth
allowedHosts - list of hosts that crawler is allowed to visit
```

### Example response

```json
{
  "crawlId": "crawl-001",
  "seedUrl": "https://example.com",
  "status": "QUEUED",
  "maxDepth": 2,
  "allowedHosts": [
    "example.com"
  ]
}
```

---

## Get Crawl Status

Returns current status of a crawl job.

```http
GET /api/v1/crawls/{crawlId}
```

### Example response

```json
{
  "crawlId": "crawl-001",
  "seedUrl": "https://example.com",
  "status": "RUNNING",
  "pagesDiscovered": 10,
  "pagesFetched": 6,
  "pagesFailed": 1
}
```

### Possible statuses

```text
QUEUED
RUNNING
COMPLETED
FAILED
CANCELLED
```

---

## List Crawled Pages

Returns pages discovered or fetched during a crawl.

```http
GET /api/v1/crawls/{crawlId}/pages
```

### Example response

```json
{
  "crawlId": "crawl-001",
  "pages": [
    {
      "url": "https://example.com",
      "canonicalUrl": "https://example.com",
      "title": "Example Domain",
      "status": "FETCHED",
      "depth": 0
    },
    {
      "url": "https://example.com/about",
      "canonicalUrl": "https://example.com/about",
      "title": "About",
      "status": "FETCHED",
      "depth": 1
    }
  ]
}
```

---

## Get Page Details

Returns detailed information about one crawled page.

```http
GET /api/v1/pages/{pageId}
```

### Example response

```json
{
  "pageId": "page-001",
  "url": "https://example.com",
  "canonicalUrl": "https://example.com",
  "title": "Example Domain",
  "description": "Example page description",
  "headings": [
    "Example Domain"
  ],
  "keywords": [
    "example",
    "domain",
    "page"
  ],
  "status": "FETCHED",
  "depth": 0
}
```

---

## Search Pages

Searches crawled pages by query text or keyword.

```http
GET /api/v1/search?q=crawler
```

### Query parameters

```text
q - search query
```

### Example response

```json
{
  "query": "crawler",
  "results": [
    {
      "pageId": "page-001",
      "url": "https://example.com/java-crawler",
      "title": "Java Web Crawler",
      "keywords": [
        "java",
        "crawler",
        "links"
      ]
    }
  ]
}
```

---

## Cancel Crawl

Cancels a running crawl job.

```http
POST /api/v1/crawls/{crawlId}/cancel
```

### Example response

```json
{
  "crawlId": "crawl-001",
  "status": "CANCELLED"
}
```

---

## Health Check

Checks whether the application is running.

```http
GET /api/v1/health
```

### Example response

```json
{
  "status": "UP"
}
```

---

## Error Response Format

All API errors should use the same JSON structure.

### Example

```json
{
  "error": "INVALID_REQUEST",
  "message": "seedUrl must not be empty",
  "path": "/api/v1/crawls"
}
```

### Common error codes

```text
INVALID_REQUEST
CRAWL_NOT_FOUND
PAGE_NOT_FOUND
INTERNAL_ERROR
```

---

## First API Version

The first API version can be very simple.

Minimum endpoints:

```text
POST /api/v1/crawls
GET  /api/v1/crawls/{crawlId}
GET  /api/v1/crawls/{crawlId}/pages
GET  /api/v1/search?q=...
GET  /api/v1/health
```

Cancel crawl and detailed page information can be added later.

---

## Future API Improvements

Later the API can support:

* pagination;
* sorting;
* filtering by status;
* filtering by host;
* filtering by depth;
* crawl configuration presets;
* authentication;
* rate limits;
* OpenAPI/Swagger documentation.

---

## Summary

The API should provide a simple way to control and inspect the crawler.

Main actions:

* start a crawl;
* check crawl status;
* list crawled pages;
* view page details;
* search indexed pages;
* check application health.

The first version should stay small and easy to implement.