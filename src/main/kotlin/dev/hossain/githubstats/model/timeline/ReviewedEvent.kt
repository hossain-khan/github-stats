package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

data class ReviewedEvent(
    @Json(name = "event")
    override val eventType: String = TYPE,
    val id: Long,
    val state: String,
    val submitted_at: String,
    val user: User
) : TimelineEvent {
    companion object {
        const val TYPE = "reviewed"
    }
}
