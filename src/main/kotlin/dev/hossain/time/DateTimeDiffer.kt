package dev.hossain.time

import dev.hossain.time.DateTimeDiffer.isOnWorkingDay
import dev.hossain.time.DateTimeDiffer.isSameDay
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Contains utility function to diff date-time.
 */
object DateTimeDiffer {
    /**
     * Provides working hours between two instants on given [ZoneId].
     * It adds all the working hours between `9:00AM` and `5:00PM` excluding weekends (Saturday and Sunday).
     *
     * ```
     *   +------+------+------+------+------+------+------+
     *   |      |      |      |      |      |      |      |
     *   |  M   |  T   |  W   |  T   |  F   |  S   |  S   |
     *   |      |      |      |      |      |      |      |
     *   +------+------+------+------+------+------+------+
     *      ↑                           ↑
     *    Week                         Week
     *    Start                        End
     *
     * ┌──────┐  ┌────┐  ┌────┐  ┌────┐  ┌────┐  ┌────┐  ┌────┐  ┌──────┐
     * │ 9 AM ├──┤ .. │──┤ .. │──┤ .. │──┤ .. │──┤ .. │──┤ .. │──┤ 5 PM │
     * └──────┘  └────┘  └────┘  └────┘  └────┘  └────┘  └────┘  └──────┘
     *    ↑                                                         ↑
     *  Start                                                      End
     *  of Day                                                    of Day
     * ```
     *
     * NOTE: The weekends and work hours are currently non-configurable and can be found at [TemporalsExtension].
     */
    fun diffWorkingHours(
        startInstant: Instant,
        endInstant: Instant,
        timeZoneId: ZoneId
    ): Duration {
        val startDateTime: ZonedDateTime = startInstant.toJavaInstant().atZone(timeZoneId)
        val endDateTime: ZonedDateTime = endInstant.toJavaInstant().atZone(timeZoneId)

        if (endDateTime.isBefore(startDateTime)) {
            throw IllegalArgumentException("The end time $endInstant is before $startInstant.")
        }

        when {
            startDateTime.isSameDay(endDateTime) &&
                startDateTime.isOnWorkingDay() &&
                endDateTime.isOnWorkingDay() -> {
                // Both start and end times are working day. Consider single day calculation.
                // Future Note: this logic ideally could also be merged with multi-day calculation
                return workingDuration(startDateTime, endDateTime)
            }

            startDateTime.isOnWorkingDay().not() &&
                endDateTime.isOnWorkingDay().not() &&
                (endInstant - startInstant < Duration.parse("2d")) -> {
                // Silly logic to check if both start and end time happened in one weekend
                return Duration.ZERO
            }

            // Loop through all dates and sums up only the working hours on working day.
            else -> {
                var workingHours = Duration.ZERO
                var previousWorkingDay =
                    if (startDateTime.isAfterWorkingHour()) startDateTime.nextWorkingHourOrSame() else startDateTime
                var immediateNextWorkingDay = previousWorkingDay.nextNonWorkingHour()

                while (immediateNextWorkingDay.isBefore(endDateTime) && !immediateNextWorkingDay.isSameDay(endDateTime)) {
                    if (previousWorkingDay.isSameDay(immediateNextWorkingDay) &&
                        previousWorkingDay.isOnWorkingDay().not()
                    ) {
                        // Skip calculating weekends
                        previousWorkingDay = previousWorkingDay.nextWorkingDay().prevWorkingHour()
                        immediateNextWorkingDay = immediateNextWorkingDay.nextWorkingDay()
                        continue
                    }

                    val workingHoursToday = workingDuration(previousWorkingDay, immediateNextWorkingDay)
                    workingHours = workingHours.plus(workingHoursToday)

                    previousWorkingDay = previousWorkingDay.nextWorkingDay().prevWorkingHour()
                    immediateNextWorkingDay = immediateNextWorkingDay.nextWorkingDay()
                }

                // Finally calculate the last day
                if (endDateTime.isOnWorkingDay()) {
                    workingHours = workingHours.plus(workingDuration(endDateTime.startOfDay(), endDateTime))
                }
                return workingHours
            }
        }
    }

