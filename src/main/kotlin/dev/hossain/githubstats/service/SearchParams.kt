package dev.hossain.githubstats.service

import java.net.URLEncoder.encode
import kotlin.text.Charsets.UTF_8

/**
 * Convenience class to search for PRs that are merged.
 *
 * See [Search API](https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests)
 * and [Search Syntax](https://docs.github.com/en/search-github/getting-started-with-searching-on-github/understanding-the-search-syntax)
 */
class SearchParams constructor(
    private val repoOwner: String,
    private val repoId: String,
    private val author: String? = null,
    private val reviewer: String? = null,
    /**
     * Lower bound of date to limit older PRs from showing.
     * Format: YYYY-MM-DD
     */
    private val dateAfter: String,
    /**
     * Upper bound of date to limit anything beyond [dateBefore].
     * Format: YYYY-MM-DD
     */
    private val dateBefore: String
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
        val query = StringBuilder()

        // Builds query based on params passed on.
        query
            .append(encode("is:closed", UTF_8))
            .append("+")
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-based-on-whether-a-pull-request-is-merged-or-unmerged
            .append(encode("is:pr", UTF_8))
            .append("+")
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-based-on-whether-a-pull-request-is-merged-or-unmerged
            .append(encode("is:merged", UTF_8))
            .append("+")
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-within-a-users-or-organizations-repositories
            .append(encode("repo:$repoOwner/$repoId", UTF_8))
            .append("+")
            // Searches issue between dates
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-when-an-issue-or-pull-request-was-created-or-last-updated
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-when-an-issue-or-pull-request-was-created-or-last-updated
            .append(encode("created:$dateAfter..$dateBefore", UTF_8))

        if (author != null) {
            query.append("+")
                // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-author
                .append(encode("author:$author", UTF_8))
        }
        if (reviewer != null) {
            query.append("+")
                // reviewed-by:USERNAME
                // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-pull-request-review-status-and-reviewer
                .append(encode("reviewed-by:$reviewer", UTF_8))
        }
        return query.toString()
    }
}
