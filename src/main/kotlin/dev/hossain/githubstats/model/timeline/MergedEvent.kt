package dev.hossain.githubstats.model.timeline

import dev.hossain.githubstats.model.User

data class MergedEvent(
    val actor: User,
    val created_at: String,
    val id: Long,
    val url: String
) : TimelineEvent
