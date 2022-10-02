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
            startDateTime.isSameDay(endDateTime) && startDateTime.isOnWorkingDay() && endDateTime.isOnWorkingDay() -> {
                return workingDuration(startDateTime, endDateTime)
            }

            !startDateTime.isOnWorkingDay() && !endDateTime.isOnWorkingDay() -> {
                return Duration.parse("0s")
            }

            else -> {
                var workingHours = Duration.ZERO
                var previousWorkingDay = startDateTime
                var immediateNextWorkingDay = startDateTime.nextNonWorkingHour()

                while (immediateNextWorkingDay.isBefore(endDateTime) && !immediateNextWorkingDay.isSameDay(endDateTime)) {
                    val workingHoursToday = workingDuration(previousWorkingDay, immediateNextWorkingDay)
                    workingHours = workingHours.plus(workingHoursToday)

                    previousWorkingDay = previousWorkingDay.nextWorkingDay().prevWorkingHour()
                    immediateNextWorkingDay = immediateNextWorkingDay.nextWorkingDay()
                }

                // Finally calculate the last day
                workingHours = workingHours.plus(workingDuration(endDateTime.startOfDay(), endDateTime))
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
                return startToEndDiff
            }

            (startDateTime.isWithinWorkingHour() || endDateTime.isWithinWorkingHour()).not() -> {
                return if ((startDateTime.isBeforeWorkingHour() && endDateTime.isBeforeWorkingHour()) ||
                    startDateTime.isAfterWorkingHour() && endDateTime.isAfterWorkingHour()
                ) {
                    // Both start and end time was before/after working hour. Make the diff almost zero.
                    Duration.parse("0s") // Kudos, you get bonus point
                } else {
                    // That means, the start hour is before working hour,
                    // and the end hour is after working hour.
                    Duration.parse("8h") // Full day
                }
            }

            startDateTime.isWithinWorkingHour().not() -> {
                return startToEndDiff - (startDateTime.diffWith(startDateTime.nextWorkingHourOrSame()))
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
