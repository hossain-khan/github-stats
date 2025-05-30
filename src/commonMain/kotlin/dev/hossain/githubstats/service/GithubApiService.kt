package dev.hossain.githubstats.service

import dev.hossain.githubstats.model.CodeReviewComment
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.PullRequestState
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.TimelineEvent

/**
 * Expected REST API service interface for GitHub.
 * Actual implementations will use platform-specific HTTP clients (e.g., Ktor).
 */
expect interface GithubApiService {
    // Companion object can remain if constants are needed in common code,
    // but they should not be tied to Retrofit/OkHttp specifics.
    companion object {
        const val DEFAULT_PAGE_NUMBER = 1
        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_CONTRIBUTORS_PER_PAGE = 10
    }

    suspend fun pullRequest(
        owner: String,
        repo: String,
        pullNumber: Int,
    ): PullRequest

    suspend fun pullRequests(
        owner: String,
        repo: String,
        filter: String? = null,
        prState: String = PullRequestState.CLOSED.name.lowercase(), // Consider making PullRequestState KMP compatible if not already
        page: Int = DEFAULT_PAGE_NUMBER,
        size: Int = DEFAULT_PAGE_SIZE,
    ): List<PullRequest>

    suspend fun timelineEvents(
        owner: String,
        repo: String,
        issue: Int, // issue_number was the path param
        page: Int = DEFAULT_PAGE_NUMBER,
        size: Int = DEFAULT_PAGE_SIZE,
    ): List<TimelineEvent>

    suspend fun prSourceCodeReviewComments(
        owner: String,
        repo: String,
        prNumber: Int, // pull_number was the path param
        page: Int = DEFAULT_PAGE_NUMBER,
        size: Int = DEFAULT_PAGE_SIZE,
    ): List<CodeReviewComment>

    suspend fun searchIssues(
        searchQuery: String, // q was the query param, assumed to be pre-encoded if necessary by caller or actual
        sort: String = "created",
        order: String = "desc",
        page: Int = DEFAULT_PAGE_NUMBER,
        size: Int = DEFAULT_PAGE_SIZE,
    ): IssueSearchResult

    suspend fun topContributors(
        owner: String,
        repo: String,
        itemPerPage: Int = MAX_CONTRIBUTORS_PER_PAGE, // per_page was the query param
    ): List<User>
}