    /**
     * Provides working day work hour duration between to working dates denoted by [startDateTime] and [endDateTime].
     */
    private fun workingDuration(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): Duration {
        val startToEndDiff = startDateTime.diffWith(endDateTime)
        when {
            startDateTime.isWithinWorkingHour() && endDateTime.isWithinWorkingHour() -> {
                // Both start and end time is within working hour
                // No after hours to subtract - return normal diff
                return startToEndDiff
            }

            // Alternatively, check if both of the start and end time is outside working hours
            (startDateTime.isWithinWorkingHour() || endDateTime.isWithinWorkingHour()).not() -> {
                return if ((startDateTime.isBeforeWorkingHour() && endDateTime.isBeforeWorkingHour()) ||
                    startDateTime.isAfterWorkingHour() && endDateTime.isAfterWorkingHour()
                ) {
                    // Both start and end time was before/after working hour. Make the diff almost zero.
                    Duration.ZERO // Kudos, you get bonus point
                } else {
                    // That means, the start hour is before working hour,
                    // and the end hour is after working hour.
                    Duration.parse("8h") // Full day
                }
            }

            startDateTime.isWithinWorkingHour().not() -> {
                return startToEndDiff - (startDateTime.diffWith(endDateTime.prevWorkingHour()))
            }

            endDateTime.isWithinWorkingHour().not() -> {
                return startToEndDiff - (startDateTime.nextNonWorkingHour().diffWith(endDateTime))
            }

            else -> {
                return startToEndDiff
            }
        }
    }

    // region: Internal Extension Functions
    /**
     * Provides date-time set to 12:00 AM of the same time-time provided.
     * Example:
     *  - Current Date Time: Saturday 11 AM --> Saturday 12 AM (Reset to 12:00 am)
     *  - Current Date Time: Sunday 11 AM   --> Sunday 12 AM (Reset to 12:00 am)
     *  - Current Date Time: Monday 2 PM    --> Monday 12 AM (Reset to 12:00 am)
     */
    private fun ZonedDateTime.startOfDay() = this.with(TemporalsExtension.startOfDay())

    /**
     * Provides next working day for current date-time.
     * Example:
     *  - Current Date Time: Saturday 11 AM --> Monday 11 AM (nex working day - excluding weekends)
     *  - Current Date Time: Sunday 11 AM   --> Monday 11 AM (nex working day - excluding weekends)
     *  - Current Date Time: Monday 11 AM   --> Tuesday 11 AM (nex working day)
     */
    private fun ZonedDateTime.nextWorkingDay() = this.with(TemporalsExtension.nextWorkingDay())

    /**
     * Provides next working day for current date-time or same day if current date-time is already working day.
     * Example:
     *  - Current Date Time: Saturday 11 AM --> Monday 11 AM (nex working day - excluding weekends)
     *  - Current Date Time: Sunday 11 AM   --> Monday 11 AM (nex working day - excluding weekends)
     *  - Current Date Time: Monday 11 AM   --> Monday 11 AM (Same day - as it monday is working day)
     */
    private fun ZonedDateTime.nextWorkingDayOrSame() = this.with(TemporalsExtension.nextWorkingDayOrSame())

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
    private fun ZonedDateTime.nextWorkingHourOrSame() = this.with(TemporalsExtension.nextWorkingHourOrSame())

    private fun ZonedDateTime.nextNonWorkingHour() = this.with(TemporalsExtension.nextNonWorkingHourOrSame())

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
    private fun ZonedDateTime.prevWorkingHour() = this.with(TemporalsExtension.prevWorkingHour())

    private fun ZonedDateTime.diffWith(endDateTime: ZonedDateTime): Duration {
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
    private fun ZonedDateTime.isSameDay(other: ZonedDateTime): Boolean {
        return this.year == other.year && this.month == other.month && this.dayOfMonth == other.dayOfMonth
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
    private fun ZonedDateTime.isOnWorkingDay(): Boolean {
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
     * - Thursday, September 22, 2022 at 5:54:25 PM EDT = true [TODO: This should be `false`]
     * - Wednesday, February 21, 2018 at 5:55:35 PM EST = true [TODO: This should be `false`]
     */
    private fun ZonedDateTime.isWithinWorkingHour(): Boolean {
        val nonWorkingHour = this.nextWorkingHourOrSame()
        return this == nonWorkingHour
    }

    private fun ZonedDateTime.isBeforeWorkingHour(): Boolean {
        val nextWorkingHourOrSame = this.nextWorkingHourOrSame()
        val prevWorkingHour = this.prevWorkingHour()
        if (nextWorkingHourOrSame == this) {
            return false
        }

        // If the previous working our is previous day, then must be before 9 am
        return prevWorkingHour.isSameDay(this).not()
    }

    private fun ZonedDateTime.isAfterWorkingHour(): Boolean {
        val nextWorkingHourOrSame = this.nextWorkingHourOrSame()
        if (nextWorkingHourOrSame == this) {
            return false
        }

        // If the next working our is next day, then must be after 5pm
        return nextWorkingHourOrSame.isSameDay(this).not()
    }

    /**
     * Formats the [ZonedDateTime] with formatter to more human-readable time.
     */
    private fun ZonedDateTime.format(): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault())

        return this.format(formatter)
    }
    // endregion: Internal Extension Functions
}
