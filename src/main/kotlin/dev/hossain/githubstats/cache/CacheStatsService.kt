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
     * Records a database cache hit for a specific request type.
     */
    fun recordDatabaseCacheHit(url: String)

    /**
     * Records a database cache miss for a specific request type.
     */
    fun recordDatabaseCacheMiss(url: String)

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
     * Gets the cache status summary for PR-level requests and resets the tracking.
     */
    fun getPrCacheStatusAndReset(): String?

    /**
     * Resets all cache statistics counters.
     */
    fun reset()
}

/**
 * Represents the status of cache for different types of PR requests.
 */
data class PrCacheStatus(
    val prInfo: String? = null, // HIT, MISS, or null if not requested
    val timeline: String? = null, // HIT, MISS, or null if not requested
    val comments: String? = null, // HIT, MISS, or null if not requested
) {
    /**
     * Formats the cache status as a summary string.
     * Returns null if no cache-relevant requests were made.
     */
    fun toSummaryString(): String? {
        val parts = mutableListOf<String>()
        prInfo?.let { parts.add("PR info: $it") }
        timeline?.let { parts.add("Timeline: $it") }
        comments?.let { parts.add("Comments: $it") }

        return if (parts.isNotEmpty()) {
            "Database cache status (${parts.joinToString(", ")})"
        } else {
            null
        }
    }
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
     * Note: databaseCacheMisses are not included as they represent the same requests
     * that are later handled by either okHttpCacheHits or networkRequests.
     */
    val totalRequests: Long get() = databaseCacheHits + okHttpCacheHits + networkRequests

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
