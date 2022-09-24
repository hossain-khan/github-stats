package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

/**
 * PR review requested event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 */
data class ReviewRequestedEvent(
    val id: Long,
    @Json(name = "event")
    override val eventType: String = TYPE,
    val created_at: String,
    val actor: User,
    /**
     * This is null when [requested_team] is used
     */
    val requested_reviewer: User?,
    val review_requester: User
) : TimelineEvent {
    companion object {
        const val TYPE = "review_requested"
    }
}
