package time

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.threeten.extra.Temporals
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object DateTimeDiffer {
    /**
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
     */
    fun diffWorkingHours(startInstant: Instant, endInstant: Instant, zoneId: ZoneId): Duration {
        val startDateTime: ZonedDateTime = startInstant.toJavaInstant().atZone(zoneId)
        val endDateTime: ZonedDateTime = endInstant.toJavaInstant().atZone(zoneId)

        if (endDateTime.isBefore(startDateTime)) {
            throw IllegalArgumentException("The end time $endInstant is before $startInstant.")
        }

        val startToEndDiff = endInstant - startInstant

        /*
         * Things to consider:
         * - Start DateTime:
         *   • Was it opened during working day and working hour?
         *   • When is the next working day and hour and the difference between start date time?
         *   • Need to ensure that next working day+hour does not exceed the end time
         * - End DateTime
         *   • Was it done during working day and time?
         *   • What if it was done in non-working hour (after hours)? how do we credit the reviewer?
         *
         * Data Available:
         * ✔ Start time
         *   ➜ Is it during working hours?
         *   ➜ Is it during working day?
         *   ➜ What is end of the day time?
         *   ➜ When does next day working hour begin?
         * ✔ End time
         *   ➜ Is end time on same day as start time?
         *   ➜ Is end time next day during working hours?
         */

        val startTimeNextWorkingDay = startDateTime.with(Temporals.nextWorkingDayOrSame())
        val endTimeNextWorkingDay = endDateTime.with(Temporals.nextWorkingDayOrSame())
        val startNextWorkingHourOrSame = startDateTime.with(TemporalsExtension.nextWorkingHourOrSame())
        val startNonWorkingHourOrSame = startDateTime.with(TemporalsExtension.nextNonWorkingHourOrSame())
        val endNextWorkingHourOrSame = endDateTime.with(TemporalsExtension.nextWorkingHourOrSame())
        val endNonWorkingHourOrSame = endDateTime.with(TemporalsExtension.nextNonWorkingHourOrSame())

        println(
            "startDateTime=$startDateTime\nendDateTime=$endDateTime;" +
                "\nstartTimeNextWorkingDay=$startTimeNextWorkingDay" +
                "\nendTimeNextWorkingDay=$endTimeNextWorkingDay" +
                "\nstartNextWorkingHourOrSame=$startNextWorkingHourOrSame" +
                "\nstartNonWorkingHourOrSame=$startNonWorkingHourOrSame" +
                "\nendNextWorkingHourOrSame=$endNextWorkingHourOrSame" +
                "\nendNonWorkingHourOrSame=$endNonWorkingHourOrSame"
        )

        when {
            startDateTime.isSameDay(endDateTime) && isOnWorkingDay(startDateTime) && isOnWorkingDay(endDateTime) -> {
                when {
                    isWithinWorkingHour(startDateTime) && isWithinWorkingHour(endDateTime) -> {
                        return startToEndDiff
                    }

                    (isWithinWorkingHour(startDateTime) || isWithinWorkingHour(endDateTime)).not() -> {
                        // Both start and end time was before/after working hour. Make the diff almost zero.
                        return Duration.parse("1m") // Kudos, you get bonus point
                    }

                    isWithinWorkingHour(startDateTime).not() -> {
                        return startToEndDiff - (startDateTime.diffWith(startDateTime.nextWorkingHour()))
                    }

                    isWithinWorkingHour(endDateTime).not() -> {
                        return startToEndDiff - (startDateTime.nextNonWorkingHour().diffWith(endDateTime))
                    }

                    else -> {
                        return startToEndDiff
                    }
                }
            }

            else -> {
                var nonWorkingDuration = Duration.ZERO
                var immediateNextWorkingDay = startDateTime
                do {
                    immediateNextWorkingDay = immediateNextWorkingDay.nextWorkingDay()
                    println(
                        "while $immediateNextWorkingDay (${immediateNextWorkingDay.isBefore(endDateTime)} & ${
                        !immediateNextWorkingDay.isSameDay(
                            endDateTime
                        )
                        })"
                    )

                    when {
                        immediateNextWorkingDay.isSameDay(endDateTime) -> {
                            val diffWith =
                                immediateNextWorkingDay.prevWorkingDay().nextNonWorkingHour()
                                    .diffWith(endDateTime.prevWorkingHour())
                            println("nonWorkingDuration (last day) = $nonWorkingDuration + $diffWith")
                            nonWorkingDuration = nonWorkingDuration.plus(diffWith)
                        }

                        else -> {
                            println("nonWorkingDuration = $nonWorkingDuration + 16h")
                            nonWorkingDuration = nonWorkingDuration.plus(Duration.parse("16h"))
                        }
                    }

                    nonWorkingDuration = nonWorkingDuration.plus(Duration.ZERO)
                } while (immediateNextWorkingDay.isBefore(endDateTime) && !immediateNextWorkingDay.isSameDay(endDateTime))

                val diffWorkingHoursOnly = startToEndDiff - nonWorkingDuration
                println("Return: ($startToEndDiff - $nonWorkingDuration) = $diffWorkingHoursOnly")
                return diffWorkingHoursOnly
            }
        }
    }

    private fun isOnWorkingDay(zonedDateTime: ZonedDateTime): Boolean {
        val nextWorkingDayOrSame = zonedDateTime.nextWorkingDayOrSame()
        return zonedDateTime == nextWorkingDayOrSame
    }

    private fun isWithinWorkingHour(zonedDateTime: ZonedDateTime): Boolean {
        val nonWorkingHour = zonedDateTime.nextWorkingHour()
        return zonedDateTime == nonWorkingHour
    }

    // region: Internal Extension Functions
    private fun ZonedDateTime.nextWorkingDay() = this.with(Temporals.nextWorkingDay())
    private fun ZonedDateTime.prevWorkingDay() = this.with(Temporals.previousWorkingDay())
    private fun ZonedDateTime.nextWorkingDayOrSame() = this.with(Temporals.nextWorkingDayOrSame())
    private fun ZonedDateTime.nextWorkingHour() = this.with(TemporalsExtension.nextWorkingHourOrSame())
    private fun ZonedDateTime.nextNonWorkingHour() = this.with(TemporalsExtension.nextNonWorkingHourOrSame())

    /** Previous working start hour of the day.*/
    private fun ZonedDateTime.prevWorkingHour() = this.with(TemporalsExtension.prevWorkingHourOrSame())

    private fun ZonedDateTime.diffWith(endDateTime: ZonedDateTime): Duration {
        return java.time.Duration.between(this, endDateTime).seconds.toDuration(DurationUnit.SECONDS)
    }

    private fun Instant.isSameDay(other: Instant): Boolean {
        return this.toJavaInstant().truncatedTo(ChronoUnit.DAYS) == other.toJavaInstant().truncatedTo(ChronoUnit.DAYS)
    }

    private fun ZonedDateTime.isSameDay(other: ZonedDateTime): Boolean {
        return this.year == other.year && this.month == other.month && this.dayOfMonth == other.dayOfMonth
    }
    // endregion: Internal Extension Functions
}
