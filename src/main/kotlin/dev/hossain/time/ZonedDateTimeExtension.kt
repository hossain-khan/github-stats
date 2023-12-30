package dev.hossain.time

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Provides date-time set to 12:00 AM of the same time-time provided.
 * Example:
 *  - Current Date Time: Saturday 11 AM --> Saturday 12 AM (Reset to 12:00 am)
 *  - Current Date Time: Sunday 11 AM   --> Sunday 12 AM (Reset to 12:00 am)
 *  - Current Date Time: Monday 2 PM    --> Monday 12 AM (Reset to 12:00 am)
 *  - Current Date Time: Tuesday 2:43:05 AM --> Tuesday 12:00:00 AM
 *  - Current Date Time: Wednesday 10:37:39 AM --> Wednesday 12:00:00 AM
 */
internal fun ZonedDateTime.startOfDay() = this.with(TemporalsExtension.startOfDay())

/**
 * Provides next working day for current date-time.
 * Example:
 *  - Current Date Time: Saturday 11 AM --> Monday 11 AM (nex working day - excluding weekends)
 *  - Current Date Time: Sunday 11 AM   --> Monday 11 AM (nex working day - excluding weekends)
 *  - Current Date Time: Monday 11 AM   --> Tuesday 11 AM (nex working day)
 */
internal fun ZonedDateTime.nextWorkingDay() = this.with(TemporalsExtension.nextWorkingDay())

/**
 * Provides next working day for current date-time or same day if current date-time is already working day.
 * Example:
 *  - Current Date Time: Saturday 11 AM --> Monday 11 AM (nex working day - excluding weekends)
 *  - Current Date Time: Sunday 11 AM   --> Monday 11 AM (nex working day - excluding weekends)
 *  - Current Date Time: Monday 11 AM   --> Monday 11 AM (Same day - as it monday is working day)
 */
internal fun ZonedDateTime.nextWorkingDayOrSame() = this.with(TemporalsExtension.nextWorkingDayOrSame())

/**
 * Provides immediate start of working hour for working day, or same time if it's on weekend.
 *
 * Example:
 *  - Current Date Time: Saturday 11 AM --> Saturday 11 AM (Same - because on non-weekday)
 *  - Current Date Time: Sunday 11 AM   --> Sunday 11 AM (Same - because on non-weekday)
 *  - Current Date Time: Monday 11 AM   --> Monday 11 AM (Same - because it's already in working hour)
 *  - Current Date Time: Tuesday 8 PM   --> Wednesday 9 AM (Next day, because it was after hours)
 *  - Current Date Time: Tuesday 6 AM   --> Tuesday 9 AM (Same day but time changed to start of work)
 */
internal fun ZonedDateTime.nextWorkingHourOrSame() = this.with(TemporalsExtension.nextWorkingHourOrSame())

internal fun ZonedDateTime.nextNonWorkingHour() = this.with(TemporalsExtension.nextNonWorkingHourOrSame())

/**
 * Previous working start hour of the day irrespective of weekday or weekends.
 *
 * Example:
 *  - Current Date Time: Saturday 11 AM --> Saturday 9 AM
 *  - Current Date Time: Sunday 2 PM    --> Sunday 9 AM
 *  - Current Date Time: Monday 11 AM   --> Monday 9 AM
 *  - Current Date Time: Tuesday 8 PM   --> Tuesday 5 PM (End of the day for same day)
 *  - Current Date Time: Tuesday 6 AM   --> Monday 5 PM (End of the day for previous day)
 */
internal fun ZonedDateTime.prevWorkingHour() = this.with(TemporalsExtension.prevWorkingHour())

internal fun ZonedDateTime.diffWith(endDateTime: ZonedDateTime): Duration {
    return java.time.Duration.between(this, endDateTime).seconds.toDuration(DurationUnit.SECONDS)
}

/**
 * Checks if two [ZonedDateTime] are in same day (ignores time zone).
 *
 * Example:
 * - Monday, May 23, 2022 at 5:02:33 PM EDT <-> and
 *   Monday, June 27, 2022 at 12:28:16 PM EDT isSameDay = false
 * - Thursday, September 22, 2022 at 5:10:46 PM EDT <-> and
 *   Friday, September 23, 2022 at 9:14:25 AM EDT isSameDay = false
 * - Wednesday, September 21, 2022 at 2:39:31 PM EDT <-> and
 *   Thursday, September 22, 2022 at 5:54:25 PM EDT isSameDay = false
 * - Monday, May 23, 2022 at 12:02:33 PM EDT <-> and
 *   Monday, May 23, 2022 at 5:02:33 PM EDT isSameDay = true
 * - Tuesday, May 24, 2022 at 9:00:33 AM EDT <-> and
 *   Tuesday, May 24, 2022 at 5:02:33 PM EDT isSameDay = true
 * - Friday, June 24, 2022 at 9:00:33 AM EDT <-> and
 *   Friday, June 24, 2022 at 5:02:33 PM EDT isSameDay = true
 */
