package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

/**
 * PR ready for review event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 *
 * Example JSON:
 * ```
 * {
 *   "id": 3252423176,
 *   "node_id": "MDE5OlJlYWR5Rm9yUmV2aWV3RXZlbnQzMjUyNDIzMTc2",
 *   "url": "https://api.github.com/repos/jellyfin/jellyfin/issues/events/3252423176",
 *   "actor": {
 *     "login": "JustAMan"
 *   },
 *   "event": "ready_for_review",
 *   "commit_id": null,
 *   "commit_url": null,
 *   "created_at": "2020-04-20T15:54:57Z",
 *   "performed_via_github_app": null
 * }
 * ```
 */
data class ReadyForReviewEvent(
    val actor: User,
    val created_at: String,
    @Json(name = "event")
    override val eventType: String = TYPE,
    val id: Long,
) : TimelineEvent {
    companion object {
        const val TYPE = "ready_for_review"
    }
}
