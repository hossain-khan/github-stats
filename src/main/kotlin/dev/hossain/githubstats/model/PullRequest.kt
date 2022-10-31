package dev.hossain.githubstats.model

/**
 * A GitHub PR (Pull Request).
 * Pull requests let you tell others about changes you've pushed to a repository on GitHub.
 * Once a pull request is sent, interested parties can review the set of changes,
 * discuss potential modifications, and even push follow-up commits if necessary.
 */
data class PullRequest(
    val id: Long,
    /**
     * PR number.
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
    val merged_at: String?
)
