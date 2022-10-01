package time

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.threeten.extra.Temporals
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration

object DateTimeDiffer {
    fun diffWorkingHours(startTime: Instant, endTime: Instant, zoneId: ZoneId): Duration {
        val startDateTime: ZonedDateTime = startTime.toJavaInstant().atZone(zoneId)
        val endDateTime: ZonedDateTime = endTime.toJavaInstant().atZone(zoneId)

        /*
         * Things to consider:
         * - Start DateTime:
         *   • Was it opened during working day and working hour?
         *   • When is the next working day and hour and the difference between start date time?
         *   • Need to ensure that next working day+hour does not exceed the end time
         * - End DateTime
         *   • Was it done during working day and time?
         *   • What if it was done in non-working hour (after hours)? how do we credit the reviewer?
         */

        println("startDateTime=$startDateTime and $endDateTime")

        val startTimeNextWorkingDay = startDateTime.with(Temporals.nextWorkingDayOrSame())
        val endTimeNextWorkingDay = endDateTime.with(Temporals.nextWorkingDayOrSame())
        println("startTimeNextWorkingDay=$startTimeNextWorkingDay : " + startTimeNextWorkingDay.equals(startDateTime))
        println("endTimeNextWorkingDay=$endTimeNextWorkingDay : " + endTimeNextWorkingDay.equals(endDateTime))

        if (startTimeNextWorkingDay.equals(startDateTime).not()) {
            val nextWorkingDayDiff = java.time.Duration.between(startDateTime, startTimeNextWorkingDay)
            println("nextWorkingDayDiff=$nextWorkingDayDiff")
        }

        val nonWorkingHour = startDateTime.with(TemporalsExtension.nextWorkingHourOrSame())
        println("Next nonWorkingHour = $nonWorkingHour")

        // Basically find how many hours between start and end time was non-countable
        // Count hours til end of the day if it's in working hours
        // - else, counter will start from next business day
        // Then if the end date is on next day, count hours from next working day

        return endTime - startTime
    }

    fun isOnWorkingDay(zonedDateTime: ZonedDateTime): Boolean {
        val nextWorkingDayOrSame = zonedDateTime.nextWorkingDay()
        return zonedDateTime == nextWorkingDayOrSame
    }

    fun isWithinWorkingHour(zonedDateTime: ZonedDateTime): Boolean {
        val nonWorkingHour = zonedDateTime.nextWorkingHour()
        return zonedDateTime == nonWorkingHour
    }

    private inline fun ZonedDateTime.nextWorkingDay() = this.with(Temporals.nextWorkingDayOrSame())
    private inline fun ZonedDateTime.nextWorkingHour() = this.with(TemporalsExtension.nextWorkingHourOrSame())
}
