package dev.hossain.githubstats.model

/**
 * Pull request info for an issue, when the [Issue] is a PR.
 *
 * Example JSON:
 * ```
 * {
 *  "url": "https://api.github.com/repos/jellyfin/jellyfin-web/pulls/1011",
 *  "html_url": "https://github.com/jellyfin/jellyfin-web/pull/1011",
 *  "diff_url": "https://github.com/jellyfin/jellyfin-web/pull/1011.diff",
 *  "patch_url": "https://github.com/jellyfin/jellyfin-web/pull/1011.patch",
 *  "merged_at": "2020-05-27T12:08:28Z"
 *}
 * ```
 * @see Issue
 */
class IssuePullRequest(
    val url: String,
    val html_url: String,
    val diff_url: String,
    val patch_url: String,
    val merged_at: String?
)
