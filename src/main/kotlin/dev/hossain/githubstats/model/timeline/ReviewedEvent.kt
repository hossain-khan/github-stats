package dev.hossain.githubstats.model.timeline

import dev.hossain.githubstats.model.User

data class ReviewedEvent(
    val event: String,
    val id: Long,
    val state: String,
    val submitted_at: String,
    val user: User
) : TimelineEvent
