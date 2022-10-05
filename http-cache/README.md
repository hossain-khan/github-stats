Cache directory for OkHttp client.
https://square.github.io/okhttp/features/caching/


### Example

#### Core APIs
Core APIs seems to have 5000 requests limit per hour.

```
x-ratelimit-limit: 5000
x-ratelimit-remaining: 1509
x-ratelimit-reset: 1664937631
x-ratelimit-used: 3491
x-ratelimit-resource: core
```

#### Search APIs
Search API is bit limited and has 30 requests per minute.

```
x-ratelimit-limit: 30
x-ratelimit-remaining: 27
x-ratelimit-reset: 1664936651
x-ratelimit-used: 3
x-ratelimit-resource: search
```
