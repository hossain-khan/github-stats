package dev.hossain.githubstats.ui.model

import java.time.LocalDate

/**
 * UI model representing the configuration settings for the GitHub PR Stats app.
 */
data class ConfigUiModel(
    val accessToken: String = "",
    val repoOwner: String = "",
    val repoId: String = "",
    val authors: String = "",
    val botUsers: String = "",
    val dateAfter: LocalDate = LocalDate.now().minusMonths(1),
    val dateBefore: LocalDate = LocalDate.now(),
    val isTokenValid: Boolean = false
)