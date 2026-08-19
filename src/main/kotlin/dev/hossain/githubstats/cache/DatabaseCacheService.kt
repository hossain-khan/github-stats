package dev.hossain.githubstats.cache

import java.security.MessageDigest

/**
 * Interface for database-based caching of GitHub API responses.
 *
 * Implementations provide persistence using SQLite or PostgreSQL.
 */
interface DatabaseCacheService {
    /**
     * Retrieves a cached response for the given URL if it exists and hasn't expired.
     *
     * @param url The request URL to look up
     * @return The cached JSON response string, or null if not found or expired
     */
    suspend fun getCachedResponse(url: String): String?

    /**
     * Stores a response in the database cache with expiration time.
     *
     * @param url The request URL
     * @param responseJson The JSON response to cache
     * @param httpStatus The HTTP status code of the response
     */
    suspend fun cacheResponse(
        url: String,
        responseJson: String,
        httpStatus: Int = 200,
    )

    /**
     * Cleans up expired cache entries to maintain database performance.
     */
    suspend fun cleanupExpiredEntries()

    /**
     * Gets cache statistics for monitoring and debugging.
     */
    suspend fun getCacheStats(): CacheStats?

    /**
     * Generates a unique SHA-256 hash cache key for the given URL.
     */
    fun generateCacheKey(url: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(url.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Data class representing cache statistics.
 */
data class CacheStats(
    val totalEntries: Long,
    val validEntries: Long,
    val expiredEntries: Long,
)
