package dev.hossain.githubstats.cache

import okhttp3.Interceptor
import okhttp3.Response

/**
 * HTTP interceptor that tracks OkHttp cache performance by detecting cache hits.
 *
 * This interceptor monitors responses to determine if they were served from
 * OkHttp's internal cache or required a network request. It works by checking
 * response headers and timing information to distinguish between cached and
 * network responses.
 */
class OkHttpCacheStatsInterceptor(
    private val cacheStatsService: CacheStatsService,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Only track GitHub API requests
        if (!isGitHubApiRequest(request.url.toString())) {
            return response
        }

        // Check if this response came from OkHttp cache
        if (isFromOkHttpCache(response)) {
            cacheStatsService.recordOkHttpCacheHit()
        } else {
            // This was a network request (not from any cache layer)
            cacheStatsService.recordNetworkRequest()
        }

        return response
    }

    /**
     * Determines if the response was served from OkHttp's cache.
     *
     * OkHttp cache hits can be identified by:
     * - networkResponse is null (pure cache hit)
     * - cacheResponse is not null
     * - Specific cache-related headers
     */
    private fun isFromOkHttpCache(response: Response): Boolean {
        // If networkResponse is null, it's a pure cache hit
        if (response.networkResponse == null && response.cacheResponse != null) {
            return true
        }

        // Additional indicators of cache hits
        val cacheControl = response.header("Cache-Control")
        val age = response.header("Age")

        // If we have an Age header with cacheResponse, it's likely from cache
        return response.cacheResponse != null && !age.isNullOrEmpty()
    }

    /**
     * Checks if the request is to GitHub API.
     */
    private fun isGitHubApiRequest(url: String): Boolean = url.contains("api.github.com")
}
