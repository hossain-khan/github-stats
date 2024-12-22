package dev.hossain.githubstats.service

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.service.GithubApiService.Companion.DEFAULT_PAGE_SIZE
import dev.hossain.githubstats.util.ErrorProcessor
import kotlinx.coroutines.delay
import kotlin.math.ceil

/**
 * GitHub issue search with paging support.
 */
class IssueSearchPagerService constructor(
    private val githubApiService: GithubApiService,
    private val errorProcessor: ErrorProcessor,
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
) {
    /**
     * Does the search API call using [GithubApiService.searchIssues] and pages to collect all results.
     */
    suspend fun searchIssues(searchQuery: String): List<Issue> {
        val allSearchedIssues = mutableListOf<Issue>()
        var pageNumber = 1

        do {
            val issueSearchResult: IssueSearchResult =
                try {
                    githubApiService.searchIssues(
                        searchQuery = searchQuery,
                        page = pageNumber,
                        size = pageSize,
                    )
                } catch (exception: Exception) {
                    val errorInfo = errorProcessor.getDetailedError(exception)
                    throw errorInfo.exception
                }

            val totalItemCount: Int = issueSearchResult.total_count
            val maxPageNeeded: Int = ceil(totalItemCount * 1.0f / pageSize * 1.0f).toInt()

            allSearchedIssues.addAll(issueSearchResult.items)

            Log.d("Loaded ${issueSearchResult.items.size} of total $totalItemCount merged PRs (Page#$pageNumber)")

            pageNumber++
            delay(BuildConfig.API_REQUEST_DELAY_MS)
        } while (pageNumber <= maxPageNeeded)

        return allSearchedIssues
    }
}
