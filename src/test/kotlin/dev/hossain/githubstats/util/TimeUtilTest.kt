package dev.hossain.githubstats.util

import dev.hossain.time.TemporalsExtension
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toDateTimePeriod
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration

/**
 * Some random testing ground to test out date time library and functionality.
 */
@Suppress("ktlint:standard:max-line-length")
internal class TimeUtilTest {
    @Test
    fun testDateTime() {
        // https://github.com/Kotlin/kotlinx-datetime#converting-instant-and-local-datetime-to-and-from-string
        val dateText1 = "2022-02-22T07:43:05Z"
        val dateText2 = "2022-09-21T14:37:39Z"

        val instantNow = Clock.System.now()
        println(instantNow.toString())

        val instant1: Instant = dateText1.toInstant()
        val instant2: Instant = dateText2.toInstant()
        println(instant1.toString())
        println(instant2.toString())

        val durationSinceThen1: Duration = instantNow - instant1
        val durationSinceThen2: Duration = instantNow - instant2
        println(durationSinceThen1)
        println(durationSinceThen2)
    }

    @Test
    fun testDateTimeFormat() {
        val instantNow = Clock.System.now()
        val shortFormat =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault())
        val mediumFormat =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault())
        val fullFormat =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault())

        println(shortFormat.format(instantNow.toJavaInstant()))
        println(mediumFormat.format(instantNow.toJavaInstant()))
        println(fullFormat.format(instantNow.toJavaInstant()))
    }

    @Test
    fun `given two time - the difference accounts for working hours`() {
        println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")
        // For example, working hours are usually from 9:00AM-5:00PM
        // Date#1: Saturday, January 1, 2022 10:00AM
        // Date#2: Monday, January 3, 2022 02:00PM
        // The difference should be 20 hours,
        // However, since it's in saturday, actual working hour is 5 hours!!!

        val mediumFormat =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault())

        // Sat Jan 01 2022 10:00:00 GMT-0500 (Eastern Standard Time)
        // Sat Jan 01 2022 15:00:00 GMT+0000
        val date1: Instant = Instant.fromEpochSeconds(1641049200)
        println(mediumFormat.format(date1.toJavaInstant()))
        val calendar1: Calendar = Calendar.getInstance()
        calendar1.timeInMillis = 1641049200000
        println("Calendar 1: ${calendar1.time}")

        // Mon Jan 03 2022 14:00:00 GMT-0500 (Eastern Standard Time)
        // Mon Jan 03 2022 19:00:00 GMT+0000
        val date2: Instant = Instant.fromEpochSeconds(1641236400)
        println(mediumFormat.format(date2.toJavaInstant()))
        val calendar2: Calendar = Calendar.getInstance()
        calendar2.timeInMillis = 1641236400000
        println("Calendar 2: ${calendar2.time}")

        val dateTimePeriod = date1.periodUntil(date2, TimeZone.currentSystemDefault())
        println("dateTimePeriod=$dateTimePeriod")

        val duration: Duration = date2 - date1
        println("Difference duration = $duration")
        println("Difference duration in period = ${duration.toDateTimePeriod()}")

        val workedMinutes = getWorkedMinutes(calendar1, calendar2)
        println("getWorkedMinutes = $workedMinutes or ${workedMinutes / 60} hours")

        println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")
        val localDateTime1: LocalDateTime = date1.toLocalDateTime(TimeZone.currentSystemDefault())
        val localDateTime2: LocalDateTime = date2.toLocalDateTime(TimeZone.currentSystemDefault())
        println("localDateTime1=$localDateTime1, localDateTime2=$localDateTime2")

        println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")
        val date1JavaInstant: java.time.Instant = date1.toJavaInstant()
        val zonedDateTime1: ZonedDateTime = date1JavaInstant.atZone(ZoneId.systemDefault())
        val nextWorkingDay: ZonedDateTime = zonedDateTime1.with(TemporalsExtension.nextWorkingDay())
        val nextWorkingDayOrSame = zonedDateTime1.with(TemporalsExtension.nextWorkingDayOrSame())

        println(
            "Date#1: ($zonedDateTime1) \nnextWorkingDay=$nextWorkingDay, \nnextWorkingDayOrSame=$nextWorkingDayOrSame",
        )

        val date2JavaInstant: java.time.Instant = date2.toJavaInstant()
        val zonedDateTime2: ZonedDateTime = date2JavaInstant.atZone(ZoneId.systemDefault())
        val nextWorkingDay2: ZonedDateTime = zonedDateTime2.with(TemporalsExtension.nextWorkingDay())
        val nextWorkingDayOrSame2 = zonedDateTime2.with(TemporalsExtension.nextWorkingDayOrSame())
        println(
            "Date#2: ($zonedDateTime2) \nnextWorkingDay=$nextWorkingDay2, \nnextWorkingDayOrSame=$nextWorkingDayOrSame2",
        )

        // Represent a span-of-time in terms of days (24-hour chunks of time, not calendar days), hours, minutes, seconds.
        // Internally, a count of whole seconds plus a fractional second (nanoseconds).
        val durationZ1Z2: java.time.Duration = java.time.Duration.between(zonedDateTime1, zonedDateTime2)
        println("durationZ1Z2=$durationZ1Z2")

        // Represent a span-of-time in terms of years-months-days.
        // Extract the date-only from the date-time-zone object.
        val periodZ1Z2 =
            Period.between(
                zonedDateTime1.toLocalDate(),
                zonedDateTime2.toLocalDate(),
            )
        println("periodZ1Z2=$periodZ1Z2")
        println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")
    }

    @Test
    fun whenAdjust_thenNextSunday() {
        val localDate: LocalDate = LocalDate.of(2017, 7, 8)
        val nextSunday: LocalDate = localDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        val expected = "2017-07-09"
        assertEquals(expected, nextSunday.toString())
    }

    @Test
    fun whenAdjust_thenFourteenDaysAfterDate() {
        val localDate = LocalDate.of(2017, 7, 8)
        val temporalAdjuster = TemporalAdjuster { t -> t.plus(Period.ofDays(14)) }
        val result = localDate.with(temporalAdjuster)
        val fourteenDaysAfterDate = "2017-07-22"
        assertEquals(fourteenDaysAfterDate, result.toString())
    }

    var NEXT_WORKING_DAY =
        TemporalAdjusters.ofDateAdjuster { date: LocalDate ->
            val dayOfWeek = date.dayOfWeek
            val daysToAdd: Int =
                if (dayOfWeek == DayOfWeek.FRIDAY) {
                    3
                } else if (dayOfWeek == DayOfWeek.SATURDAY) {
                    2
                } else {
                    1
                }
            date.plusDays(daysToAdd.toLong())
        }

    @Test
    fun whenAdjust_thenNextWorkingDay() {
        val localDate = LocalDate.of(2017, 7, 8)
        val temporalAdjuster: TemporalAdjuster = NEXT_WORKING_DAY
        val result = localDate.with(temporalAdjuster)
        assertEquals("2017-07-10", result.toString())
    }

    @Test
    fun testTemporalAdjusters() {
        val localDate = LocalDate.now()
        println("current date : $localDate")

        val with = localDate.with(TemporalAdjusters.firstDayOfMonth())
        println("firstDayOfMonth : $with")

        val with1 = localDate.with(TemporalAdjusters.lastDayOfMonth())
        println("lastDayOfMonth : $with1")

        val with2 = localDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        println("next monday : $with2")

        val with3 = localDate.with(TemporalAdjusters.firstDayOfNextMonth())
        println("firstDayOfNextMonth : $with3")
    }

    /**
     * https://stackoverflow.com/questions/28995301/get-minutes-between-two-working-days-in-java
     */
    fun getWorkedMinutes(
        startTime: Calendar,
        endTime: Calendar,
    ): Int {
        val BEGINWORKHOUR = 8
        val ENDWORKHOUR = 16

        fun workedMinsDay(start: Calendar): Int {
            return if (start[Calendar.DAY_OF_WEEK] == 1 || start[Calendar.DAY_OF_WEEK] == 6) 0 else 60 - start[Calendar.MINUTE] + (ENDWORKHOUR - start[Calendar.HOUR_OF_DAY] - 1) * 60
        }

        fun sameDay(
            start: Calendar,
            end: Calendar,
        ): Boolean {
            return start[Calendar.YEAR] == end[Calendar.YEAR] && start[Calendar.MONTH] == end[Calendar.MONTH] && start[Calendar.DAY_OF_MONTH] == end[Calendar.DAY_OF_MONTH]
        }

        val start = startTime
        val end = endTime
        if (start[Calendar.HOUR_OF_DAY] < BEGINWORKHOUR) {
            start[Calendar.HOUR_OF_DAY] = BEGINWORKHOUR
            start[Calendar.MINUTE] = 0
        }
        if (end[Calendar.HOUR_OF_DAY] >= ENDWORKHOUR) {
            end[Calendar.HOUR_OF_DAY] = ENDWORKHOUR
            end[Calendar.MINUTE] = 0
        }
        var workedMins = 0
        while (!sameDay(start, end)) {
            workedMins += workedMinsDay(start)
            start.add(Calendar.DAY_OF_MONTH, 1)
            start[Calendar.HOUR_OF_DAY] = BEGINWORKHOUR
            start[Calendar.MINUTE] = 0
        }
        workedMins += end[Calendar.MINUTE] - start[Calendar.MINUTE] + (end[Calendar.HOUR_OF_DAY] - start[Calendar.HOUR_OF_DAY]) * 60
        return workedMins
    }
}
