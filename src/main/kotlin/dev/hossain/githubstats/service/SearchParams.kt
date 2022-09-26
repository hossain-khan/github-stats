package dev.hossain.githubstats.service

import java.net.URLEncoder.encode
import kotlin.text.Charsets.UTF_8

/**
 * Convenience class to search for PRs.
 *
 * See [Search API](https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests).
 */
class SearchParams constructor(
    private val repoOwner: String,
    private val repoId: String,
    private val author: String
) {
    /**
     * Provides search query for the [GithubService.searchIssues] API.
     * Example search query:
     * - `is:pr+repo:owner/repoid+author:userlogin`
     * - `is%3Apr+is%3Aclosed+author%3ADanielRosa74`
     */
    fun toQuery(): String {
        return encode("is:closed", UTF_8) +
            "+" +
            encode("is:pr", UTF_8) +
            "+" +
            encode("$repoOwner/$repoId", UTF_8) +
            "+" +
            encode("author:$author", UTF_8)
    }
}
