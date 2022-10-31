package dev.hossain.githubstats.model

/**
 * Model class for GitHub issue search result.
 *
 * https://docs.github.com/en/rest/search#search-issues-and-pull-requests
 * @see Issue
 */
data class IssueSearchResult(
    val total_count: Int,
    val items: List<Issue>
)
