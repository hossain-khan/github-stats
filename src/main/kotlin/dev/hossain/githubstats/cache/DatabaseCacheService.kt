package dev.hossain.githubstats.cache

import dev.hossain.githubstats.cache.database.GitHubStatsDatabase
import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.time.OffsetDateTime

/**
 * Database-based cache service for GitHub API responses using PostgreSQL JSON storage.
 *
 * This service stores HTTP responses as JSON in PostgreSQL using JSONB for efficient
 * storage and querying. It works alongside OkHttp caching to provide persistent caching.
 */
class DatabaseCacheService(
    private val database: GitHubStatsDatabase,
    private val expirationHours: Long = 24L,
) {
    /**
     * Retrieves a cached response for the given URL if it exists and hasn't expired.
     *
     * @param url The request URL to look up
     * @return The cached JSON response string, or null if not found or expired
     */
    suspend fun getCachedResponse(url: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = generateCacheKey(url)
                val cachedData = database.responseCacheQueries.getCachedResponse(cacheKey).executeAsOneOrNull()

                cachedData?.let {
                    Log.d("DatabaseCache: Cache HIT for URL: $url")
                    it.response_data
                } ?: run {
                    Log.d("DatabaseCache: Cache MISS for URL: $url")
                    null
                }
            } catch (e: Exception) {
                Log.w("DatabaseCache: Error retrieving cached response for $url: ${e.message}")
                null
            }
        }

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
    ) = withContext(Dispatchers.IO) {
        try {
            val cacheKey = generateCacheKey(url)
            val expiresAt = OffsetDateTime.now().plusHours(expirationHours)

            database.responseCacheQueries.insertOrReplaceResponse(
                cache_key = cacheKey,
                response_data = responseJson,
                request_url = url,
                http_status = httpStatus,
                expires_at = expiresAt,
            )

            Log.d("DatabaseCache: Cached response for URL: $url (expires: $expiresAt)")
        } catch (e: Exception) {
            Log.w("DatabaseCache: Error caching response for $url: ${e.message}")
        }
    }

    /**
     * Cleans up expired cache entries to maintain database performance.
     */
    suspend fun cleanupExpiredEntries() =
        withContext(Dispatchers.IO) {
            try {
                database.responseCacheQueries.deleteExpiredResponses()
                Log.d("DatabaseCache: Cleaned up expired cache entries")
            } catch (e: Exception) {
                Log.w("DatabaseCache: Error cleaning up expired entries: ${e.message}")
            }
        }

    /**
     * Gets cache statistics for monitoring and debugging.
     */
    suspend fun getCacheStats(): CacheStats? =
        withContext(Dispatchers.IO) {
            try {
                database.responseCacheQueries.getCacheStats().executeAsOneOrNull()?.let { stats ->
                    CacheStats(
                        totalEntries = stats.total_entries,
                        validEntries = stats.valid_entries,
                        expiredEntries = stats.expired_entries,
                    )
                }
            } catch (e: Exception) {
                Log.w("DatabaseCache: Error getting cache stats: ${e.message}")
                null
            }
        }

    /**
     * Generates a unique cache key for the given URL using SHA-256 hash.
     * This ensures consistent key generation and handles long URLs.
     */
    private fun generateCacheKey(url: String): String {
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
