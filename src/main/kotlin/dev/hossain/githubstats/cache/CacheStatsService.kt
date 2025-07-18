package dev.hossain.githubstats.cache

/**
 * Service interface for collecting cache performance statistics during stats generation.
 *
 * This service tracks different types of cache hits and misses to provide insights
 * into how effective the caching layers are during GitHub API requests.
 */
interface CacheStatsService {
    /**
     * Records a database cache hit (response served from PostgreSQL).
     */
    fun recordDatabaseCacheHit()

    /**
     * Records a database cache miss (not found in PostgreSQL cache).
     */
    fun recordDatabaseCacheMiss()

    /**
     * Records an OkHttp cache hit (response served from OkHttp's file cache).
     */
    fun recordOkHttpCacheHit()

    /**
     * Records a network request (cache miss from all layers).
     */
    fun recordNetworkRequest()

    /**
     * Gets the current cache statistics snapshot.
     */
    fun getStats(): CachePerformanceStats

    /**
     * Resets all cache statistics counters.
     */
    fun reset()
}

/**
 * Data class representing cache performance statistics for a stats generation session.
 */
data class CachePerformanceStats(
    val databaseCacheHits: Long = 0,
    val databaseCacheMisses: Long = 0,
    val okHttpCacheHits: Long = 0,
    val networkRequests: Long = 0,
) {
    /**
     * Total number of requests processed.
     */
    val totalRequests: Long get() = databaseCacheHits + databaseCacheMisses + okHttpCacheHits + networkRequests

    /**
     * Database cache hit rate as a percentage.
     */
    val databaseCacheHitRate: Double get() = if (totalRequests > 0) (databaseCacheHits.toDouble() / totalRequests) * 100 else 0.0

    /**
     * OkHttp cache hit rate as a percentage.
     */
    val okHttpCacheHitRate: Double get() = if (totalRequests > 0) (okHttpCacheHits.toDouble() / totalRequests) * 100 else 0.0

    /**
     * Network request rate as a percentage.
     */
    val networkRequestRate: Double get() = if (totalRequests > 0) (networkRequests.toDouble() / totalRequests) * 100 else 0.0

    /**
     * Overall cache effectiveness (any cache hit) as a percentage.
     */
    val overallCacheHitRate: Double get() =
        if (totalRequests >
            0
        ) {
            ((databaseCacheHits + okHttpCacheHits).toDouble() / totalRequests) * 100
        } else {
            0.0
        }
}
