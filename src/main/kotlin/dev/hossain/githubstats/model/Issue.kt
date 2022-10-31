package dev.hossain.githubstats.model

/**
 * GitHub issue - that can be regular issue or pull-request.
 */
data class Issue(
    val id: Long,
    /**
     * Issue or PR number.
     * Number uniquely identifying the pull request within its repository.
     */
    val number: Int,
    val state: String,
    val title: String,
    val url: String,
    val html_url: String,
    val user: User,
    val merged: Boolean?,
    val created_at: String,
    val updated_at: String?,
    val closed_at: String?,
    val pull_request: IssuePullRequest?
)
