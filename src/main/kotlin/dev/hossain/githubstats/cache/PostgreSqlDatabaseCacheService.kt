package dev.hossain.githubstats.cache

import dev.hossain.githubstats.cache.database.postgres.PostgreSqlDatabase
import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

/**
 * Database cache service implementation for PostgreSQL.
 *
 * Uses JSONB columns and PostgreSQL native timestamp queries.
 */
class PostgreSqlDatabaseCacheService(
    private val database: PostgreSqlDatabase,
    private val expirationHours: Long = 24L,
) : DatabaseCacheService {
    override suspend fun getCachedResponse(url: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = generateCacheKey(url)
                val cachedData = database.responseCacheQueries.getCachedResponse(cacheKey).executeAsOneOrNull()

                cachedData?.let {
                    Log.v("PostgreSqlCache: Cache HIT for URL: $url")
                    it.response_data
                } ?: run {
                    Log.v("PostgreSqlCache: Cache MISS for URL: $url")
                    null
                }
            } catch (e: Exception) {
                Log.w("PostgreSqlCache: Error retrieving cached response for $url: ${e.message}")
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
            val expiresAt = OffsetDateTime.now().plusHours(expirationHours)

            database.responseCacheQueries.insertOrReplaceResponse(
                cache_key = cacheKey,
                response_data = responseJson,
                request_url = url,
                http_status = httpStatus,
                expires_at = expiresAt,
            )

            Log.v("PostgreSqlCache: Cached response for URL: $url (expires: $expiresAt)")
        } catch (e: Exception) {
            Log.w("PostgreSqlCache: Error caching response for $url: ${e.message}")
        }
    }

    override suspend fun cleanupExpiredEntries() =
        withContext(Dispatchers.IO) {
            try {
                database.responseCacheQueries.deleteExpiredResponses()
                Log.d("PostgreSqlCache: Cleaned up expired cache entries")
            } catch (e: Exception) {
                Log.w("PostgreSqlCache: Error cleaning up expired entries: ${e.message}")
            }
        }

    override suspend fun getCacheStats(): CacheStats? =
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
                Log.w("PostgreSqlCache: Error getting cache stats: ${e.message}")
                null
            }
        }
}
