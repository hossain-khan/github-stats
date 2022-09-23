package dev.hossain.githubstats.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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
}
