package dev.hossain.githubstats.model

import com.squareup.moshi.Json

@Json
data class PullRequest(
    val id: Long,
    val state: String,
    val title: String,
    val url: String,
    val html_url: String,
    val user: User,
    val created_at: String,
    val updated_at: String?,
    val closed_at: String?,
    val merged_at: String?
)
