package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/**
 * Application config loader from the [LOCAL_PROPERTIES_FILE].
 */
class AppConfig constructor(localProperties: LocalProperties) {
    private val repoOwner: String = localProperties.getRepoOwner()
    private val repoId: String = localProperties.getRepoId()
    private val dateLimit: String = requireValidDate(localProperties.getDateLimit())
    private val prAuthorUserIds: List<String> = requireUser(localProperties.getAuthors())

    /**
     * Provides all available config values from [LOCAL_PROPERTIES_FILE].
     */
    fun get(): Config = Config(repoOwner, repoId, dateLimit, prAuthorUserIds)

    /**
     * Validates at least one user is provided in [LOCAL_PROPERTIES_FILE]  for the stats generations.
     */
    private fun requireUser(authors: String?): List<String> {
        requireNotNull(authors) {
            "Author/user list config is required in $LOCAL_PROPERTIES_FILE"
        }

        val users = authors.split(",")
            .filter { it.isNotEmpty() }
            .map { it.trim() }

        if (users.isEmpty()) {
            throw IllegalArgumentException(
                "You must provide at least one user name for generating " +
                    "stats as PR author or reviewer."
            )
        }

        return users
    }

    /**
     * Validates date provided in the [LOCAL_PROPERTIES_FILE] config.
     */
    private fun requireValidDate(dateStr: String?): String {
        requireNotNull(dateStr) {
            "Date limit config is required in $LOCAL_PROPERTIES_FILE"
        }

        val dateFormatter: DateTimeFormatter = DateTimeFormatter
            .ofPattern("uuuu-MM-dd", Locale.US)
            .withResolverStyle(ResolverStyle.STRICT)
        try {
            dateFormatter.parse(dateStr)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException(
                "The date '$dateStr' should be formatted like `YYYY-MM-DD`. Today is `${
                dateFormatter.format(
                    LocalDate.now()
                )
                }`."
            )
        }
        return dateStr
    }
}
