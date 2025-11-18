package dev.hossain.githubstats.client

import dev.hossain.githubstats.model.CodeReviewComment
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.service.GithubApiService

/**
 * Implementation of [GitHubApiClient] using Retrofit/OkHttp.
 * This is the existing implementation that uses HTTP client libraries.
 */
class RetrofitApiClient(
    private val githubApiService: GithubApiService,
) : GitHubApiClient {
    override suspend fun pullRequest(
        owner: String,
        repo: String,
        pullNumber: Int,
    ): PullRequest =
        githubApiService.pullRequest(
            owner = owner,
            repo = repo,
            pullNumber = pullNumber,
        )

    override suspend fun pullRequests(
        owner: String,
        repo: String,
        filter: String?,
        prState: String,
        page: Int,
        size: Int,
    ): List<PullRequest> =
        githubApiService.pullRequests(
            owner = owner,
            repo = repo,
            filter = filter,
            prState = prState,
            page = page,
            size = size,
        )

    override suspend fun timelineEvents(
        owner: String,
        repo: String,
        issue: Int,
        page: Int,
        size: Int,
    ): List<TimelineEvent> =
        githubApiService.timelineEvents(
            owner = owner,
            repo = repo,
            issue = issue,
            page = page,
            size = size,
        )

    override suspend fun prSourceCodeReviewComments(
        owner: String,
        repo: String,
        prNumber: Int,
        page: Int,
        size: Int,
    ): List<CodeReviewComment> =
        githubApiService.prSourceCodeReviewComments(
            owner = owner,
            repo = repo,
            prNumber = prNumber,
            page = page,
            size = size,
        )

    override suspend fun searchIssues(
        searchQuery: String,
        sort: String,
        order: String,
        page: Int,
        size: Int,
    ): IssueSearchResult =
        githubApiService.searchIssues(
            searchQuery = searchQuery,
            sort = sort,
            order = order,
            page = page,
            size = size,
        )

    override suspend fun topContributors(
        owner: String,
        repo: String,
        itemPerPage: Int,
    ): List<User> =
        githubApiService.topContributors(
            owner = owner,
            repo = repo,
            itemPerPage = itemPerPage,
        )
}
