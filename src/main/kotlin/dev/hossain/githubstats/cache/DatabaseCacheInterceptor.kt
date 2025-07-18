package dev.hossain.githubstats.cache

import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * HTTP interceptor that adds database-based caching to GitHub API requests.
 *
 * This interceptor works alongside OkHttp's built-in caching by providing
 * an additional layer of persistent database caching for JSON responses.
 * It will attempt to serve responses from the database cache before making
 * network requests, and cache successful responses for future use.
 */
class DatabaseCacheInterceptor(
    private val cacheService: DatabaseCacheService,
    private val cacheStatsService: CacheStatsService,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Only cache GET requests to GitHub API
        if (request.method != "GET" || !isGitHubApiRequest(url)) {
            return chain.proceed(request)
        }

        try {
            // Try to serve from database cache first
            val cachedResponse = runBlocking { cacheService.getCachedResponse(url) }
            if (cachedResponse != null) {
                Log.d("DatabaseCacheInterceptor: Serving from database cache: $url")
                cacheStatsService.recordDatabaseCacheHit()
                return createCachedResponse(request, cachedResponse)
            }

            // Cache miss - record it and proceed with network request
            cacheStatsService.recordDatabaseCacheMiss()
            val response = chain.proceed(request)

            // Cache successful responses
            if (response.isSuccessful && isJsonResponse(response)) {
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    runBlocking {
                        cacheService.cacheResponse(url, responseBody, response.code)
                    }

                    // Return response with the body we read
                    return response
                        .newBuilder()
                        .body(responseBody.toResponseBody(response.body?.contentType()))
                        .build()
                }
            }

            return response
        } catch (e: Exception) {
            Log.w("DatabaseCacheInterceptor: Error in cache handling for $url: ${e.message}")
            // Fall back to normal request processing
            return chain.proceed(request)
        }
    }

    /**
     * Creates a cached response from stored JSON data.
     */
    private fun createCachedResponse(
        request: okhttp3.Request,
        cachedJson: String,
    ): Response =
        Response
            .Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(200)
            .message("OK (from database cache)")
            .body(cachedJson.toResponseBody())
            .header("X-Cached-By", "GitHubStats-DatabaseCache")
            .build()

    /**
     * Checks if the request is to GitHub API and should be cached.
     */
    private fun isGitHubApiRequest(url: String): Boolean = url.contains("api.github.com")

    /**
     * Checks if the response contains JSON content that should be cached.
     */
    private fun isJsonResponse(response: Response): Boolean {
        val contentType = response.header("Content-Type") ?: ""
        return contentType.contains("application/json", ignoreCase = true)
    }
}
