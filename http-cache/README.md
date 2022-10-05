Cache directory for OkHttp client to reduce being rate-limited.
https://square.github.io/okhttp/features/caching/


### Rate Limit Example
* https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting
* https://docs.github.com/en/rest/overview/resources-in-the-rest-api#secondary-rate-limits

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
Search API is [bit limited](https://docs.github.com/en/rest/search#rate-limit) and has 30 requests per minute.

```
x-ratelimit-limit: 30
x-ratelimit-remaining: 27
x-ratelimit-reset: 1664936651
x-ratelimit-used: 3
x-ratelimit-resource: search
```
