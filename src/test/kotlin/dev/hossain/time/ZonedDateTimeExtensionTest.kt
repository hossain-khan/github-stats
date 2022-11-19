package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ZonedDateTimeExtensionTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun startOfDay() {
    }

    @Test
    fun nextWorkingDay() {
    }

    @Test
    fun nextWorkingDayOrSame() {
    }

    @Test
    fun nextWorkingHourOrSame() {
    }

    @Test
    fun nextNonWorkingHour() {
    }

    @Test
    fun prevWorkingHour() {
    }

    @Test
    fun diffWith() {
    }

    @Test
    fun isSameDay() {
    }

    @Test
    fun isOnWorkingDay() {
    }

    @Test
    fun `isWithinWorkingHour - given date-time within working hour - provides true`() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        assertThat(dateTime.isWithinWorkingHour()).isTrue()
    }

    @Test
    fun `isWithinWorkingHour - given date-time few minutes after working hour - provides false`() {
        val dateTime = Instant.parse("2022-09-05T17:10:00-04:00").toZdt() // 05:10pm Monday
        assertThat(dateTime.isWithinWorkingHour()).isFalse()
    }

    @Test
    fun `isWithinWorkingHour - given date-time few minutes before working hour - provides false`() {
        val dateTime = Instant.parse("2022-09-05T08:52:32-04:00").toZdt() // 05:10pm Monday
        assertThat(dateTime.isWithinWorkingHour()).isFalse()
    }

    @Test
    fun isBeforeWorkingHour() {
    }

    @Test
    fun isAfterWorkingHour() {
    }

    @Test
    fun format() {
    }

    private fun Instant.toZdt(): ZonedDateTime {
        val date1JavaInstant: java.time.Instant = this.toJavaInstant()
        return date1JavaInstant.atZone(ZoneId.systemDefault())
    }
}
