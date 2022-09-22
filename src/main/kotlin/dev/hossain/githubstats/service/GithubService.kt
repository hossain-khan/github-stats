package dev.hossain.githubstats.service

import dev.hossain.githubstats.model.Repo
import retrofit2.http.GET
import retrofit2.http.Path

interface GithubService {
    @GET("users/{user}/repos")
    suspend fun listRepos(@Path("user") user: String) : List<Repo>

    suspend fun timeline()
}
