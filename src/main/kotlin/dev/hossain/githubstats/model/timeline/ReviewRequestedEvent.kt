package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

data class ReviewRequestedEvent(
    val id: Long,
    @Json(name = "event")
    override val eventType: String = TYPE,
    val created_at: String,
    val actor: User,
    val requested_reviewer: User,
    val review_requester: User
) : TimelineEvent {
    companion object {
        const val TYPE = "review_requested"
    }
}
