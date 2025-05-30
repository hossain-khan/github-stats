package dev.hossain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Duration

// NOTE: The original ZonedDateTimeExtension.kt contained complex business logic
// for determining working days/hours using java.time.TemporalAdjuster.
// That logic is now expected to be part of the `actual` implementations of
// the `expect` functions defined in `ExpectedTemporals.kt`.
// These extension functions will now call those `expect` functions.
// The `TimeZone` parameter is crucial for applying these adjustments correctly.

internal fun Instant.startOfDay(timeZone: TimeZone): Instant = this.applyStartOfDay(timeZone)

internal fun Instant.nextWorkingDay(timeZone: TimeZone): Instant = this.applyNextWorkingDay(timeZone)

internal fun Instant.nextWorkingDayOrSame(timeZone: TimeZone): Instant = this.applyNextWorkingDayOrSame(timeZone)

internal fun Instant.nextWorkingHourOrSame(timeZone: TimeZone): Instant = this.applyNextWorkingHourOrSame(timeZone)

internal fun Instant.nextNonWorkingHour(timeZone: TimeZone): Instant = this.applyNextNonWorkingHourOrSame(timeZone)

internal fun Instant.prevWorkingHour(timeZone: TimeZone): Instant = this.applyPrevWorkingHour(timeZone)

/**
 * Calculates the duration between two Instants.
 */
internal fun Instant.diffWith(endDateTime: Instant): Duration = endDateTime - this

/**
 * Checks if two [Instant] objects fall on the same calendar day in a given [timeZone].
 */
internal fun Instant.isSameDay(
    other: Instant,
    timeZone: TimeZone,
): Boolean {
    val thisLocalDateTime = this.toLocalDateTime(timeZone)
    val otherLocalDateTime = other.toLocalDateTime(timeZone)
    return thisLocalDateTime.date == otherLocalDateTime.date
}

/**
 * Checks if this [Instant] is on the next calendar day compared to the [other] Instant in a given [timeZone].
 */
internal fun Instant.isNextDay(
    other: Instant,
    timeZone: TimeZone,
): Boolean {
    val thisLocalDateTime = this.toLocalDateTime(timeZone)
    // It's more robust to check if other.date + 1 day == this.date
    // However, kotlinx-datetime doesn't have direct date arithmetic like that easily.
    // Let's convert `other` to its date, add one day, and compare with `this` date.
    val otherDatePlusOneDay = other.toLocalDateTime(timeZone).date.plus(1, DateTimeUnit.DAY)
    return thisLocalDateTime.date == otherDatePlusOneDay
}

/**
 * Checks if the given [Instant] is on a working day in the specified [timeZone].
 * This relies on the `actual` implementation of `applyNextWorkingDayOrSame`.
 */
internal fun Instant.isOnWorkingDay(timeZone: TimeZone): Boolean {
    // If applying nextWorkingDayOrSame doesn't change the day, it's a working day.
    // This requires careful implementation of applyNextWorkingDayOrSame.
    // A simpler check might be if (instant == instant.applyNextWorkingDayOrSame(timeZone))
    // but that only works if time component is also preserved or irrelevant.
    // For now, let's assume the logic holds: if it's a working day, it remains the same day.
    val nextWorkingInstant = this.applyNextWorkingDayOrSame(timeZone)
    return this.isSameDay(nextWorkingInstant, timeZone)
}

/**
 * Checks if the given [Instant] is within working hours in the specified [timeZone].
 * This relies on the `actual` implementation of `applyNextWorkingHourOrSame`.
 */
internal fun Instant.isWithinWorkingHour(timeZone: TimeZone): Boolean {
    // If applying nextWorkingHourOrSame doesn't change the instant, it's within working hours.
    return this == this.applyNextWorkingHourOrSame(timeZone)
}

/**
 * Checks if the current [Instant] is before the start of the working hour in the specified [timeZone].
 * Relies on `applyNextWorkingHourOrSame` and `applyPrevWorkingHour`.
 */
internal fun Instant.isBeforeWorkingHour(timeZone: TimeZone): Boolean {
    val nextWorkingHourOrSame = this.applyNextWorkingHourOrSame(timeZone)
    // If current time is not a working hour start, and the next working hour start is later today,
    // or if current time IS a working hour start but previous working hour was on a different day.
    if (this != nextWorkingHourOrSame) { // Not currently at a working hour start
        // If next working hour is on the same day, then we are before it.
        // If next working hour is on a different day, this logic might be complex.
        // Let's use the original logic's spirit:
        // If the previous working hour's end was on a different day, then we are before 9 AM.
        val prevWorkingHourEnd = this.applyPrevWorkingHour(timeZone) // This expect should give start of prev slot
        // This logic needs to be very carefully translated in actuals.
        // Simplified: if this instant is earlier than the start of its own working day slot.
        return this < this.applyStartOfDay(timeZone).applyNextWorkingHourOrSame(timeZone)
    }
    return false // It is a working hour start.
}

/**
 * Checks if the current [Instant] is after the end of the working hour in the specified [timeZone].
 * Relies on `applyNextWorkingHourOrSame`.
 */
internal fun Instant.isAfterWorkingHour(timeZone: TimeZone): Boolean {
    val nextWorkingHourOrSame = this.applyNextWorkingHourOrSame(timeZone)
    if (this == nextWorkingHourOrSame) { // Currently within a working slot or at its start
        return false
    }
    // If this is not a working hour, and the next working hour is on a *different* day,
    // it implies we are past today's working hours.
    return !this.isSameDay(nextWorkingHourOrSame, timeZone)
}

/**
 * Formats the [Instant] to a string representation.
 * KMP formatting is basic. For rich localized formatting, use `expect` actual.
 * This currently returns the ISO string representation in the given timeZone.
 */
internal fun Instant.format(timeZone: TimeZone): String {
    // kotlinx.datetime.format is experimental, for robust formatting, expect/actual is better.
    // For now, just use default toString of LocalDateTime in the zone.
    return this.toLocalDateTime(timeZone).toString()
}

// Note: The original `format()` used DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
// This rich, locale-sensitive formatting is not directly available in kotlinx-datetime common.
// An expect/actual mechanism would be needed for such formatting.
