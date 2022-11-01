package dev.hossain.githubstats.model

/**
 * Team for PR review.
 * Groups of organization members that gives permissions on specified repositories.
 *
 * Example JSON:
 * ```json
 * {
 *   "name": "opensearch-core",
 *   "id": 4615246,
 *   "node_id": "MDQ6VGVhbTQ2MTUyNDY=",
 *   "slug": "opensearch-core",
 *   "description": "Maintainers for OpenSearch core (OpenSearch engine) ",
 *   "privacy": "closed",
 *   "url": "https://api.github.com/organizations/80134844/team/4615246",
 *   "html_url": "https://github.com/orgs/opensearch-project/teams/opensearch-core",
 *   "members_url": "https://api.github.com/organizations/80134844/team/4615246/members{/member}",
 *   "repositories_url": "https://api.github.com/organizations/80134844/team/4615246/repos",
 *   "permission": "maintain",
 *   "permissions": {
 *     "admin": false,
 *     "maintain": true,
 *     "push": true,
 *     "triage": true,
 *     "pull": true
 *   },
 *   "parent": null
 * }
 * ```
 */
data class Team(
    val id: Long,
    /**
     * Name of the team (used for display).
     * For example: `"Justice League"`
     */
    val name: String,
    /**
     * Team slug is the team used in URL, and could be used as unique ID.
     * For example: `"justice-league"`
     */
    val slug: String,
    /**
     * Description of the team
     */
    val description: String,
    /**
     * Permission that the team will have for its repositories
     */
    val permission: String
)
