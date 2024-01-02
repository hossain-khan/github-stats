package dev.hossain.time

import kotlin.time.Duration

/**
 * Formats duration into working hours based on [workingHoursInADay] configuration.
 *
 * Examples:
 *  - Input: 11h     | Output: 1 day and 3h [Based on 8h on a working day]
 *  - Input: 2h 49m  | Output: 2h 49m
 *  - Input: 1d      | Output: 3 days [Based on 8h on a working day]
 *  - Input: 24h     | Output: 3 days [Based on 8h on a working day]
 */
fun Duration.toWorkingHour(workingHoursInADay: Int = 8): String {
    return if (inWholeHours >= workingHoursInADay) {
        val wholeDays: Long = inWholeHours.div(workingHoursInADay)
        val wholeDayHours = Duration.parse("${wholeDays * workingHoursInADay}h")
        val remainingHoursAfterWholeDays = this.minus(wholeDayHours)

        "$wholeDays ${if (wholeDays > 1) "days" else "day"}${
            (if (remainingHoursAfterWholeDays > Duration.ZERO) " and $remainingHoursAfterWholeDays" else "")
        } [Based on ${workingHoursInADay}h on a working day]"
    } else {
        this.toString()
    }
}
