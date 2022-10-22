package dev.hossain.githubstats.service

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.service.GithubService.Companion.DEFAULT_PAGE_SIZE
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import kotlin.math.ceil

/**
 * GitHub issue search with paging support.
 */
class IssueSearchPager constructor(
    private val githubService: GithubService,
    private val pageSize: Int = DEFAULT_PAGE_SIZE
) {
    private val allSearchedIssues = mutableListOf<Issue>()
    private var pageNumber = 1

    /**
     * Does the search API call using [GithubService.searchIssues] and pages to collect all results.
     */
    suspend fun searchIssues(searchQuery: String): List<Issue> {
        do {
            val issueSearchResult: IssueSearchResult = try {
                githubService.searchIssues(
                    searchQuery = searchQuery,
                    page = pageNumber,
                    size = pageSize
                )
            } catch (exception: Exception) {
                throw IllegalStateException(getErrorMessage(exception), exception)
            }

            val totalItemCount: Int = issueSearchResult.total_count
            val maxPageNeeded: Int = ceil(totalItemCount * 1.0f / pageSize * 1.0f).toInt()

            allSearchedIssues.addAll(issueSearchResult.items)

            if (BuildConfig.DEBUG) {
                println("Loaded ${issueSearchResult.items.size} of total $totalItemCount merged PRs (Page#$pageNumber)")
            }

            pageNumber++
            delay(BuildConfig.API_REQUEST_DELAY_MS)
        } while (pageNumber <= maxPageNeeded)

        return allSearchedIssues
    }

    /**
     * Provides bit more verbose error message to help understand the error.
     */
    private fun getErrorMessage(exception: Exception): String {
        if (exception is HttpException) {
            val response: Response<*>? = exception.response()
            val error: ResponseBody? = response?.errorBody()
            val message: String = exception.message ?: "HTTP Error ${exception.code()}"

            if (error != null) {
                return "$message - ${error.string()}"
            }
            return message
        } else {
            return exception.message ?: exception.toString()
        }
    }
}