internal fun ZonedDateTime.isSameDay(other: ZonedDateTime): Boolean {
    return this.year == other.year && this.month == other.month && this.dayOfMonth == other.dayOfMonth
}

/**
 * Checks if date-time is next day of the [other] provided [ZonedDateTime].
 */
internal fun ZonedDateTime.isNextDay(other: ZonedDateTime): Boolean {
    val nextDayOfStartDateTime = this.plusDays(1)
    return nextDayOfStartDateTime.year == other.year &&
        nextDayOfStartDateTime.month == other.month &&
        nextDayOfStartDateTime.dayOfMonth == other.dayOfMonth
}

/**
 * Checks if given date time is on working day.
 *
 * Example:
 * - Sunday, February 25, 2018 at 12:31:04 PM = false
 * - Saturday, February 24, 2018 at 3:10:49 PM = false
 * - Sunday, February 25, 2018 at 12:31:04 PM = false
 * - Friday, February 23, 2018 at 9:00:35 AM = true
 * - Friday, September 23, 2022 at 8:33:21 AM = true
 * - Thursday, September 22, 2022 at 12:10:46 PM = true
 */
internal fun ZonedDateTime.isOnWorkingDay(): Boolean {
    val nextWorkingDayOrSame = this.nextWorkingDayOrSame()
    return this == nextWorkingDayOrSame
}

/**
 * Checks if given date time is in working hour.
 *
 * Example:
 * - Monday, June 27, 2022 at 12:28:16 AM EDT = false
 * - Friday, September 23, 2022 at 8:33:21 AM EDT = false
 * - Tuesday, September 13, 2022 at 8:11:30 AM EDT = false
 * - Monday, December 16, 2019 at 8:49:13 PM EST = false
 * - Tuesday, September 13, 2022 at 1:21:51 PM EDT = true
 * - Wednesday, September 21, 2022 at 11:52:30 AM EDT = true
 * - Thursday, September 22, 2022 at 5:54:25 PM EDT = false
 * - Wednesday, February 21, 2018 at 5:55:35 PM EST = false
 */
internal fun ZonedDateTime.isWithinWorkingHour(): Boolean {
    val nonWorkingHour = this.nextWorkingHourOrSame()
    return this == nonWorkingHour
}

/**
 * Checks if the current ZonedDateTime is before the start of the working hour.
 * The working hour is considered to start at 9 AM.
 *
 * @return Boolean value indicating whether the current time is before the start of the working hour.
 * If the next working hour is the same as the current time, it returns false.
 * If the previous working hour is on the previous day, it means the current time is before 9 AM, so it returns true.
 */
internal fun ZonedDateTime.isBeforeWorkingHour(): Boolean {
    val nextWorkingHourOrSame = this.nextWorkingHourOrSame()
    val prevWorkingHour = this.prevWorkingHour()
    if (nextWorkingHourOrSame == this) {
        return false
    }

    // If the previous working our is previous day, then must be before 9 am
    return prevWorkingHour.isSameDay(this).not()
}

/**
 * Checks if the current ZonedDateTime is after the end of the working hour.
 * The working hour is considered to end at 5 PM.
 *
 * @return Boolean value indicating whether the current time is after the end of the working hour.
 * If the next working hour is the same as the current time, it returns false.
 * If the next working hour is on the next day, it means the current time is after 5 PM, so it returns true.
 */
internal fun ZonedDateTime.isAfterWorkingHour(): Boolean {
    val nextWorkingHourOrSame = this.nextWorkingHourOrSame()
    if (nextWorkingHourOrSame == this) {
        return false
    }

    // If the next working our is next day, then must be after 5pm
    return nextWorkingHourOrSame.isSameDay(this).not()
}

/**
 * Formats the ZonedDateTime to a more human-readable format.
 *
 * @return A string representing the formatted date and time.
 * The format used is the full localized date-time format for the US locale.
 */
internal fun ZonedDateTime.format(): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
        .withLocale(Locale.US)
        .withZone(zone)

    return this.format(formatter)
}
