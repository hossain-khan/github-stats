package dev.hossain.githubstats.service

import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.Repo
import dev.hossain.githubstats.model.timeline.TimelineEvent
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * REST API service for GitHub
 *
 * See [GitHub API Browser](https://docs.github.com/en/rest)
 */
interface GithubService {
    @GET("users/{user}/repos")
    suspend fun listRepos(@Path("user") user: String): List<Repo>

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
     * The Timeline events API can return different types of events triggered by timeline activity
     * in issues and pull requests.
     *
     * https://docs.github.com/en/rest/issues/timeline
     */
    @GET("repos/{owner}/{repo}/issues/{issue_number}/timeline")
    suspend fun timelineEvents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issue: Int
    ): List<TimelineEvent>
}
