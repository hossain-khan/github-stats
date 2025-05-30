package dev.hossain.githubstats.model.timeline

// import com.squareup.moshi.Json // Removed
import dev.hossain.githubstats.model.User

/**
 * PR merged event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 *
 * Example JSON:
 * ```
 * {
 *   "id": 4358085088,
 *   "node_id": "MDExOk1lcmdlZEV2ZW50NDM1ODA4NTA4OA==",
 *   "url": "https://api.github.com/repos/square/moshi/issues/events/4358085088",
 *   "actor": {
 *     "login": "ZacSweers"
 *   },
 *   "event": "merged",
 *   "commit_id": "156b1f03656891d87f6b8d3bdd0fff2650fa9b8b",
 *   "commit_url": "https://api.github.com/repos/square/moshi/commits/156b1f03656891d87f6b8d3bdd0fff2650fa9b8b",
 *   "created_at": "2021-02-22T07:43:05Z",
 *   "performed_via_github_app": null
 * }
 * ```
 */
data class MergedEvent(
    val actor: User,
    val created_at: String,
    // @Json(name = "event") // Removed
    override val eventType: String = TYPE,
    val id: Long,
    val url: String,
) : TimelineEvent {
    companion object {
        const val TYPE = "merged"
    }
}
