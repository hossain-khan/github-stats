package dev.hossain.githubstats.util

import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.platform.ExpectedPropertiesReader
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDate
// Remove unused formatter imports
// import kotlinx.datetime.format.char
// import kotlinx.datetime.format.LocalDateComponentFormatter
// import kotlinx.datetime.format.MonthNames
// import kotlinx.datetime.format.Padding
// import kotlinx.datetime.format.byUnicodePattern
// import kotlinx.datetime.format.DayOfWeekNames


/**
 * Application config loader from the [LOCAL_PROPERTIES_FILE].
 * Uses [ExpectedPropertiesReader] for platform-agnostic property loading.
 */
class AppConfig constructor(
    propertiesReader: ExpectedPropertiesReader,
) {
    private val repoOwner: String = propertiesReader.getRepoOwner()
    private val repoId: String = propertiesReader.getRepoId()
    private val dateLimitAfter: String = requireValidDate(propertiesReader.getDateLimitAfter())
    private val dateLimitBefore: String = requiredValidDateOrDefault(propertiesReader.getDateLimitBefore())
    private val prAuthorUserIds: List<String> = requireUser(propertiesReader.getAuthors())
    private val botUserIds: List<String> = propertiesReader.getBotUsers()?.let { extractUserIds(it) } ?: emptyList()

    /**
     * Provides all available config values.
     */
    fun get(): Config = Config(repoOwner, repoId, prAuthorUserIds, botUserIds, dateLimitAfter, dateLimitBefore)

    private fun requireUser(authors: String?): List<String> {
        requireNotNull(authors) {
            "Author/user list config is required in $LOCAL_PROPERTIES_FILE"
        }

        val users = extractUserIds(authors)

        if (users.isEmpty()) {
            throw IllegalArgumentException(
                "You must provide at least one user name for generating stats as PR author or reviewer.",
            )
        }
        return users
    }

    private fun extractUserIds(userIds: String): List<String> =
        userIds
            .split(",")
            .filter { it.isNotEmpty() }
            .map { it.trim() }

    // Using kotlinx-datetime an ISO 8601 date format YYYY-MM-DD
    // For example: LocalDate.Format { year(); char('-'); monthNumber(); char('-'); dayOfMonth() }
    // For strict parsing, we primarily rely on kotlinx-datetime's default ISO parser.
    // Further strictness can be emulated by checking components if needed.
    // private val commonDateFormat by lazy { // This is no longer used
    //     LocalDate.Format {
    //         year()
    //         char('-')
    //         monthNumber(Padding.ZERO)
    //         char('-')
    //         dayOfMonth(Padding.ZERO)
    //     }
    // }


    private fun requiredValidDateOrDefault(dateText: String?): String {
        if (dateText.isNullOrBlank()) {
            val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
            // No specific formatter needed for default ISO format with kotlinx-datetime's `toString()`
            val todayDate = today.toString() // Default format is YYYY-MM-DD
            validateDate(todayDate) // Should be valid by default
            return todayDate
        }
        validateDate(dateText)
        return dateText
    }

    private fun requireValidDate(dateText: String?): String {
        requireNotNull(dateText) {
            "Date limit config is required in $LOCAL_PROPERTIES_FILE"
        }
        validateDate(dateText)
        return dateText
    }

    private fun validateDate(dateText: String) {
        try {
            // kotlinx-datetime's parse method for LocalDate defaults to ISO_LOCAL_DATE (YYYY-MM-DD)
            // and is generally strict.
            LocalDate.parse(dateText)
            // If more specific pattern validation like "uuuu-MM-dd" with ResolverStyle.STRICT is needed,
            // it's typically handled by ensuring the input string matches the exact pattern and component ranges.
            // kotlinx-datetime's default parser is quite strict for ISO formats.
            // For example, it won't parse "2023-1-1" (expects "2023-01-01").
            // It also validates month and day ranges.
            // "uuuu" (year) vs "yyyy" (year-of-era) differences are less common in ISO date context.
            // kotlinx-datetime uses proleptic Gregorian calendar like java.time.
        } catch (e: IllegalArgumentException) { // Catches format errors from parse
            val today = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())
            throw IllegalArgumentException(
                "The date '$dateText' should be formatted like `YYYY-MM-DD`. Today is `${today}`.",
                e,
            )
        }
    }
}
