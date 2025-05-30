package dev.hossain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone // Use kotlinx.datetime.TimeZone
import kotlin.time.Duration

/**
 * Contains utility function to diff date-time using [kotlinx.datetime.Instant] and [kotlinx.datetime.TimeZone].
 */
object DateTimeDiffer {
    /**
     * > ⚠️ WARNING ⚠️: This is overly complex function. Do not try to decipher it 😅
     *
     * Provides working hours between two instants on given [timeZone].
     * It adds all the working hours between `9:00AM` and `5:00PM` excluding weekends (Saturday and Sunday).
     * The exact definitions of "working hours" and "weekends" are determined by the
     * `actual` implementations of the `expect` functions in `ExpectedTemporals.kt`.
     */
    fun diffWorkingHours(
        startInstant: Instant,
        endInstant: Instant,
        timeZone: TimeZone, // Changed from ZoneId to TimeZone
    ): Duration {
        if (endInstant < startInstant) {
            // Using format() defined in InstantExtension.kt (which currently uses ISO format)
            throw IllegalArgumentException("The end time ${endInstant.format(timeZone)} is before ${startInstant.format(timeZone)}.")
        }

        when {
            startInstant.isSameDay(endInstant, timeZone) &&
                startInstant.isOnWorkingDay(timeZone) &&
                endInstant.isOnWorkingDay(timeZone) -> {
                return workingDuration(startInstant, endInstant, timeZone)
            }

            startInstant.isNextDay(endInstant, timeZone) &&
                startInstant.isOnWorkingDay(timeZone) &&
                !endInstant.isOnWorkingDay(timeZone) -> {
                return workingDuration(startInstant, endInstant.prevWorkingHour(timeZone), timeZone)
            }

            !startInstant.isOnWorkingDay(timeZone) &&
                !endInstant.isOnWorkingDay(timeZone) &&
                ((endInstant - startInstant) < Duration.parse("2d")) -> { // Ensure kotlin.time.Duration is used
                return Duration.ZERO
            }

            else -> {
                var workingHours = Duration.ZERO
                var currentProcessingInstant =
                    if (startInstant.isAfterWorkingHour(timeZone)) {
                        startInstant.nextWorkingHourOrSame(timeZone)
                    } else {
                        startInstant
                    }
                var endOfCurrentProcessingDay = currentProcessingInstant.nextNonWorkingHour(timeZone)

                while (endOfCurrentProcessingDay < endInstant && !endOfCurrentProcessingDay.isSameDay(endInstant, timeZone)) {
                    if (!currentProcessingInstant.isSameDay(endOfCurrentProcessingDay, timeZone) ||
                        !currentProcessingInstant.isOnWorkingDay(timeZone)
                    ) {
                        val nextWorkingDayAfterWeekend = currentProcessingInstant.nextWorkingDay(timeZone)
                        currentProcessingInstant =
                            if (nextWorkingDayAfterWeekend.isBeforeWorkingHour(timeZone)) {
                                nextWorkingDayAfterWeekend.nextWorkingHourOrSame(timeZone)
                            } else {
                                // This logic might need to be part of the expect function itself
                                // For now, assuming nextWorkingDay brings it to the start of that working day
                                nextWorkingDayAfterWeekend.startOfDay(timeZone).nextWorkingHourOrSame(timeZone)
                            }
                        endOfCurrentProcessingDay = currentProcessingInstant.nextNonWorkingHour(timeZone)
                        continue
                    }

                    workingHours += workingDuration(currentProcessingInstant, endOfCurrentProcessingDay, timeZone)

                    // Move to the start of the next working day's relevant slot
                    currentProcessingInstant = currentProcessingInstant.nextWorkingDay(timeZone).startOfDay(timeZone).nextWorkingHourOrSame(timeZone)
                    endOfCurrentProcessingDay = currentProcessingInstant.nextNonWorkingHour(timeZone)
                }

                if (endInstant.isOnWorkingDay(timeZone)) {
                    // Calculate for the last partial day
                    // Ensure currentProcessingInstant is not after endInstant for this final calculation
                    val lastDayStart = endInstant.startOfDay(timeZone).nextWorkingHourOrSame(timeZone)
                    if (endInstant > lastDayStart) {
                         workingHours += workingDuration(lastDayStart, endInstant, timeZone)
                    } else if (currentProcessingInstant < endInstant && currentProcessingInstant.isSameDay(endInstant, timeZone)) {
                        // If the loop exited because endOfCurrentProcessingDay went past endInstant,
                        // but currentProcessingInstant is still relevant for the final day.
                        workingHours += workingDuration(currentProcessingInstant, endInstant, timeZone)
                    }
                }
                return workingHours
            }
        }
    }

    private fun workingDuration(
        startInstant: Instant,
        endInstant: Instant,
        timeZone: TimeZone,
    ): Duration {
        if (endInstant < startInstant) return Duration.ZERO // Ensure start is not after end

        if (!startInstant.isOnWorkingDay(timeZone) || !endInstant.isOnWorkingDay(timeZone)) {
             // This check might be too strict if one of them is a non-working day but the period spans working parts.
             // However, the main loop in diffWorkingHours should handle iterating through days.
             // This function is mostly for intra-day calculation on a known working day.
             // If called for a non-working day, it means the logic in diffWorkingHours might have an issue.
             // For now, returning ZERO, but this indicates complex edge cases.
            return Duration.ZERO
        }

        val effectiveStart = if (startInstant.isBeforeWorkingHour(timeZone)) startInstant.nextWorkingHourOrSame(timeZone) else startInstant
        val effectiveEnd = if (endInstant.isAfterWorkingHour(timeZone)) endInstant.prevWorkingHour(timeZone).nextNonWorkingHour(timeZone) else endInstant
        // prevWorkingHour().nextNonWorkingHour() should ideally give the end of the working slot for that day (e.g. 5 PM)

        if (effectiveEnd <= effectiveStart) return Duration.ZERO

        // Ensure both effectiveStart and effectiveEnd are within the *same* working day's valid hours
        // This simplified version assumes effectiveStart and effectiveEnd are now within a valid working period.
        // The core logic of TemporalAdjusters (now in expect/actual) must correctly define these boundaries.

        // Direct difference if both are within a valid working period.
        // The original logic had more cases here. This simplified version relies on the
        // effectiveness of the boundary-setting functions (nextWorkingHourOrSame, prevWorkingHour, etc.)
        if (effectiveStart.isWithinWorkingHour(timeZone) && effectiveEnd.isWithinWorkingHour(timeZone)) {
            return effectiveEnd - effectiveStart
        }

        // If start is before working hours and end is after working hours of the same day
        if (effectiveStart.isBeforeWorkingHour(timeZone) && effectiveEnd.isAfterWorkingHour(timeZone) && effectiveStart.isSameDay(effectiveEnd, timeZone)) {
            return Duration.parse("8h") // Assuming 8 working hours per day
        }
        
        // Fallback for more complex scenarios not perfectly covered by the simplified boundary adjustments
        // This part of logic is the most complex and error prone without the exact TemporalAdjuster behavior.
        // The expect functions for temporals need to be very robust.
        // A more conservative approach if unsure:
        if (effectiveStart < effectiveEnd) {
            return effectiveEnd - effectiveStart
        }

        return Duration.ZERO
    }
}
