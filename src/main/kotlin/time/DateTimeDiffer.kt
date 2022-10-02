package time

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.threeten.extra.Temporals
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
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
            "startDateTime=${startDateTime.format()}\nendDateTime=${endDateTime.format()};" +
                "\nstartTimeNextWorkingDay=${startTimeNextWorkingDay.format()}" +
                "\nendTimeNextWorkingDay=${endTimeNextWorkingDay.format()}" +
                "\nstartNextWorkingHourOrSame=${startNextWorkingHourOrSame.format()}" +
                "\nstartNonWorkingHourOrSame=${startNonWorkingHourOrSame.format()}" +
                "\nendNextWorkingHourOrSame=${endNextWorkingHourOrSame.format()}" +
                "\nendNonWorkingHourOrSame=${endNonWorkingHourOrSame.format()}"
        )

        when {
            startDateTime.isSameDay(endDateTime) && isOnWorkingDay(startDateTime) && isOnWorkingDay(endDateTime) -> {
                return workingDuration(startDateTime, endDateTime)
            }

            else -> {
                var workingHours = Duration.ZERO
                var previousWorkingDay = startDateTime
                var immediateNextWorkingDay = startDateTime.nextNonWorkingHour()

                while (immediateNextWorkingDay.isBefore(endDateTime) && !immediateNextWorkingDay.isSameDay(endDateTime)) {
                    val workingHoursToday = workingDuration(previousWorkingDay, immediateNextWorkingDay)
                    println("Add $workingHoursToday to $workingHours")
                    workingHours = workingHours.plus(workingHoursToday)

                    previousWorkingDay = previousWorkingDay.nextWorkingDay().prevWorkingHour()
                    immediateNextWorkingDay = immediateNextWorkingDay.nextWorkingDay()
                }

                // Finally calculate the last day
                workingHours = workingHours.plus(workingDuration(endDateTime.startOfDay(), endDateTime))
                return workingHours
            }

            /*else -> {
                var nonWorkingDuration = Duration.ZERO
                var immediateNextWorkingDay = startDateTime
                do {
                    // Total time diff = 34 hours
                    // 7 working hours first day and 8 working hour second day = 15 hours
                    // 7 non-working hour on first day till 12am
                    // 9 non-working hour on second day till 9 am
                    // 3 non-working hour on second day till 8pm
                    // TOTAL non-working= 19 hours
                    //
                    val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
                    val endTime: Instant = Instant.parse("2022-09-06T20:00:00-04:00") // 08:00pm next day

                    immediateNextWorkingDay = immediateNextWorkingDay.nextWorkingDay()
                    println(
                        "while $immediateNextWorkingDay (${immediateNextWorkingDay.isBefore(endDateTime)} & ${
                        !immediateNextWorkingDay.isSameDay(
                            endDateTime
                        )
                        })"
                    )

                    when {
                        // 10am next day
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
            }*/
        }
    }

    private fun workingDuration(
        startDateTime: ZonedDateTime,
        endDateTime: ZonedDateTime
    ): Duration {
        val startToEndDiff = startDateTime.diffWith(endDateTime)
        println("workingDuration() - startToEndDiff = $startToEndDiff")
        when {
            isWithinWorkingHour(startDateTime) && isWithinWorkingHour(endDateTime) -> {
                return startToEndDiff
            }

            (isWithinWorkingHour(startDateTime) || isWithinWorkingHour(endDateTime)).not() -> {
                return if ((isBeforeWorkingHour(startDateTime) && isBeforeWorkingHour(endDateTime)) ||
                    isAfterWorkingHour(startDateTime) && isAfterWorkingHour(endDateTime)
                ) {
                    // Both start and end time was before/after working hour. Make the diff almost zero.
                    Duration.parse("0s") // Kudos, you get bonus point
                } else {
                    // That means, the start hour is before working hour,
                    // and the end hour is after working hour.
                    Duration.parse("8h") // Full day
                }
            }

            isWithinWorkingHour(startDateTime).not() -> {
                return startToEndDiff - (startDateTime.diffWith(startDateTime.nextWorkingHourOrSame()))
            }

            isWithinWorkingHour(endDateTime).not() -> {
                return startToEndDiff - (startDateTime.nextNonWorkingHour().diffWith(endDateTime))
            }

            else -> {
                return startToEndDiff
            }
        }
    }

    private fun isOnWorkingDay(zonedDateTime: ZonedDateTime): Boolean {
        val nextWorkingDayOrSame = zonedDateTime.nextWorkingDayOrSame()
        return zonedDateTime == nextWorkingDayOrSame
    }

    private fun isWithinWorkingHour(zonedDateTime: ZonedDateTime): Boolean {
        val nonWorkingHour = zonedDateTime.nextWorkingHourOrSame()
        return zonedDateTime == nonWorkingHour
    }

    private fun isBeforeWorkingHour(zonedDateTime: ZonedDateTime): Boolean {
        val nextWorkingHourOrSame = zonedDateTime.nextWorkingHourOrSame()
        val prevWorkingHour = zonedDateTime.prevWorkingHour()
        if (nextWorkingHourOrSame == zonedDateTime) {
            return false
        }

        // If the previous working our is previous day, then must be before 9 am
        return prevWorkingHour.isSameDay(zonedDateTime).not()
    }

    private fun isAfterWorkingHour(zonedDateTime: ZonedDateTime): Boolean {
        val nextWorkingHourOrSame = zonedDateTime.nextWorkingHourOrSame()
        if (nextWorkingHourOrSame == zonedDateTime) {
            return false
        }

        // If the next working our is next day, then must be after 5pm
        return nextWorkingHourOrSame.isSameDay(zonedDateTime).not()
    }

    // region: Internal Extension Functions
    private fun ZonedDateTime.startOfDay() = this.with(TemporalsExtension.startOfDay())
    private fun ZonedDateTime.nextWorkingDay() = this.with(Temporals.nextWorkingDay())
    private fun ZonedDateTime.prevWorkingDay() = this.with(Temporals.previousWorkingDay())
    private fun ZonedDateTime.nextWorkingDayOrSame() = this.with(Temporals.nextWorkingDayOrSame())
    private fun ZonedDateTime.nextWorkingHourOrSame() = this.with(TemporalsExtension.nextWorkingHourOrSame())
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

    private fun ZonedDateTime.format(): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault())

        return this.format(formatter)
    }
    // endregion: Internal Extension Functions
}
