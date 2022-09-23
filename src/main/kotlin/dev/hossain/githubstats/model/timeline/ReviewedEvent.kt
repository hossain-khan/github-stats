package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

/**
 * PR reviewed event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 */
data class ReviewedEvent(
    @Json(name = "event")
    override val eventType: String = TYPE,
    val id: Long,
    val state: ReviewState,
    val submitted_at: String,
    val user: User
) : TimelineEvent {
    companion object {
        const val TYPE = "reviewed"
    }

    /**
     * PR review states for [ReviewedEvent.state]
     */
    enum class ReviewState { approved, commented }
}
