package dev.hossain.githubstats.model.timeline

// import com.squareup.moshi.Json // Removed
import dev.hossain.githubstats.model.User

/**
 * PR reviewed event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 *
 * Example JSON:
 * ```json
 * {
 *  "id": 99064550,
 *  "node_id": "MDE3OlB1bGxSZXF1ZXN0UmV2aWV3OTkwNjQ1NTA=",
 *  "user": {
 *    "login": "yschimke",
 *  },
 *  "body": null,
 *  "commit_id": "e2292c9b1d465794aeb0202da13a63f468ce8e79",
 *  "submitted_at": "2018-02-23T21:54:27Z",
 *  "state": "commented",
 *  "html_url": "https://github.com/square/okhttp/pull/3873#pullrequestreview-99064550",
 *  "pull_request_url": "https://api.github.com/repos/square/okhttp/pulls/3873",
 *  "author_association": "COLLABORATOR",
 *  "event": "reviewed"
 *}
 * ```
 */
data class ReviewedEvent(
    // @Json(name = "event") // Removed
    override val eventType: String = TYPE,
    val id: Long,
    val state: ReviewState, // Enum ReviewState also needs @Json removed
    val submitted_at: String,
    val html_url: String,
    val user: User,
) : TimelineEvent {
    companion object {
        const val TYPE = "reviewed"
    }

    /**
     * PR review states for [ReviewedEvent.state]
     */
    enum class ReviewState {
        // @Json(name = "approved") // Removed
        APPROVED,

        // @Json(name = "changes_requested") // Removed
        CHANGE_REQUESTED,

        // @Json(name = "commented") // Removed
        COMMENTED,

        // @Json(name = "dismissed") // Removed
        DISMISSED,
    }

    override fun toString(): String = "Reviewed (${state.name.lowercase()}) by `${user.login}` at $html_url"
}
