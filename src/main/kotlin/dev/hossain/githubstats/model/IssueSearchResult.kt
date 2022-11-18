package dev.hossain.githubstats.model

/**
 * Model class for GitHub issue search result.
 *
 * Example JSON:
 * ```
 * {
 *   "total_count": 2,
 *   "incomplete_results": false,
 *   "items": [{}, {}]
 * }
 * ```
 *
 * https://docs.github.com/en/rest/search#search-issues-and-pull-requests
 * @see Issue
 */
data class IssueSearchResult(
    val total_count: Int,
    val items: List<Issue>
)
