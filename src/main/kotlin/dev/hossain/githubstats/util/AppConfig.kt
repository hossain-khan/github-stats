package dev.hossain.githubstats.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

class AppConfig constructor(localProperties: LocalProperties) {
    private val repoOwner: String = localProperties.getRepoOwner()
    private val repoId: String = localProperties.getRepoId()
    private val dateLimit: String = requireValidDate(localProperties.getDateLimit())
    private val prAuthorUserIds = localProperties.getAuthors().split(",")
        .filter { it.isNotEmpty() }
        .map { it.trim() }

    fun get(): Config = Config(repoOwner, repoId, dateLimit, prAuthorUserIds)

    private fun requireValidDate(dateStr: String): String {
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
