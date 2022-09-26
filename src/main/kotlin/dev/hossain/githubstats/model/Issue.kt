package dev.hossain.githubstats.model

data class Issue(
    val id: Long,
    /**
     * Issue or PR number.
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
