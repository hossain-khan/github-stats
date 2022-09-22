package dev.hossain.githubstats.service

import dev.hossain.githubstats.model.Repo
import dev.hossain.githubstats.model.timeline.TimelineEvent
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubService {
    @GET("users/{user}/repos")
    suspend fun listRepos(@Path("user") user: String): List<Repo>

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
