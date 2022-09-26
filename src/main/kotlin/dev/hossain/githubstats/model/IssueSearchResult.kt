package dev.hossain.githubstats.model

data class IssueSearchResult(
    val total_count: Int,
    val items: List<Issue>
)
