package dev.hossain.githubstats.service

import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.PullRequestState
import dev.hossain.githubstats.model.Repository
import dev.hossain.githubstats.model.timeline.TimelineEvent
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * REST API service for GitHub
 *
 * See [GitHub API Browser](https://docs.github.com/en/rest)
 */
interface GithubService {
    companion object {
        private const val DEFAULT_PAGE_NUMBER = 1
        private const val DEFAULT_PAGE_SIZE = 100
    }

    @GET("users/{user}/repos")
    suspend fun listRepos(@Path("user") user: String): List<Repository>

    /**
     * Lists details of a pull request by providing its number.
     *
     * https://docs.github.com/en/rest/pulls/pulls#get-a-pull-request
     */
    @GET("/repos/{owner}/{repo}/pulls/{pull_number}")
    suspend fun pullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int
    ): PullRequest

    /**
     * List pull requests
     *
     * https://docs.github.com/en/rest/pulls/pulls#list-pull-requests
     */
    @GET("/repos/{owner}/{repo}/pulls")
    suspend fun pullRequests(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        /**
         * Filter pulls by head user or head organization and branch name
         * in the format of `user:ref-name` or `organization:ref-name`.
         * For example: `github:new-script-format` or `octocat:test-branch`.
         */
        @Query("head") filter: String? = null,
        @Query("state") prState: String = PullRequestState.CLOSED.name.lowercase(),
        @Query("page") page: Int = DEFAULT_PAGE_NUMBER,
        @Query("per_page") size: Int = DEFAULT_PAGE_SIZE
    ): List<PullRequest>

    /**
     * The Timeline events API can return different types of events triggered by timeline activity
     * in issues and pull requests.
     *
     * https://docs.github.com/en/rest/issues/timeline
     */
    @GET("repos/{owner}/{repo}/issues/{issue_number}/timeline")
    suspend fun timelineEvents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issue: Int,
        @Query("page") page: Int = DEFAULT_PAGE_NUMBER,
        @Query("per_page") size: Int = DEFAULT_PAGE_SIZE
    ): List<TimelineEvent>
}
