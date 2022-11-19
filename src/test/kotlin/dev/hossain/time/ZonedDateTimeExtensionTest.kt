package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ZonedDateTimeExtensionTest {

    @Test
    fun startOfDay() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        val startOfDay: ZonedDateTime = dateTime.startOfDay()

        assertThat(startOfDay)
            .isEqualTo(Instant.parse("2022-09-05T00:00:00-04:00").toZdt())
    }

    @Test
    fun nextWorkingDay() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        val nextWorkingDay = dateTime.nextWorkingDay()
        assertThat(nextWorkingDay)
            .isEqualTo(Instant.parse("2022-09-06T10:00:00-04:00").toZdt()) // Next day, Tuesday
    }

    @Test
    fun nextWorkingDayOrSame() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        val nextWorkingDayOrSame = dateTime.nextWorkingDayOrSame()
        assertThat(nextWorkingDayOrSame).isEqualTo(dateTime)
    }

    @Test
    fun nextWorkingHourOrSame() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        val nextWorkingHourOrSame = dateTime.nextWorkingHourOrSame()
        assertThat(nextWorkingHourOrSame).isEqualTo(dateTime)
    }

    @Test
    fun nextNonWorkingHour() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        val nextNonWorkingHour = dateTime.nextNonWorkingHour()
        assertThat(nextNonWorkingHour)
            .isEqualTo(Instant.parse("2022-09-05T17:00:00-04:00").toZdt())
    }

    @Test
    fun prevWorkingHour() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        val prevWorkingHour = dateTime.prevWorkingHour()
        assertThat(prevWorkingHour)
            .isEqualTo(Instant.parse("2022-09-05T09:00:00-04:00").toZdt())
    }

    @Test
    fun isSameDay() {
        val dateTime1 = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        val dateTime2 = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        assertThat(dateTime1.isSameDay(dateTime2)).isTrue()
    }

    @Test
    fun isOnWorkingDay() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        assertThat(dateTime.isOnWorkingDay()).isTrue()
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
    fun `isBeforeWorkingHour - given date time during working hour - provides false`() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        assertThat(dateTime.isBeforeWorkingHour()).isFalse()
    }

    @Test
    fun `isBeforeWorkingHour - given date time before working hour - provides true`() {
        val dateTime = Instant.parse("2022-09-05T06:00:00-04:00").toZdt() // 06:00am Monday
        assertThat(dateTime.isBeforeWorkingHour()).isTrue()
    }

    @Test
    fun `isBeforeWorkingHour - given date time after working hour - provides false`() {
        val dateTime = Instant.parse("2022-09-05T21:00:00-04:00").toZdt() // 9:00pm Monday
        assertThat(dateTime.isBeforeWorkingHour()).isFalse()
    }

    @Test
    fun `isAfterWorkingHour - given date time during working hour - provides false`() {
        val dateTime = Instant.parse("2022-09-05T10:00:00-04:00").toZdt() // 10:00am Monday
        assertThat(dateTime.isAfterWorkingHour()).isFalse()
    }

    @Test
    fun `isAfterWorkingHour - given date time after working hour - provides true`() {
        val dateTime = Instant.parse("2022-09-05T21:00:00-04:00").toZdt() // 09:00pm Monday
        assertThat(dateTime.isAfterWorkingHour()).isTrue()
    }

    @Test
    fun `isAfterWorkingHour - given date time before working hour - provides false`() {
        val dateTime = Instant.parse("2022-09-05T06:00:00-04:00").toZdt() // 06:00am Monday
        assertThat(dateTime.isAfterWorkingHour()).isFalse()
    }

    private fun Instant.toZdt(): ZonedDateTime {
        val date1JavaInstant: java.time.Instant = this.toJavaInstant()
        return date1JavaInstant.atZone(Zone.city("New York"))
    }
}
