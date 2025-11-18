package dev.hossain.githubstats.client

import dev.hossain.githubstats.model.CodeReviewComment
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.PullRequestState
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.TimelineEvent

/**
 * Abstract interface for GitHub API client implementations.
 * This allows different implementations (Retrofit/OkHttp, GitHub CLI, etc.) to be used interchangeably.
 */
interface GitHubApiClient {
    companion object {
        /**
         * Initial page number for GitHub API requests.
         */
        const val DEFAULT_PAGE_NUMBER = 1

        /**
         * GitHub maximum resource item size for API requests.
         */
        const val DEFAULT_PAGE_SIZE = 100
    }

    /**
     * Lists details of a pull request by providing its number.
     *
     * https://docs.github.com/en/rest/pulls/pulls#get-a-pull-request
     */
    suspend fun pullRequest(
        owner: String,
        repo: String,
        pullNumber: Int,
    ): PullRequest

    /**
     * List pull requests
     *
     * https://docs.github.com/en/rest/pulls/pulls#list-pull-requests
     */
    suspend fun pullRequests(
        owner: String,
        repo: String,
        filter: String? = null,
        prState: String = PullRequestState.CLOSED.name.lowercase(),
        page: Int = DEFAULT_PAGE_NUMBER,
        size: Int = DEFAULT_PAGE_SIZE,
    ): List<PullRequest>

    /**
     * The Timeline events API can return different types of events triggered by timeline activity
     * in issues and pull requests.
     *
     * https://docs.github.com/en/rest/issues/timeline
     */
    suspend fun timelineEvents(
        owner: String,
        repo: String,
        issue: Int,
        page: Int = DEFAULT_PAGE_NUMBER,
        size: Int = DEFAULT_PAGE_SIZE,
    ): List<TimelineEvent>

    /**
     * Lists all review comments for a pull request.
     * By default, review comments are in ascending order by ID.
     *
     * https://docs.github.com/en/rest/pulls/comments#list-review-comments-on-a-pull-request
     */
    suspend fun prSourceCodeReviewComments(
        owner: String,
        repo: String,
        prNumber: Int,
        page: Int = DEFAULT_PAGE_NUMBER,
        size: Int = DEFAULT_PAGE_SIZE,
    ): List<CodeReviewComment>

    /**
     * Search issues and pull requests
     *
     * https://docs.github.com/en/rest/search#search-issues-and-pull-requests
     */
    suspend fun searchIssues(
        searchQuery: String,
        sort: String = "created",
        order: String = "desc",
        page: Int = DEFAULT_PAGE_NUMBER,
        size: Int = DEFAULT_PAGE_SIZE,
    ): IssueSearchResult

    /**
     * Lists contributors to the specified repository and sorts them
     * by the number of commits per contributor in descending order.
     *
     * https://docs.github.com/en/rest/repos/repos#list-repository-contributors
     */
    suspend fun topContributors(
        owner: String,
        repo: String,
        itemPerPage: Int = 10,
    ): List<User>
}
