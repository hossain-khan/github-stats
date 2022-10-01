package time

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.threeten.extra.Temporals
import java.time.ZoneId
import java.time.ZonedDateTime
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
    fun diffWorkingHours(startTime: Instant, endTime: Instant, zoneId: ZoneId): Duration {
        val startDateTime: ZonedDateTime = startTime.toJavaInstant().atZone(zoneId)
        val endDateTime: ZonedDateTime = endTime.toJavaInstant().atZone(zoneId)

        if (endDateTime.isBefore(startDateTime)) {
            throw IllegalArgumentException("The end time $endTime is before $startTime.")
        }

        val startToEndDiff = endTime - startTime

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
        val nextWorkingHourOrSame = startDateTime.with(TemporalsExtension.nextWorkingHourOrSame())
        val nextNonWorkingHourOrSame = startDateTime.with(TemporalsExtension.nextNonWorkingHourOrSame())

        println(
            "startTimeNextWorkingDay=$startTimeNextWorkingDay\nendTimeNextWorkingDay=$endTimeNextWorkingDay" +
                "\nnextWorkingHourOrSame=$nextWorkingHourOrSame\nnextNonWorkingHourOrSame=$nextNonWorkingHourOrSame"
        )

        val durationDiff = java.time.Duration.between(nextNonWorkingHourOrSame, nextWorkingHourOrSame)
        println("startToEndDiff = $startToEndDiff")
        return when {
            isOnWorkingDay(startDateTime) && isOnWorkingDay(endDateTime) -> {
                startToEndDiff
            }

            else -> startToEndDiff - durationDiff.seconds.toDuration(DurationUnit.SECONDS)
        }
    }

    fun isOnWorkingDay(zonedDateTime: ZonedDateTime): Boolean {
        val nextWorkingDayOrSame = zonedDateTime.nextWorkingDay()
        return zonedDateTime == nextWorkingDayOrSame
    }

    fun isWithinWorkingHour(zonedDateTime: ZonedDateTime): Boolean {
        val nonWorkingHour = zonedDateTime.nextWorkingHour()
        return zonedDateTime == nonWorkingHour
    }

    // region: Internal Extension Functions
    private fun ZonedDateTime.nextWorkingDay() = this.with(Temporals.nextWorkingDayOrSame())
    private fun ZonedDateTime.nextWorkingHour() = this.with(TemporalsExtension.nextWorkingHourOrSame())
    private fun ZonedDateTime.nextNonWorkingHour() = this.with(TemporalsExtension.nextNonWorkingHourOrSame())
    // endregion: Internal Extension Functions
}
