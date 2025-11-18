package dev.hossain.githubstats.client

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.githubstats.model.CodeReviewComment
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.TimelineEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Implementation of [GitHubApiClient] using GitHub CLI (`gh` command).
 * This implementation shells out to the `gh api` command to make API requests.
 *
 * Prerequisites:
 * - GitHub CLI must be installed (`brew install gh` on macOS)
 * - User must be authenticated (`gh auth login`)
 *
 * @see <a href="https://cli.github.com/">GitHub CLI</a>
 * @see <a href="https://cli.github.com/manual/gh_api">gh api documentation</a>
 */
class GhCliApiClient(
    private val moshi: Moshi = createMoshi(),
) : GitHubApiClient {
    companion object {
        private const val GH_COMMAND = "gh"

        private fun createMoshi(): Moshi =
            Moshi
                .Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

        /**
         * Checks if GitHub CLI is installed and available.
         */
        fun isGhCliAvailable(): Boolean =
            try {
                val process = ProcessBuilder("which", GH_COMMAND).start()
                process.waitFor() == 0
            } catch (e: Exception) {
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
     */
    private suspend fun <T> executeGhApi(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        responseType: Class<T>,
    ): T =
        withContext(Dispatchers.IO) {
            val command = buildGhApiCommand(endpoint, params)
            val jsonResponse = executeCommand(command)

            val adapter = moshi.adapter(responseType)
            adapter.fromJson(jsonResponse)
                ?: throw IllegalStateException("Failed to parse response for $endpoint")
        }

    /**
     * Executes a GitHub API request that returns a list/array.
     */
    private suspend fun <T> executeGhApiList(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        responseType: Class<T>,
    ): List<T> =
        withContext(Dispatchers.IO) {
            val command = buildGhApiCommand(endpoint, params)
            val jsonResponse = executeCommand(command)

            val listType = Types.newParameterizedType(List::class.java, responseType)
            val adapter = moshi.adapter<List<T>>(listType)
            adapter.fromJson(jsonResponse)
                ?: throw IllegalStateException("Failed to parse list response for $endpoint")
        }

    /**
     * Builds the `gh api` command with parameters.
     */
    private fun buildGhApiCommand(
        endpoint: String,
        params: Map<String, String>,
    ): List<String> {
        val command = mutableListOf(GH_COMMAND, "api", endpoint, "--method", "GET")

        params.forEach { (key, value) ->
            command.add("-F")
            command.add("$key=$value")
        }

        return command
    }

    /**
     * Executes a shell command and returns the output as a string.
     */
    private fun executeCommand(command: List<String>): String {
        val process =
            ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

        val output =
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.readText()
            }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw RuntimeException("gh command failed with exit code $exitCode: $output")
        }

        return output
    }
}
