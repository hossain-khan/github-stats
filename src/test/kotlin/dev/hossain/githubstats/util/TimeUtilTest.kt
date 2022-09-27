package dev.hossain.githubstats.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toDateTimePeriod
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration

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
        val shortFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault())
        val mediumFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault())
        val fullFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault())

        println(shortFormat.format(instantNow.toJavaInstant()))
        println(mediumFormat.format(instantNow.toJavaInstant()))
        println(fullFormat.format(instantNow.toJavaInstant()))
    }

    @Test
    fun `given two time - the difference accounts for working hours`() {
        println("- - - - - - - - - - - - - - - - - - - - - - - - -")
        // For example, working hours are usually from 9:00AM-5:00PM
        // Date#1: January 1, 2022 10:00AM
        // Date#2: January 3, 2020 02:00PM
        // The difference should be 20 hours

        val mediumFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault())

        // Sat Jan 01 2022 10:00:00 GMT-0500 (Eastern Standard Time)
        // Sat Jan 01 2022 15:00:00 GMT+0000
        val date1: Instant = Instant.fromEpochSeconds(1641049200)
        println(mediumFormat.format(date1.toJavaInstant()))

        // Mon Jan 03 2022 14:00:00 GMT-0500 (Eastern Standard Time)
        // Mon Jan 03 2022 19:00:00 GMT+0000
        val date2: Instant = Instant.fromEpochSeconds(1641236400)
        println(mediumFormat.format(date2.toJavaInstant()))

        val dateTimePeriod = date1.periodUntil(date2, TimeZone.currentSystemDefault())
        println("dateTimePeriod=$dateTimePeriod")

        val duration: Duration = date2 - date1
        println("Difference duration = $duration")
        println("Difference duration in period = ${duration.toDateTimePeriod()}")

        println("- - - - - - - - - - - - - - - - - - - - - - - - -")
    }
}
