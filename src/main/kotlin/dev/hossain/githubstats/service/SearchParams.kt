package dev.hossain.githubstats.service

import java.net.URLEncoder
import kotlin.text.Charsets

/**
 * Convenience class to search for PRs that are merged.
 *
 * See [Search API](https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests)
 * and [Search Syntax](https://docs.github.com/en/search-github/getting-started-with-searching-on-github/understanding-the-search-syntax)
 */
data class SearchParams(
    val repoOwner: String,
    val repoId: String,
    val author: String? = null,
    val reviewer: String? = null,
    /**
     * Lower bound of date to limit older PRs from showing.
     * Format: YYYY-MM-DD
     */
    val dateAfter: String,
    /**
     * Upper bound of date to limit anything beyond [dateBefore].
     * Format: YYYY-MM-DD
     */
    val dateBefore: String,
) {
    /**
     * Provides search query for the [GithubApiService.searchIssues] API.
     * Example search query:
     * - `is%3Aclosed+is%3Apr+is%3Amerged+repo%3AfreeCodeCamp%2FfreeCodeCamp+created%3A%3E2022-09-11+reviewed-by%3ADanielRosa74`
     * - `is:closed+is:pr+is:merged+repo:freeCodeCamp/freeCodeCamp+created:>2022-09-11+reviewed-by:DanielRosa74`
     * - `is:pr+repo:owner/repoid+author:userlogin+created:>2021-01-01`
     * - `is%3Apr+is%3Aclosed+author%3ADanielRosa74`
     * - `is:pr+is:closed+is:merged+author:swankjesse+created:2022-01-01..2022-03-01`
     */
    fun toQuery(): String {
        // Builds query based on params passed on.
        // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-based-on-whether-a-pull-request-is-merged-or-unmerged
        // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-within-a-users-or-organizations-repositories
        // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-when-an-issue-or-pull-request-was-created-or-last-updated
        var query = "${"is:closed".encodeUrl()}+" +
            "${"is:pr".encodeUrl()}+" +
            "${"is:merged".encodeUrl()}+" +
            "${"repo:$repoOwner/$repoId".encodeUrl()}+" +
            "created:$dateAfter..$dateBefore".encodeUrl()

        if (author != null) {
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-author
            query += "+${"author:$author".encodeUrl()}"
        }
        if (reviewer != null) {
            // reviewed-by:USERNAME
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-pull-request-review-status-and-reviewer
            query += "+${"reviewed-by:$reviewer".encodeUrl()}"
        }
        return query
    }
}

/**
 * Extension function to URL encode a string using UTF-8 charset.
 */
private fun String.encodeUrl(): String {
    return URLEncoder.encode(this, Charsets.UTF_8.name())
}
