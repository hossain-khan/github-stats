# GitHub CLI API Client Caching

This document describes the caching capabilities of `GhCliApiClient` that help reduce API rate limit consumption and improve performance.

## Overview

`GhCliApiClient` supports **database-based caching** using PostgreSQL to store API responses persistently. This brings cache performance parity with the Retrofit-based client implementation.

## Benefits

- **Reduced API Rate Limit Usage**: Cached responses don't count against GitHub's API rate limits
- **Faster Response Times**: Cached responses are served instantly without shell command overhead
- **Persistent Cache**: Cache survives application restarts (stored in PostgreSQL)
- **Performance Parity**: Similar caching capabilities as RetrofitApiClient
- **Cache Analytics**: Integrated with `CacheStatsService` for performance monitoring

## Configuration

### Enable Database Caching

Add these properties to your `local.properties` file:

```properties
# Database cache configuration
db_cache_url=jdbc:postgresql://localhost:5432/github_stats_cache
db_cache_username=your_username
db_cache_password=your_password
db_cache_expiration_hours=24
```

### Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `db_cache_url` | PostgreSQL JDBC connection URL | Required for caching |
| `db_cache_username` | Database username | Required for caching |
| `db_cache_password` | Database password | Required for caching |
| `db_cache_expiration_hours` | Cache expiration time in hours | 24 hours |

If database cache properties are not configured, GhCliApiClient will work without caching (direct API calls).

## How It Works

### Cache Flow

1. **Cache Lookup**: Before executing `gh api` command, check if response exists in cache
2. **Cache Hit**: If found and not expired, return cached response immediately
3. **Cache Miss**: Execute `gh api` command and store response in cache for future use
4. **Cache Expiration**: Cached responses expire after configured hours (default 24h)

### Cache Key Generation

Cache keys are generated from the API endpoint URL and query parameters:

```
https://api.github.com/repos/{owner}/{repo}/pulls/{number}?page=1&per_page=100
```

This ensures consistent cache keys between Retrofit and GH CLI implementations.

## Performance Comparison

### Without Caching
```
GH CLI API Client Statistics:
  Total Requests: 100
  Total Time: 45000ms
  Average Time: 450ms per request
```

### With Database Caching (50% hit rate)
```
GH CLI API Client Statistics:
  Total Requests: 100
  Cache Hits: 50
  Cache Misses: 50
  Cache Hit Rate: 50.0%
  Total Time: 22500ms
  Average Time: 225ms per request
```

**Result**: ~50% reduction in execution time with 50% cache hit rate.

## Monitoring Cache Performance

### Enable Logging

Set log level to see cache operations:

```kotlin
// In BuildConfig.kt or local configuration
const val LOG_LEVEL = 2 // DEBUG level
```

### Example Cache Logs

```
[INFO] GhCliApiClient initialized - using GitHub CLI for API requests with database caching
[DEBUG] [1] GH CLI API Request: /repos/owner/repo/pulls/123
[DEBUG] [1] Response from cache in 5ms (2847 bytes)
[DEBUG] [2] GH CLI API Request: /repos/owner/repo/pulls/124
[DEBUG] [2] Response received in 423ms (2951 bytes)
```

### View Statistics

Call `getRequestStatistics()` to see detailed metrics:

```kotlin
val client = GhCliApiClient(
    databaseCacheService = cacheService,
    cacheStatsService = statsService
)

// After running operations
println(client.getRequestStatistics())
```

Output:
```
GH CLI API Client Statistics:
  Total Requests: 25
  Cache Hits: 18
  Cache Misses: 7
  Cache Hit Rate: 72.0%
  Total Time: 3245ms
  Average Time: 129ms per request
  Total Data: 0.51 MB
  Average Size: 21234 bytes per response
```

## Cache Statistics Integration

`GhCliApiClient` integrates with the global `CacheStatsService` to track:

- Database cache hits per request URL
- Database cache misses per request URL
- Aggregate cache performance metrics

### View Global Cache Report

At the end of stats generation, the application displays a comprehensive cache report:

```
📊 Cache Performance Statistics
══════════════════════════════════════════════════
┌─────────────────────────────┬───────────┐
│ Cache Performance Summary   │           │
├─────────────────────────────┼───────────┤
│ Total API Requests          │ 150       │
├─────────────────────────────┼───────────┤
│ Database Cache Hits         │ 95 (63%)  │
├─────────────────────────────┼───────────┤
│ Network Requests            │ 55 (37%)  │
├─────────────────────────────┼───────────┤
│ Overall Cache Effectiveness │ 63%       │
└─────────────────────────────┴───────────┘

🔍 Cache Analysis:
✅ Good cache performance!
💾 Database cache is working effectively with 95 hits.
```

## Troubleshooting

### Cache Not Working

**Symptoms**: All requests show "Cache Miss" in logs

**Solutions**:
1. Verify database connection properties in `local.properties`
2. Check PostgreSQL is running: `pg_isready`
3. Verify database exists and user has permissions
4. Check application logs for database setup errors

### Performance Not Improved

**Symptoms**: Cache hit rate is low even after multiple runs

**Solutions**:
1. Increase `db_cache_expiration_hours` if data doesn't change frequently
2. Run the same analysis multiple times to populate cache
3. Check if different query parameters are being used (creates different cache keys)

### Cache Taking Too Much Space

**Solution**: Reduce `db_cache_expiration_hours` or manually clear old cache entries:

```sql
-- Clear cache entries older than 7 days
DELETE FROM response_cache 
WHERE created_at < NOW() - INTERVAL '7 days';
```

## Comparison: Retrofit vs GH CLI Caching

| Feature | RetrofitApiClient | GhCliApiClient |
|---------|------------------|----------------|
| Database Cache | ✅ Yes | ✅ Yes |
| OkHttp File Cache | ✅ Yes | ❌ No |
| Cache Expiration | ✅ Configurable | ✅ Configurable |
| Cache Statistics | ✅ Full tracking | ✅ Full tracking |
| Performance | Fastest | Fast (with cache) |

## Best Practices

1. **Enable Database Caching**: Always configure database cache for production use
2. **Set Appropriate Expiration**: Balance freshness vs cache hit rate
   - Stable data (old PRs): 168 hours (7 days)
   - Recent data: 24 hours (default)
   - Active development: 4-8 hours
3. **Monitor Cache Hit Rate**: Aim for >50% hit rate for repeated analyses
4. **Share Cache**: Multiple runs can benefit from shared PostgreSQL cache
5. **Regular Maintenance**: Periodically clear very old cache entries

## Related Files

- `src/main/kotlin/dev/hossain/githubstats/client/GhCliApiClient.kt` - Main implementation
- `src/main/kotlin/dev/hossain/githubstats/cache/DatabaseCacheService.kt` - Cache service
- `src/main/kotlin/dev/hossain/githubstats/cache/CacheStatsService.kt` - Statistics tracking
- `docs/GH_CLI_LOGGING.md` - Logging configuration guide

## See Also

- [GitHub Rate Limits](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [GitHub CLI Documentation](https://cli.github.com/manual/)
