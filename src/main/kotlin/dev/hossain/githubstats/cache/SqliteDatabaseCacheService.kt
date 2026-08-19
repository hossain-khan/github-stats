package dev.hossain.githubstats.cache

import dev.hossain.githubstats.cache.database.sqlite.SqliteDatabase
import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Database cache service implementation for local SQLite file storage.
 *
 * Uses SQLite TEXT columns for JSON data and epoch millisecond timestamps.
 */
class SqliteDatabaseCacheService(
    private val database: SqliteDatabase,
    private val expirationHours: Long = 24L,
) : DatabaseCacheService {
    override suspend fun getCachedResponse(url: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = generateCacheKey(url)
                val currentTime = System.currentTimeMillis()
                val cachedData =
                    database.responseCacheQueries
                        .getCachedResponse(cache_key = cacheKey, currentTime = currentTime)
                        .executeAsOneOrNull()

                cachedData?.let {
                    Log.v("SqliteCache: Cache HIT for URL: $url")
                    it.response_data
                } ?: run {
                    Log.v("SqliteCache: Cache MISS for URL: $url")
                    null
                }
            } catch (e: Exception) {
                Log.w("SqliteCache: Error retrieving cached response for $url: ${e.message}")
                null
            }
        }

    override suspend fun cacheResponse(
        url: String,
        responseJson: String,
        httpStatus: Int,
    ) = withContext(Dispatchers.IO) {
        try {
            val cacheKey = generateCacheKey(url)
            val currentTime = System.currentTimeMillis()
            val expiresAt = currentTime + (expirationHours * 60 * 60 * 1000L)

            database.responseCacheQueries.insertOrReplaceResponse(
                cache_key = cacheKey,
                response_data = responseJson,
                request_url = url,
                http_status = httpStatus.toLong(),
                created_at = currentTime,
                expires_at = expiresAt,
            )

            Log.v("SqliteCache: Cached response for URL: $url")
        } catch (e: Exception) {
            Log.w("SqliteCache: Error caching response for $url: ${e.message}")
        }
    }

    override suspend fun cleanupExpiredEntries() =
        withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                database.responseCacheQueries.deleteExpiredResponses(currentTime = currentTime)
                Log.d("SqliteCache: Cleaned up expired cache entries")
            } catch (e: Exception) {
                Log.w("SqliteCache: Error cleaning up expired entries: ${e.message}")
            }
        }

    override suspend fun getCacheStats(): CacheStats? =
        withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                database.responseCacheQueries.getCacheStats(currentTime = currentTime).executeAsOneOrNull()?.let { stats ->
                    CacheStats(
                        totalEntries = stats.total_entries,
                        validEntries = stats.valid_entries,
                        expiredEntries = stats.expired_entries,
                    )
                }
            } catch (e: Exception) {
                Log.w("SqliteCache: Error getting cache stats: ${e.message}")
                null
            }
        }
}
