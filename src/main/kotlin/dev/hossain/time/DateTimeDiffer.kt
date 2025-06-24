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
     * Calculates the total working duration between two given [Instant]s, considering a specific [ZoneId].
     *
     * This function defines working hours as **9:00 AM to 5:00 PM** on weekdays.
     * Weekends are defined as **Saturday and Sunday**. These are currently non-configurable.
     * The calculation excludes time outside of these working hours and any time on weekends.
     *
     * > ⚠️ WARNING ⚠️: This is an overly complex function. Do not try to decipher its internal logic directly.
     * > It handles various edge cases, such as start or end times falling outside working hours or on weekends.
     *
     * Visual representation of a working week:
     * ```
     *   +------+------+------+------+------+------+------+
     *   |      |      |      |      |      |      |      |
     *   |  M   |  T   |  W   |  T   |  F   |  S   |  S   |
     *   |      |      |      |      |      |      |      | <- Weekdays (Working Days)
     *   +------+------+------+------+------+------+------+
     *      ↑                           ↑              ↑
     *    Week                        Week            Weekend
     *    Start                        End             (Non-Working Days)
     * ```
     *
     * For details on how working days, working hours, and weekends are defined,
     * see the extension functions in [TemporalsExtension].
     *
     * @param startInstant The start instant of the period.
     * @param endInstant The end instant of the period.
     * @param timeZoneId The ZoneId to determine local working days and hours.
     * @return The total working duration between the two instants, excluding non-working hours and weekends.
     * @throws IllegalArgumentException if [endInstant] is before [startInstant].
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
                // Both start and end times are on the same working day.
                // TODO: Consider merging single-day calculation logic with multi-day calculation for simplification.
                return workingDuration(startDateTime, endDateTime)
            }

            startDateTime.isNextDay(endDateTime) &&
                startDateTime.isOnWorkingDay() &&
                endDateTime.isOnWorkingDay().not() -> {
                // Start is on a working day, and end is the next day, which is not a working day (e.g., Friday to Saturday).
                // Calculate working hours only up to the end of the working day for `startDateTime`.
                return workingDuration(startDateTime, endDateTime.prevWorkingHour())
            }

            startDateTime.isOnWorkingDay().not() &&
                endDateTime.isOnWorkingDay().not() &&
                (endInstant - startInstant < Duration.parse("2d")) -> {
                // Both start and end times are on non-working days, and the duration is less than 2 days.
                // This typically means the period falls entirely within a single weekend.
                return Duration.ZERO
            }

            // Handles multi-day calculations, including periods that span across weekends.
            else -> {
                var workingHours = Duration.ZERO
                // Initialize `previousWorkingDay` to the start of the working period, adjusted to the next working hour if currently after hours.
                var previousWorkingDay = if (startDateTime.isAfterWorkingHour()) startDateTime.nextWorkingHourOrSame() else startDateTime
                // `immediateNextWorkingDay` marks the end of the current calculation chunk (e.g., end of a working day).
                var immediateNextWorkingDay = previousWorkingDay.nextNonWorkingHour()

                // Loop through full working days between start and end dates.
                while (immediateNextWorkingDay.isBefore(endDateTime) && !immediateNextWorkingDay.isSameDay(endDateTime)) {
                    // This block handles scenarios where the loop lands on a weekend or non-working day.
                    if (previousWorkingDay.isSameDay(immediateNextWorkingDay) &&
                        previousWorkingDay.isOnWorkingDay().not()
                    ) {
                        // Current `previousWorkingDay` is a non-working day.
                        // Advance `previousWorkingDay` to the beginning of the next actual working day.
                        val nextWorkingDayAfterWeekend = previousWorkingDay.nextWorkingDay()
                        previousWorkingDay =
                            if (nextWorkingDayAfterWeekend.isBeforeWorkingHour()) {
                                // If the next working day starts before 9 AM, adjust to 9 AM.
                                nextWorkingDayAfterWeekend.nextWorkingHourOrSame()
                            } else {
                                // If the next working day starts after 9 AM (e.g., due to holidays), take the current time.
                                // This case might need further refinement based on specific holiday handling rules.
                                nextWorkingDayAfterWeekend.prevWorkingHour()
                            }

                        // Set `previousWorkingDay` to the start of the next working day's calculation window (e.g., Monday 9 AM).
                        previousWorkingDay = previousWorkingDay.nextWorkingDay().prevWorkingHour()
                        // Advance `immediateNextWorkingDay` to the end of that working day (e.g., Monday 5 PM).
                        immediateNextWorkingDay = immediateNextWorkingDay.nextWorkingDay()
                        continue // Skip the current iteration as it's a non-working day.
                    }

                    // Calculate working hours for the current full working day segment.
                    val workingHoursToday = workingDuration(previousWorkingDay, immediateNextWorkingDay)
                    workingHours = workingHours.plus(workingHoursToday)

                    // Advance `previousWorkingDay` to the start of the next working day's calculation window (e.g., Tuesday 9 AM).
                    previousWorkingDay = previousWorkingDay.nextWorkingDay().prevWorkingHour()
                    // Advance `immediateNextWorkingDay` to the end of that working day (e.g., Tuesday 5 PM).
                    immediateNextWorkingDay = immediateNextWorkingDay.nextWorkingDay()
                }

                // After the loop, calculate the remaining working hours for the final partial day.
                if (endDateTime.isOnWorkingDay()) {
                    // If the end date is a working day, calculate hours from the start of that working day up to the end time.
                    workingHours = workingHours.plus(workingDuration(endDateTime.startOfDay(), endDateTime))
                }
                return workingHours
            }
        }
    }

    /**
     * Calculates the working duration between two [ZonedDateTime]s, assuming they fall on the same working day.
     *
     * This function is designed to calculate the actual working time within the standard 9:00 AM to 5:00 PM window.
     * It makes the core assumption that both [startDateTime] and [endDateTime] are on a working day (Monday-Friday).
     * If `startDateTime` is before 9 AM, it's treated as 9 AM. If `endDateTime` is after 5 PM, it's treated as 5 PM.
     * If the entire period is outside working hours (e.g., 8 AM to 8:30 AM, or 6 PM to 7 PM), the duration will be zero.
     * If the period spans across the entire working day (e.g. 8 AM to 6 PM), it returns the full working day duration (8 hours).
     *
     * @param startDateTime The start ZonedDateTime.
     * @param endDateTime The end ZonedDateTime.
     * @return The duration between the two ZonedDateTimes, adjusted for working hours (9 AM - 5 PM).
     * @throws IllegalArgumentException if either [startDateTime] or [endDateTime] is not on a working day (Monday-Friday).
     */
    private fun workingDuration(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime,
    ): Duration {
        // Precondition: Ensure both start and end date-times are on working days.
        if ((startDateTime.isOnWorkingDay() && endDateTime.isOnWorkingDay()).not()) {
            throw IllegalArgumentException(
                "workingDuration function assumes both start and end times are on a working day. " +
                    "Received: Start: $startDateTime (isWorkingDay=${startDateTime.isOnWorkingDay()}), " +
                    "End: $endDateTime (isWorkingDay=${endDateTime.isOnWorkingDay()}).",
            )
        }

        val startToEndDiff = startDateTime.diffWith(endDateTime)
        when {
            // Case 1: Both start and end times are within standard working hours (9 AM - 5 PM).
            startDateTime.isWithinWorkingHour() && endDateTime.isWithinWorkingHour() -> {
                // The entire period is within the working window, so return the direct difference.
                return startToEndDiff
            }

            // Case 2: Both start and end times are outside standard working hours.
            (startDateTime.isWithinWorkingHour() || endDateTime.isWithinWorkingHour()).not() -> {
                // This condition means both startDateTime AND endDateTime are outside working hours.
                return if ((startDateTime.isBeforeWorkingHour() && endDateTime.isBeforeWorkingHour()) ||
                    (startDateTime.isAfterWorkingHour() && endDateTime.isAfterWorkingHour())
                ) {
                    // Both times are in the same non-working period (e.g., both before 9 AM, or both after 5 PM).
                    // Thus, no working hours are covered.
                    Duration.ZERO
                } else {
                    // Start time is before 9 AM and end time is after 5 PM (or vice-versa, though less common for valid ranges).
                    // This effectively spans the entire working day.
                    Duration.parse("8h") // Full standard working day duration.
                }
            }

            // Case 3: Start time is outside working hours (i.e., before 9 AM), but end time is within (9 AM - 5 PM).
            startDateTime.isWithinWorkingHour().not() -> {
                // The period starts before working hours but ends within them.
                // Calculate the difference from the actual start time to the end time (clamped to 5 PM by prevWorkingHour).
                // Then, subtract the non-working duration from the beginning of the day.
                // Example: start=8AM, end=10AM. diff(8AM,10AM)=2hrs. diff(8AM,9AM)=1hr. Result: 2-1=1hr.
                // `endDateTime.prevWorkingHour()` clamps endDateTime to 5 PM if it's later, ensuring we only count up to 5 PM.
                return startToEndDiff - (startDateTime.diffWith(endDateTime.prevWorkingHour()))
            }

            // Case 4: End time is outside working hours (i.e., after 5 PM), but start time is within (9 AM - 5 PM).
            endDateTime.isWithinWorkingHour().not() -> {
                // The period starts within working hours but ends after them.
                // Calculate the difference from the start time (clamped to 9 AM by nextNonWorkingHour) to the actual end time.
                // Then, subtract the non-working duration from the end of the day.
                // Example: start=4PM, end=6PM. diff(4PM,6PM)=2hrs. diff(5PM,6PM)=1hr. Result: 2-1=1hr.
                // `startDateTime.nextNonWorkingHour()` clamps startDateTime to 9 AM if it's earlier, ensuring we only count from 9 AM.
                return startToEndDiff - (startDateTime.nextNonWorkingHour().diffWith(endDateTime))
            }

            // Fallback Case: This case should ideally not be reached if the above conditions are exhaustive
            // for two times on the same working day. It implies that one time is within working hours and
            // the other is also within working hours, which should have been caught by Case 1.
            // Returning startToEndDiff is a defensive measure.
            else -> {
                // This typically implies both start and end are within working hours, already handled by Case 1.
                // If reached, it might indicate an unexpected scenario or a need to refine conditions.
                return startToEndDiff
            }
        }
    }
}
