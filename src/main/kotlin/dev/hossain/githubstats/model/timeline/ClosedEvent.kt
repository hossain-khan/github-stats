package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

/**
 * PR closed event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 */
data class ClosedEvent(
    val actor: User,
    val created_at: String,
    @Json(name = "event")
    override val eventType: String = TYPE,
    val id: Long
) : TimelineEvent {
    companion object {
        const val TYPE = "closed"
    }
}
