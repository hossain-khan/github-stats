package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.User

/**
 * PR closed event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 *
 * Example JSON
 * ```
 * {
 *   "id": 6892809849,
 *   "node_id": "CE_lADOAAKNBs5KO0kmzwAAAAGa1-55",
 *   "url": "https://api.github.com/repos/jquery/jquery/issues/events/6892809849",
 *   "actor": {
 *     "login": "mgol"
 *   },
 *   "event": "closed",
 *   "commit_id": null,
 *   "commit_url": null,
 *   "created_at": "2022-06-28T10:39:02Z",
 *   "state_reason": null,
 *   "performed_via_github_app": null
 * }
 * ```
 */
data class ClosedEvent(
    val actor: User,
    val created_at: String,
    @Json(name = "event")
    override val eventType: String = TYPE,
    val id: Long,
) : TimelineEvent {
    companion object {
        const val TYPE = "closed"
    }
}
