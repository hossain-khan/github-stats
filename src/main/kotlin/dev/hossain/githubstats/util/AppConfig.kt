package dev.hossain.githubstats.util

class AppConfig constructor(localProperties: LocalProperties) {
    private val repoOwner: String = localProperties.getRepoOwner()
    private val repoId: String = localProperties.getRepoId()
    private val dateLimit: String = localProperties.getDateLimit()
    private val prAuthorUserIds = localProperties.getAuthors().split(",")
        .filter { it.isNotEmpty() }
        .map { it.trim() }

    fun get(): Config = Config(repoOwner, repoId, dateLimit, prAuthorUserIds)
}
