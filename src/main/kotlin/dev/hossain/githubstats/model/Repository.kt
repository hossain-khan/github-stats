package dev.hossain.githubstats.model

/**
 * GitHub repository.
 * See [Repo API](https://docs.github.com/en/rest/repos/repos#get-a-repository) for more info.
 */
data class Repository(
    val id: Long,
    val name: String
)
