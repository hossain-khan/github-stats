package dev.hossain.githubstats.model.timeline

import dev.hossain.githubstats.model.User

data class ReadyForReviewEvent(
    val actor: User,
    val created_at: String,
    val event: String,
    val id: Long
) : TimelineEvent
