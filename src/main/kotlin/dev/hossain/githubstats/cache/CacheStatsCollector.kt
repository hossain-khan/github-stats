package dev.hossain.githubstats.cache

import java.util.concurrent.atomic.AtomicLong

/**
 * Thread-safe implementation of CacheStatsService that collects cache performance statistics.
 *
 * This implementation uses atomic counters to ensure thread safety when multiple
 * HTTP requests are processed concurrently during stats generation.
 */
class CacheStatsCollector : CacheStatsService {
    private val databaseCacheHitsCounter = AtomicLong(0)
    private val databaseCacheMissesCounter = AtomicLong(0)
    private val okHttpCacheHitsCounter = AtomicLong(0)
    private val networkRequestsCounter = AtomicLong(0)

    override fun recordDatabaseCacheHit() {
        databaseCacheHitsCounter.incrementAndGet()
    }

    override fun recordDatabaseCacheMiss() {
        databaseCacheMissesCounter.incrementAndGet()
    }

    override fun recordOkHttpCacheHit() {
        okHttpCacheHitsCounter.incrementAndGet()
    }

    override fun recordNetworkRequest() {
        networkRequestsCounter.incrementAndGet()
    }

    override fun getStats(): CachePerformanceStats =
        CachePerformanceStats(
            databaseCacheHits = databaseCacheHitsCounter.get(),
            databaseCacheMisses = databaseCacheMissesCounter.get(),
            okHttpCacheHits = okHttpCacheHitsCounter.get(),
            networkRequests = networkRequestsCounter.get(),
        )

    override fun reset() {
        databaseCacheHitsCounter.set(0)
        databaseCacheMissesCounter.set(0)
        okHttpCacheHitsCounter.set(0)
        networkRequestsCounter.set(0)
    }
}
