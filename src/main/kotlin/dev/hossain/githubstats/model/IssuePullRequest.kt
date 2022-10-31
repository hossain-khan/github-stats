package dev.hossain.githubstats.model

/**
 * Pull request info for an issue, when the [Issue] is a PR.
 * @see Issue
 */
class IssuePullRequest(
    /**
     * Example: "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/pulls/46973"
     */
    val url: String,
    /**
     * Example: "https://github.com/freeCodeCamp/freeCodeCamp/pull/46973"
     */
    val html_url: String,
    /**
     * Example: "https://github.com/freeCodeCamp/freeCodeCamp/pull/46973.diff"
     */
    val diff_url: String,
    /**
     * Example: "https://github.com/freeCodeCamp/freeCodeCamp/pull/46973.patch"
     */
    val patch_url: String,
    /**
     * Example: "2022-07-21T17:18:10Z"
     */
    val merged_at: String?
)
