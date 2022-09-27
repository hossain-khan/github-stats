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
    private val author: String,
    /**
     * Lower bound of date to limit older PRs from showing.
     */
    private val dateAfter: String = "2021-01-01"
) {
    /**
     * Provides search query for the [GithubService.searchIssues] API.
     * Example search query:
     * - `is:pr+repo:owner/repoid+author:userlogin+created:>2021-01-01`
     * - `is%3Apr+is%3Aclosed+author%3ADanielRosa74`
     */
    fun toQuery(): String {
        return encode("is:closed", UTF_8) +
            "+" +
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-based-on-whether-a-pull-request-is-merged-or-unmerged
            encode("is:pr", UTF_8) +
            "+" +
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-based-on-whether-a-pull-request-is-merged-or-unmerged
            encode("is:merged", UTF_8) +
            "+" +
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-within-a-users-or-organizations-repositories
            encode("repo:$repoOwner/$repoId", UTF_8) +
            "+" +
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-author
            encode("author:$author", UTF_8) +
            "+" +
            // https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests#search-by-when-an-issue-or-pull-request-was-created-or-last-updated
            encode("created:>$dateAfter", UTF_8)
    }
}
