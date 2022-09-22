package dev.hossain.githubstats.model.timeline

import dev.hossain.githubstats.model.User

data class PrMergedEvent(
    val id: Long,
    val created_at: String,
    val url: String,
    val actor: User
) : TimelineEvent
