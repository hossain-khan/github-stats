package dev.hossain.githubstats.model

import com.squareup.moshi.Json

enum class PullRequestState {
    @Json(name = "open")
    OPEN,

    @Json(name = "closed")
    CLOSED,

    @Json(name = "all")
    ALL
}