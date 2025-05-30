package dev.hossain.time

import dev.hossain.time.UserCity.NEW_YORK
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Extension function to convert [Instant] to [LocalDateTime] at a fixed time zone.
 * NOTE: kotlinx-datetime does not have a direct ZonedDateTime equivalent.
 * This converts to LocalDateTime in the specified zone.
 */
fun Instant.toLocalDateTimeInZone(timeZone: TimeZone = Zone.city(NEW_YORK)): LocalDateTime {
    return this.toLocalDateTime(timeZone)
}

/**
 * Extension function to pretty print [Instant] for a fixed time zone.
 * This will require a KMP-compatible formatting solution for LocalDateTime.
 * For now, it will return ISO string. A more sophisticated format function will be needed.
 * Let's assume a `format()` extension on LocalDateTime will be made available,
 * possibly via expect/actual or a common formatting utility.
 */
fun Instant.format(timeZone: TimeZone = Zone.city(NEW_YORK)): String {
    val localDateTime = this.toLocalDateTime(timeZone)
    // Placeholder: Replace with actual common formatting logic for LocalDateTime
    // For example, if a common LocalDateTime.format() extension is created:
    // return localDateTime.format(somePattern)
    return localDateTime.toString() // Returns ISO 8601 format
}
