package dev.hossain.githubstats.model.timeline

// import com.squareup.moshi.Json // Removed
import dev.hossain.githubstats.model.User

/**
 * PR Commented event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 *
 * Sample JSON
 * ```json
 * {
 *   "url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/issues/comments/1285382597",
 *   "html_url": "https://github.com/freeCodeCamp/freeCodeCamp/pull/48149#issuecomment-1285382597",
 *   "issue_url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/issues/48149",
 *   "id": 1285382597,
 *   "node_id": "IC_kwDOAbI7X85MnWHF",
 *   "user": {
 *     "login": "ojeytonwilliams",
 *     "id": 15801806,
 *     "type": "User",
 *     "site_admin": false
 *   },
 *   "created_at": "2022-10-20T11:42:19Z",
 *   "updated_at": "2022-10-20T11:42:19Z",
 *   "author_association": "CONTRIBUTOR",
 *   "body": "@raisedadead in that case would we only see there was a problem if we checked https://github.com/freeCodeCamp/freeCodeCamp/actions ?",
 *   "reactions": {
 *     "url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/issues/comments/1285382597/reactions",
 *     "total_count": 0
 *   },
 *   "performed_via_github_app": null,
 *   "event": "commented",
 *   "actor": {
 *     "login": "ojeytonwilliams",
 *     "id": 15801806,
 *     "node_id": "MDQ6VXNlcjE1ODAxODA2",
 *     "type": "User",
 *     "site_admin": false
 *   }
 * }
 * ```
 */
data class CommentedEvent(
    val url: String,
    val html_url: String,
    val actor: User,
    val user: User,
    val body: String,
    val created_at: String,
    val updated_at: String,
    // @Json(name = "event") // Removed
    override val eventType: String = TYPE,
) : TimelineEvent {
    companion object {
        const val TYPE = "commented"
    }

    override fun toString(): String = "Commented by ${actor.login} at $html_url"
}
