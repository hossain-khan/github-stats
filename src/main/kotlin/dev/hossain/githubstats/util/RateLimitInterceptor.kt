package dev.hossain.githubstats.util

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that captures GitHub API rate limit headers for smart rate limiting.
 */
class RateLimitInterceptor : Interceptor {
    private var lastRateLimitHeaders: Map<String, String>? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        // Capture rate limit headers from successful responses
        if (response.isSuccessful) {
            val headers = mutableMapOf<String, String>()
            response.header("x-ratelimit-remaining")?.let { headers["x-ratelimit-remaining"] = it }
            response.header("x-ratelimit-limit")?.let { headers["x-ratelimit-limit"] = it }
            response.header("x-ratelimit-reset")?.let { headers["x-ratelimit-reset"] = it }
            response.header("x-ratelimit-resource")?.let { headers["x-ratelimit-resource"] = it }
            
            if (headers.isNotEmpty()) {
                lastRateLimitHeaders = headers
            }
        }
        
        return response
    }
    
    /**
     * Get the last captured rate limit headers.
     */
    fun getLastRateLimitHeaders(): Map<String, String>? = lastRateLimitHeaders
    
    /**
     * Get the remaining requests from the last captured headers.
     */
    fun getRemainingRequests(): Int? = lastRateLimitHeaders?.get("x-ratelimit-remaining")?.toIntOrNull()
    
    /**
     * Get the rate limit reset time from the last captured headers.
     */
    fun getRateLimitReset(): Long? = lastRateLimitHeaders?.get("x-ratelimit-reset")?.toLongOrNull()
    
    /**
     * Get the rate limit total from the last captured headers.
     */
    fun getRateLimit(): Int? = lastRateLimitHeaders?.get("x-ratelimit-limit")?.toIntOrNull()
}