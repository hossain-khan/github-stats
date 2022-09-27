package dev.hossain.githubstats.service

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.model.Issue
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.service.GithubService.Companion.DEFAULT_PAGE_SIZE
import kotlinx.coroutines.delay
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
            val issueSearchResult: IssueSearchResult = githubService.searchIssues(
                searchQuery = searchQuery,
                page = pageNumber,
                size = pageSize
            )
            val totalItemCount: Int = issueSearchResult.total_count
            val maxPageNeeded: Int = ceil(totalItemCount * 1.0f / pageSize * 1.0f).toInt()

            allSearchedIssues.addAll(issueSearchResult.items)

            pageNumber++
            delay(BuildConfig.API_REQUEST_DELAY_MS)
        } while (pageNumber <= maxPageNeeded)

        return allSearchedIssues
    }
}
