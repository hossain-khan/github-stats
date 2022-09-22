package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

@Json
data class ReviewRequestedEvent(
    val id: Int,
    val actor: User,
    val event: String,
    val created_at: String
) : TimelineEvent
