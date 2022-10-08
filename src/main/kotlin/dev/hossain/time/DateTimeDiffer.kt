package dev.hossain.time

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
     * ```
     *
     * NOTE: The weekends and work hours are currently non-configurable and can be found at [TemporalsExtension].
     */
    fun diffWorkingHours(startInstant: Instant, endInstant: Instant, zoneId: ZoneId): Duration {
        val startDateTime: ZonedDateTime = startInstant.toJavaInstant().atZone(zoneId)
        val endDateTime: ZonedDateTime = endInstant.toJavaInstant().atZone(zoneId)

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

            // Loop though all dates and sums up only the working hours on working day.
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
    private fun ZonedDateTime.startOfDay() = this.with(TemporalsExtension.startOfDay())
    private fun ZonedDateTime.nextWorkingDay() = this.with(TemporalsExtension.nextWorkingDay())
    private fun ZonedDateTime.nextWorkingDayOrSame() = this.with(TemporalsExtension.nextWorkingDayOrSame())
    private fun ZonedDateTime.nextWorkingHourOrSame() = this.with(TemporalsExtension.nextWorkingHourOrSame())
    private fun ZonedDateTime.nextNonWorkingHour() = this.with(TemporalsExtension.nextNonWorkingHourOrSame())

    /** Previous working start hour of the day.*/
    private fun ZonedDateTime.prevWorkingHour() = this.with(TemporalsExtension.prevWorkingHour())

    private fun ZonedDateTime.diffWith(endDateTime: ZonedDateTime): Duration {
        return java.time.Duration.between(this, endDateTime).seconds.toDuration(DurationUnit.SECONDS)
    }

    private fun ZonedDateTime.isSameDay(other: ZonedDateTime): Boolean {
        return this.year == other.year && this.month == other.month && this.dayOfMonth == other.dayOfMonth
    }

    private fun ZonedDateTime.isOnWorkingDay(): Boolean {
        val nextWorkingDayOrSame = this.nextWorkingDayOrSame()
        return this == nextWorkingDayOrSame
    }

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

    private fun ZonedDateTime.format(): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault())

        return this.format(formatter)
    }
    // endregion: Internal Extension Functions
}
