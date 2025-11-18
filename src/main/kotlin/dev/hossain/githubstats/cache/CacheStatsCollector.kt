package dev.hossain.githubstats.cache

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

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

    // For tracking PR-level cache status
    private val prCacheStatus = AtomicReference(PrCacheStatus())

    override fun recordDatabaseCacheHit() {
        databaseCacheHitsCounter.incrementAndGet()
    }

    override fun recordDatabaseCacheMiss() {
        databaseCacheMissesCounter.incrementAndGet()
    }

    override fun recordDatabaseCacheHit(url: String) {
        recordDatabaseCacheHit()
        updatePrCacheStatus(url, "HIT")
    }

    override fun recordDatabaseCacheMiss(url: String) {
        recordDatabaseCacheMiss()
        updatePrCacheStatus(url, "MISS")
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

    override fun getPrCacheStatusAndReset(): String? {
        val currentStatus = prCacheStatus.getAndSet(PrCacheStatus())
        return currentStatus.toSummaryString()
    }

    override fun reset() {
        databaseCacheHitsCounter.set(0)
        databaseCacheMissesCounter.set(0)
        okHttpCacheHitsCounter.set(0)
        networkRequestsCounter.set(0)
        prCacheStatus.set(PrCacheStatus())
    }

    /**
     * Updates the PR cache status based on the URL pattern and cache result.
     */
    private fun updatePrCacheStatus(
        url: String,
        status: String,
    ) {
        prCacheStatus.updateAndGet { current ->
            when {
                url.contains("/pulls/") && url.contains("/comments") -> {
                    current.copy(comments = status)
                }

                url.contains("/issues/") && url.contains("/timeline") -> {
                    current.copy(timeline = status)
                }

                url.contains("/pulls/") && !url.contains("/comments") -> {
                    current.copy(prInfo = status)
                }

                else -> {
                    current
                }
            }
        }
    }
}
