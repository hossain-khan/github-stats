package dev.hossain.githubstats.model.timeline

import com.squareup.moshi.Json
import dev.hossain.githubstats.model.Team
import dev.hossain.githubstats.model.User

/**
 * PR review requested event from [Timeline](https://docs.github.com/en/rest/issues/timeline)
 *
 * Example JSON:
 * ```json
 * {
 *   "id": 7416663789,
 *   "node_id": "RRE_lADOE-ye385SKcnVzwAAAAG6EU7t",
 *   "url": "https://api.github.com/repos/opensearch-project/OpenSearch/issues/events/7416663789",
 *   "actor": {
 *     "login": "owaiskazi19",
 *     "type": "User",
 *   },
 *   "event": "review_requested",
 *   "commit_id": null,
 *   "commit_url": null,
 *   "created_at": "2022-09-19T20:10:38Z",
 *   "review_requester": {
 *     "login": "owaiskazi19",
 *     "type": "User",
 *   },
 *   "requested_team": {
 *     "name": "opensearch-core",
 *     "slug": "opensearch-core",
 *     "description": "Maintainers for OpenSearch core (OpenSearch engine) ",
 *     "privacy": "closed",
 *     "permission": "maintain",
 *   },
 *   "requested_reviewer": {
 *      "login": "reta",
 *      "type": "User",
 *    }
 *   "performed_via_github_app": null
 * }
 * ```
 */
data class ReviewRequestedEvent(
    val id: Long,
    @Json(name = "event")
    override val eventType: String = TYPE,
    val created_at: String,
    val actor: User,
    /**
     * The user requested to review the specific PR.
     * This is null when [requested_team] is used
     */
    val requested_reviewer: User?,
    /**
     * The [Team] requested to review the specific PR.
     * NOTE: This is null when [requested_reviewer] is used
     */
    val requested_team: Team?,
    val review_requester: User,
) : TimelineEvent {
    companion object {
        const val TYPE = "review_requested"
    }
}
