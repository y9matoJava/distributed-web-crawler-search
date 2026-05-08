# Responsible Crawling

## What this file is for

This file describes how the Web Crawler should behave safely and responsibly.

A crawler should not only download pages.  
It should also respect websites, avoid overload, and follow crawling rules.

---

## Core Idea

Responsible crawling means:

* do not overload websites;
* respect `robots.txt`;
* limit request rate per host;
* avoid infinite crawling;
* avoid crawling forbidden pages;
* handle errors carefully;
* make crawler behavior predictable.

The crawler should be useful, but not aggressive.

---

## Why Responsible Crawling Matters

A web crawler can send many HTTP requests.

Without limits, it can:

* overload small websites;
* ignore site owner rules;
* create unnecessary traffic;
* get blocked;
* produce unreliable results;
* behave like a bad bot.

The goal of this project is to build a safe educational crawler.

---

## robots.txt

`robots.txt` is a file that websites use to describe which pages crawlers may or may not access.

Example location:

```text
https://example.com/robots.txt
```

Example content:

```text
User-agent: *
Disallow: /private/
Disallow: /admin/
Allow: /
```

Before fetching a page, the crawler should check whether the URL is allowed.

---

## robots.txt Rule

The crawler should check `robots.txt` before downloading a page.

Basic flow:

```text
URL discovered
  -> extract host
  -> load robots.txt for host
  -> check if URL is allowed
  -> if allowed: fetch page
  -> if disallowed: skip page
```

If a URL is disallowed, the crawler should not fetch it.

Instead, it should mark it with status:

```text
SKIPPED_ROBOTS
```

---

## robots.txt Cache

The crawler should not download `robots.txt` before every page request.

Bad approach:

```text
fetch robots.txt
fetch page 1
fetch robots.txt
fetch page 2
fetch robots.txt
fetch page 3
```

Good approach:

```text
fetch robots.txt once per host
store it in cache
reuse cached rules
```

Later, Redis can be used for this cache:

```text
host -> robots.txt rules
```

Example:

```text
example.com -> cached robots rules
```

---

## If robots.txt Does Not Exist

If a website returns `404 Not Found` for `robots.txt`, the crawler can usually treat this as:

```text
no robots restrictions found
```

Then crawling may continue, while still respecting rate limits.

---

## If robots.txt Times Out

If `robots.txt` cannot be fetched because of timeout or temporary error, the crawler should be careful.

Possible safe behavior:

```text
do not fetch the target page immediately
retry robots.txt later
```

This avoids accidentally crawling forbidden pages.

---

## Rate Limiting

Rate limiting means controlling how often the crawler sends requests.

The crawler should not send too many requests to the same host too quickly.

Bad behavior:

```text
100 requests to example.com in one second
```

Better behavior:

```text
1 request to example.com
wait
next request to example.com
```

---

## Per-Host Rate Limiting

Rate limiting should be done per host.

Example:

```text
example.com       -> limited separately
docs.example.com  -> limited separately
another-site.com  -> limited separately
```

This means one slow website should not block crawling of all other websites.

---

## Redis Rate Limiter

Later, Redis can be used for distributed rate limiting.

This is important when multiple crawler workers run at the same time.

Example:

```text
worker-1 wants to fetch example.com/page-1
worker-2 wants to fetch example.com/page-2
worker-3 wants to fetch another-site.com/page
```

Redis can help ensure that only one worker fetches from `example.com` during a short time window.

---

## Redis Lock With TTL

A simple distributed lock can be stored in Redis:

```text
crawl:host-lock:example.com
```

The lock should have TTL.

TTL means:

```text
time to live
```

Example:

```text
lock host for 2 seconds
```

TTL is important because if a worker crashes, the lock will eventually expire.

Without TTL, the host could stay locked forever.

---

## Rate Limit Flow

Basic flow:

```text
URL job received
  -> extract host
  -> check robots.txt
  -> try acquire host rate limit lock
  -> if lock acquired: fetch page
  -> if lock not acquired: retry later
```

If rate limit is busy, the URL should not be marked as failed.

It should be retried later.

Possible status:

```text
RATE_LIMITED
```

or the job can be requeued with delay.

---

## Max Depth

The crawler should limit how deep it goes from the seed URL.

Example:

```text
depth 0 -> seed URL
depth 1 -> links from seed page
depth 2 -> links from depth 1 pages
```

If max depth is `2`, then pages deeper than that should not be crawled.

Possible status:

```text
SKIPPED_DEPTH
```

---

## Allowed Hosts

The crawler should support allowed hosts.

Example:

```text
allowedHosts = ["example.com"]
```

If the crawler discovers:

```text
https://example.com/about
```

it can crawl it.

If it discovers:

```text
https://another-site.com/page
```

it should skip it.

Possible status:

```text
SKIPPED_HOST
```

This prevents the crawler from accidentally crawling the whole internet.

---

## Retry Behavior

Some errors are temporary.

Examples:

```text
timeout
connection reset
HTTP 503
HTTP 429
```

These can be retried later.

Some errors are usually permanent.

Examples:

```text
HTTP 404
invalid URL
unsupported scheme
robots.txt disallow
```

Permanent errors should not be retried forever.

---

## Dead Letter Queue

If RabbitMQ is used, failed jobs can eventually move to a dead-letter queue.

A dead-letter queue stores jobs that could not be processed successfully.

Example:

```text
crawler.url.ready
crawler.url.retry
crawler.url.dead
```

The crawler should not retry the same broken URL forever.

---

## Safe Fetching Rules

The crawler should:

* use timeouts;
* limit response size;
* fetch only allowed schemes;
* avoid login pages;
* avoid private/admin paths when disallowed;
* avoid infinite redirects;
* not submit forms;
* not bypass anti-bot systems;
* not ignore `robots.txt`.

Supported schemes should be:

```text
http
https
```

Unsupported schemes should be skipped:

```text
mailto:
tel:
javascript:
ftp:
file:
```

---

## User-Agent

The crawler should use a clear User-Agent.

Example:

```text
DistributedWebCrawlerSearch/1.0
```

Later it can include project information.

A clear User-Agent helps website owners understand who is making requests.

---

## Responsible Crawler Flow

Full responsible crawling flow:

```text
UrlJob received
  -> load URL from PostgreSQL
  -> skip if already terminal
  -> check max depth
  -> check allowed hosts
  -> check robots.txt
  -> acquire per-host rate limit
  -> fetch HTML with timeout
  -> parse page
  -> extract links
  -> discover allowed links
  -> save results
  -> index document
  -> mark as completed
```

---

## Summary

Responsible crawling makes the project safer and more realistic.

The crawler should:

* respect `robots.txt`;
* limit requests per host;
* use Redis for distributed rate limiting later;
* avoid crawling forbidden URLs;
* avoid infinite retries;
* use safe timeouts;
* skip unsupported URL schemes;
* support max depth and allowed hosts.

The main rule:

```text
Crawl carefully, predictably, and respectfully.
```