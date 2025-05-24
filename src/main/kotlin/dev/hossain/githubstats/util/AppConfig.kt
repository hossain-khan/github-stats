package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.time.UserCity
import dev.hossain.time.UserTimeZone
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale
import java.util.ResourceBundle

/**
 * Application config loader from the [LOCAL_PROPERTIES_FILE].
 *
 * @see UserTimeZone.userZones
 * @see UserCity
 */
class AppConfig constructor(
    localProperties: LocalProperties,
) {
    private val bundle = ResourceBundle.getBundle("strings", Locale.getDefault())
    private val repoOwner: String = localProperties.getRepoOwner()
    private val repoId: String = localProperties.getRepoId()
    private val dateLimitAfter: String = requireValidDate(localProperties.getDateLimitAfter())
    private val dateLimitBefore: String = requiredValidDateOrDefault(localProperties.getDateLimitBefore())
    private val prAuthorUserIds: List<String> = requireUser(localProperties.getAuthors())
    private val botUserIds: List<String> = localProperties.getBotUsers()?.let { extractUserIds(it) } ?: emptyList()

    /**
     * Provides all available config values from [LOCAL_PROPERTIES_FILE].
     */
    fun get(): Config = Config(repoOwner, repoId, prAuthorUserIds, botUserIds, dateLimitAfter, dateLimitBefore)

    /**
     * Validates at least one user is provided in [LOCAL_PROPERTIES_FILE]  for the stats generations.
     */
    private fun requireUser(authors: String?): List<String> {
        requireNotNull(authors) {
            String.format(bundle.getString("app_config_author_list_required"), LOCAL_PROPERTIES_FILE)
        }

        val users = extractUserIds(authors)

        if (users.isEmpty()) {
            throw IllegalArgumentException(bundle.getString("app_config_at_least_one_user_required"))
        }

        return users
    }

    /**
     * Extracts user IDs from the comma separated string.
     */
    private fun extractUserIds(userIds: String): List<String> =
        userIds
            .split(",")
            .filter { it.isNotEmpty() }
            .map { it.trim() }

    /**
     * Validates optional date provided in the [LOCAL_PROPERTIES_FILE] config,
     * or defaults to today's date.
     */
    private fun requiredValidDateOrDefault(dateText: String?): String {
        val dateFormatter: DateTimeFormatter =
            DateTimeFormatter
                .ofPattern("uuuu-MM-dd", Locale.US)
                .withResolverStyle(ResolverStyle.STRICT)

        if (dateText.isNullOrBlank()) {
            val todayDate = dateFormatter.format(LocalDate.now())
            validateDate(todayDate)
            return todayDate
        }
        validateDate(dateText)
        return dateText
    }

    /**
     * Validates required date provided in the [LOCAL_PROPERTIES_FILE] config.
     */
    private fun requireValidDate(dateText: String?): String {
        requireNotNull(dateText) {
            String.format(bundle.getString("app_config_date_limit_required"), LOCAL_PROPERTIES_FILE)
        }
        validateDate(dateText)
        return dateText
    }

    /**
     * Validates date format defined in the [LOCAL_PROPERTIES_FILE] config.
     */
    private fun validateDate(dateText: String) {
        val dateFormatter: DateTimeFormatter =
            DateTimeFormatter
                .ofPattern("uuuu-MM-dd", Locale.US)
                .withResolverStyle(ResolverStyle.STRICT)

        try {
            dateFormatter.parse(dateText)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException(
                String.format(
                    bundle.getString("app_config_invalid_date_format"),
                    dateText,
                    dateFormatter.format(LocalDate.now()),
                ),
            )
        }
    }
}
