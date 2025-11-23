package dev.hossain.githubstats.client

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.githubstats.cache.CacheStatsService
import dev.hossain.githubstats.cache.DatabaseCacheService
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.model.CodeReviewComment
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.ClosedEvent
import dev.hossain.githubstats.model.timeline.CommentedEvent
import dev.hossain.githubstats.model.timeline.MergedEvent
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.model.timeline.UnknownEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Implementation of [GitHubApiClient] using GitHub CLI (`gh` command).
 * This implementation shells out to the `gh api` command to make API requests.
 *
 * Supports optional caching through:
 * - Database cache (PostgreSQL) for persistent caching across sessions
 * - Cache statistics tracking for performance monitoring
 *
 * Prerequisites:
 * - GitHub CLI must be installed (`brew install gh` on macOS)
 * - User must be authenticated (`gh auth login`)
 *
 * @param moshi JSON serialization/deserialization engine
 * @param databaseCacheService Optional database cache service for persistent caching
 * @param cacheStatsService Optional service for tracking cache performance metrics
 *
 * @see <a href="https://cli.github.com/">GitHub CLI</a>
 * @see <a href="https://cli.github.com/manual/gh_api">gh api documentation</a>
 */
class GhCliApiClient(
    private val moshi: Moshi = createMoshi(),
    private val databaseCacheService: DatabaseCacheService? = null,
    private val cacheStatsService: CacheStatsService? = null,
) : GitHubApiClient {
    private var requestCount = 0
    private var totalRequestTime = 0L
    private var totalResponseBytes = 0L
    private var cacheHits = 0
    private var cacheMisses = 0

    init {
        val cacheStatus =
            when {
                databaseCacheService != null -> "with database caching"
                else -> "without caching"
            }
        Log.i("GhCliApiClient initialized - using GitHub CLI for API requests $cacheStatus")
    }

    companion object {
        private const val GH_COMMAND = "gh"
        private const val LOG_RESPONSE_PREVIEW_LENGTH = 500

        private fun createMoshi(): Moshi =
            Moshi
                .Builder()
                .add(
                    // Configure polymorphic adapter for TimelineEvent interface
                    // https://github.com/square/moshi/blob/master/moshi-adapters/src/main/java/com/squareup/moshi/adapters/PolymorphicJsonAdapterFactory.kt
                    PolymorphicJsonAdapterFactory
                        .of(TimelineEvent::class.java, "event")
                        .withSubtype(ClosedEvent::class.java, ClosedEvent.TYPE)
                        .withSubtype(CommentedEvent::class.java, CommentedEvent.TYPE)
                        .withSubtype(MergedEvent::class.java, MergedEvent.TYPE)
                        .withSubtype(ReadyForReviewEvent::class.java, ReadyForReviewEvent.TYPE)
                        .withSubtype(ReviewRequestedEvent::class.java, ReviewRequestedEvent.TYPE)
                        .withSubtype(ReviewedEvent::class.java, ReviewedEvent.TYPE)
                        .withDefaultValue(UnknownEvent()),
                ).addLast(KotlinJsonAdapterFactory())
                .build()

        /**
         * Checks if GitHub CLI is installed and available.
         */
        fun isGhCliAvailable(): Boolean =
            try {
                val process = ProcessBuilder("which", GH_COMMAND).start()
                val isAvailable = process.waitFor() == 0
                Log.d("GitHub CLI availability check: ${if (isAvailable) "available" else "not found"}")
                isAvailable
            } catch (e: Exception) {
                Log.w("GitHub CLI availability check failed: ${e.message}")
                false
            }
    }

    override suspend fun pullRequest(
        owner: String,
        repo: String,
        pullNumber: Int,
    ): PullRequest =
        executeGhApi(
            endpoint = "/repos/$owner/$repo/pulls/$pullNumber",
            responseType = PullRequest::class.java,
        )

    override suspend fun pullRequests(
        owner: String,
        repo: String,
        filter: String?,
        prState: String,
        page: Int,
        size: Int,
    ): List<PullRequest> {
        val params =
            buildMap {
                filter?.let { put("head", it) }
                put("state", prState)
                put("page", page.toString())
                put("per_page", size.toString())
            }

        return executeGhApiList(
            endpoint = "/repos/$owner/$repo/pulls",
            params = params,
            responseType = PullRequest::class.java,
        )
    }

    override suspend fun timelineEvents(
        owner: String,
        repo: String,
        issue: Int,
        page: Int,
        size: Int,
    ): List<TimelineEvent> {
        val params =
            mapOf(
                "page" to page.toString(),
                "per_page" to size.toString(),
            )

        return executeGhApiList(
            endpoint = "/repos/$owner/$repo/issues/$issue/timeline",
            params = params,
            responseType = TimelineEvent::class.java,
        )
    }

    override suspend fun prSourceCodeReviewComments(
        owner: String,
        repo: String,
        prNumber: Int,
        page: Int,
        size: Int,
    ): List<CodeReviewComment> {
        val params =
            mapOf(
                "page" to page.toString(),
                "per_page" to size.toString(),
            )

        return executeGhApiList(
            endpoint = "/repos/$owner/$repo/pulls/$prNumber/comments",
            params = params,
            responseType = CodeReviewComment::class.java,
        )
    }

    override suspend fun searchIssues(
        searchQuery: String,
        sort: String,
        order: String,
        page: Int,
        size: Int,
    ): IssueSearchResult {
        val params =
            mapOf(
                "q" to searchQuery,
                "sort" to sort,
                "order" to order,
                "page" to page.toString(),
                "per_page" to size.toString(),
            )

        return executeGhApi(
            endpoint = "/search/issues",
            params = params,
            responseType = IssueSearchResult::class.java,
        )
    }

    override suspend fun topContributors(
        owner: String,
        repo: String,
        itemPerPage: Int,
    ): List<User> {
        val params = mapOf("per_page" to itemPerPage.toString())

        return executeGhApiList(
            endpoint = "/repos/$owner/$repo/contributors",
            params = params,
            responseType = User::class.java,
        )
    }

    /**
     * Executes a GitHub API request using `gh api` command and parses the JSON response.
     * Checks cache first if database caching is enabled.
     */
    private suspend fun <T> executeGhApi(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        responseType: Class<T>,
    ): T =
        withContext(Dispatchers.IO) {
            requestCount++
            val requestId = requestCount

            Log.d("[$requestId] GH CLI API Request: $endpoint")
            Log.v("[$requestId] Parameters: $params")

            // Try to get from cache first
            val cacheKey = generateCacheKey(endpoint, params)
            val cachedResponse = databaseCacheService?.getCachedResponse(cacheKey)

            val jsonResponse: String
            val startTime = System.currentTimeMillis()

            if (cachedResponse != null) {
                // Cache hit
                jsonResponse = cachedResponse
                cacheHits++
                cacheStatsService?.recordDatabaseCacheHit(cacheKey)
                val duration = System.currentTimeMillis() - startTime
                Log.d("[$requestId] Response from cache in ${duration}ms (${jsonResponse.length} bytes)")
            } else {
                // Cache miss - execute command
                cacheMisses++
                cacheStatsService?.recordDatabaseCacheMiss(cacheKey)

                val command = buildGhApiCommand(endpoint, params)
                Log.v("[$requestId] Command: ${command.joinToString(" ")}")

                jsonResponse = executeCommand(command, requestId)
                val duration = System.currentTimeMillis() - startTime

                totalRequestTime += duration
                totalResponseBytes += jsonResponse.length

                Log.d("[$requestId] Response received in ${duration}ms (${jsonResponse.length} bytes)")

                // Store in cache if enabled
                databaseCacheService?.cacheResponse(cacheKey, jsonResponse)
            }

            val preview = jsonResponse.take(LOG_RESPONSE_PREVIEW_LENGTH)
            val suffix = if (jsonResponse.length > LOG_RESPONSE_PREVIEW_LENGTH) "..." else ""
            Log.v("[$requestId] Response preview: $preview$suffix")

            val adapter = moshi.adapter(responseType)
            adapter.fromJson(jsonResponse)
                ?: throw IllegalStateException("Failed to parse response for $endpoint")
        }

    /**
     * Executes a GitHub API request that returns a list/array.
     * Checks cache first if database caching is enabled.
     */
    private suspend fun <T> executeGhApiList(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        responseType: Class<T>,
    ): List<T> =
        withContext(Dispatchers.IO) {
            requestCount++
            val requestId = requestCount

            Log.d("[$requestId] GH CLI API List Request: $endpoint")
            Log.v("[$requestId] Parameters: $params")

            // Try to get from cache first
            val cacheKey = generateCacheKey(endpoint, params)
            val cachedResponse = databaseCacheService?.getCachedResponse(cacheKey)

            val jsonResponse: String
            val startTime = System.currentTimeMillis()

            if (cachedResponse != null) {
                // Cache hit
                jsonResponse = cachedResponse
                cacheHits++
                cacheStatsService?.recordDatabaseCacheHit(cacheKey)
                val duration = System.currentTimeMillis() - startTime
                Log.d("[$requestId] List response from cache in ${duration}ms (${jsonResponse.length} bytes)")
            } else {
                // Cache miss - execute command
                cacheMisses++
                cacheStatsService?.recordDatabaseCacheMiss(cacheKey)

                val command = buildGhApiCommand(endpoint, params)
                Log.v("[$requestId] Command: ${command.joinToString(" ")}")

                jsonResponse = executeCommand(command, requestId)
                val duration = System.currentTimeMillis() - startTime

                totalRequestTime += duration
                totalResponseBytes += jsonResponse.length

                Log.d("[$requestId] List response received in ${duration}ms (${jsonResponse.length} bytes)")

                // Store in cache if enabled
                databaseCacheService?.cacheResponse(cacheKey, jsonResponse)
            }

            val preview = jsonResponse.take(LOG_RESPONSE_PREVIEW_LENGTH)
            val suffix = if (jsonResponse.length > LOG_RESPONSE_PREVIEW_LENGTH) "..." else ""
            Log.v("[$requestId] Response preview: $preview$suffix")

            val listType = Types.newParameterizedType(List::class.java, responseType)
            val adapter = moshi.adapter<List<T>>(listType)
            val result =
                adapter.fromJson(jsonResponse)
                    ?: throw IllegalStateException("Failed to parse list response for $endpoint")

            Log.d("[$requestId] Parsed ${result.size} items from response")
            result
        }

    /**
     * Returns statistics about API requests made through this client.
     * Useful for debugging and performance analysis.
     */
    fun getRequestStatistics(): String {
        val avgTime = if (requestCount > 0) totalRequestTime / requestCount else 0
        val avgBytes = if (requestCount > 0) totalResponseBytes / requestCount else 0
        val totalMB = totalResponseBytes / (1024.0 * 1024.0)
        val cacheHitRate = if (requestCount > 0) (cacheHits * 100.0) / requestCount else 0.0

        return buildString {
            appendLine("GH CLI API Client Statistics:")
            appendLine("  Total Requests: $requestCount")
            if (cacheHits > 0 || cacheMisses > 0) {
                appendLine("  Cache Hits: $cacheHits")
                appendLine("  Cache Misses: $cacheMisses")
                appendLine("  Cache Hit Rate: ${"%.1f".format(cacheHitRate)}%")
            }
            appendLine("  Total Time: ${totalRequestTime}ms")
            appendLine("  Average Time: $avgTime ms per request")
            appendLine("  Total Data: ${"%.2f".format(totalMB)} MB")
            appendLine("  Average Size: $avgBytes bytes per response")
        }
    }

    /**
     * Logs the current request statistics.
     */
    fun logStatistics() {
        Log.i(getRequestStatistics())
    }

    /**
     * Generates a cache key for the given endpoint and parameters.
     * Uses the same format as GitHub API URLs for consistency with DatabaseCacheService.
     */
    private fun generateCacheKey(
        endpoint: String,
        params: Map<String, String>,
    ): String {
        val baseUrl = "https://api.github.com$endpoint"
        return if (params.isEmpty()) {
            baseUrl
        } else {
            val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            "$baseUrl?$queryString"
        }
    }

    /**
     * Builds the `gh api` command with parameters.
     * For GET requests, query parameters are appended directly to the endpoint URL.
     */
    private fun buildGhApiCommand(
        endpoint: String,
        params: Map<String, String>,
    ): List<String> {
        val endpointWithParams = if (params.isEmpty()) {
            endpoint
        } else {
            val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            "$endpoint?$queryString"
        }

        return listOf(GH_COMMAND, "api", endpointWithParams, "--method", "GET")
    }

    /**
     * Executes a shell command and returns the output as a string.
     *
     * @param command The command to execute
     * @param requestId Optional request ID for logging correlation
     */
    private fun executeCommand(
        command: List<String>,
        requestId: Int? = null,
    ): String {
        val logPrefix = if (requestId != null) "[$requestId] " else ""

        Log.v("${logPrefix}Starting process execution...")
        val processStartTime = System.currentTimeMillis()

        val process =
            ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

        val output =
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.readText()
            }

        val exitCode = process.waitFor()
        val processTime = System.currentTimeMillis() - processStartTime

        Log.v("${logPrefix}Process completed in ${processTime}ms with exit code: $exitCode")

        if (exitCode != 0) {
            Log.w("${logPrefix}gh command failed with exit code $exitCode")
            Log.w("${logPrefix}Command: ${command.joinToString(" ")}")
            Log.w("${logPrefix}Output: $output")
            throw RuntimeException("gh command failed with exit code $exitCode: $output")
        }

        return output
    }
}
