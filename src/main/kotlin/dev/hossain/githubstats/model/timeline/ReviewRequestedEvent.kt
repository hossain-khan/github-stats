package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

@Json
data class ReviewRequestedEvent(
    val id: Long,
    val event: String,
    val created_at: String,
    val actor: User,
    val requested_reviewer: User,
    val review_requester: User
) : TimelineEvent
