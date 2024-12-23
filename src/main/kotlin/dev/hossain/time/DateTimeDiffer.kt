package dev.hossain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration

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
        timeZoneId: ZoneId,
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

            startDateTime.isNextDay(endDateTime) &&
                startDateTime.isOnWorkingDay() &&
                endDateTime.isOnWorkingDay().not() -> {
                // Case where next day is weekend, just provide difference between working hours
                return workingDuration(startDateTime, endDateTime.prevWorkingHour())
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
                    when {
                        //startDateTime.isOnWorkingDay().not() -> startDateTime.nextWorkingDay().prevWorkingHour()
                        startDateTime.isAfterWorkingHour() -> startDateTime.nextWorkingHourOrSame()
                        else -> startDateTime
                    }
                var immediateNextWorkingDay = previousWorkingDay.nextNonWorkingHour()

                // Debug date time used in the calculation - Keep it commented out in production
                /*println("startDateTime           = ${startDateTime.format()},\n" +
                        "endDateTime             = ${endDateTime.format()},\n" +
                        "previousWorkingDay      = ${previousWorkingDay.format()},\n" +
                        "immediateNextWorkingDay = ${immediateNextWorkingDay.format()},\n" +
                        "immediateNextWorkingDay.isBefore(endDateTime)         = ${immediateNextWorkingDay.isBefore(endDateTime)},\n" +
                        "!immediateNextWorkingDay.isSameDay(endDateTime)       = ${!immediateNextWorkingDay.isSameDay(endDateTime)},\n" +
                        "previousWorkingDay.isSameDay(immediateNextWorkingDay) = ${previousWorkingDay.isSameDay(immediateNextWorkingDay)},\n" +
                        "previousWorkingDay.isOnWorkingDay().not()             = ${previousWorkingDay.isOnWorkingDay().not()},\n" +
                        "\n")*/

                // Loop through the dates while `immediateNextWorkingDay` is before end date and is not same day
                while (immediateNextWorkingDay.isBefore(endDateTime) && !immediateNextWorkingDay.isSameDay(endDateTime)) {
                    if (previousWorkingDay.isSameDay(immediateNextWorkingDay) &&
                        previousWorkingDay.isOnWorkingDay().not()
                    ) {
                        // Skip calculating weekends - just move to next working day that is required to calculate working hours
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
     * Provides working day work hour duration between two working dates denoted by [startDateTime] and [endDateTime].
     *
     * NOTE: This function assumes that both [startDateTime] and [endDateTime] are on working days.
     * If either of the date-time is not on a working day, it throws an [IllegalArgumentException].
     */
    private fun workingDuration(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime,
    ): Duration {
        // Checks if both start and end date-times are on working days.
        // Throws an IllegalArgumentException if either of the date-times is not on a working day.
        if ((startDateTime.isOnWorkingDay() && endDateTime.isOnWorkingDay()).not()) {
            throw IllegalArgumentException("This function can only handle working day diff. " +
                "Start: $startDateTime, End: $endDateTime - both must be on working day.")
        }

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
                    startDateTime.isAfterWorkingHour() &&
                    endDateTime.isAfterWorkingHour()
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
}
