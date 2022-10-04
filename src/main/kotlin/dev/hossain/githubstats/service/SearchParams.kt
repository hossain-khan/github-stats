package dev.hossain.githubstats.service

import java.net.URLEncoder.encode
import kotlin.text.Charsets.UTF_8

/**
 * Convenience class to search for PRs that are merged.
 *
 * See [Search API](https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests).
 */
class SearchParams constructor(
    private val repoOwner: String,
    private val repoId: String,
    private val author: String? = null,
    private val reviewer: String? = null,
    /**
     * Lower bound of date to limit older PRs from showing.
     * TODO - Make this configurable value as well
     */
    private val dateAfter: String = "2022-01-01"
) {
    /**
     * Provides search query for the [GithubService.searchIssues] API.
     * Example search query:
     * - `is:pr+repo:owner/repoid+author:userlogin+created:>2021-01-01`
     * - `is%3Apr+is%3Aclosed+author%3ADanielRosa74`
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
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-when-an-issue-or-pull-request-was-created-or-last-updated
            .append(encode("created:>$dateAfter", UTF_8))

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
