package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
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
    fun `startOfDay - given date time has minutes and seconds - resets them to zero`() {
        val date1 = "2022-02-22T07:43:05Z".toInstant().toZdt()
        val date2 = "2022-09-21T14:37:39Z".toInstant().toZdt()

        val startOfDay1 = date1.startOfDay()
        val startOfDay2 = date2.startOfDay()

        assertThat(startOfDay1.hour).isEqualTo(0)
        assertThat(startOfDay1.minute).isEqualTo(0)
        assertThat(startOfDay1.second).isEqualTo(0)

        assertThat(startOfDay2.hour).isEqualTo(0)
        assertThat(startOfDay2.minute).isEqualTo(0)
        assertThat(startOfDay2.second).isEqualTo(0)
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
    fun `isWithinWorkingHour - given date-time few minutes before working hour - provides false`() {
        val dateTime = Instant.parse("2022-09-05T08:52:32-04:00").toZdt() // 08:52am Monday
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

    // region Tests by AI

    @Test
    fun `startOfDay - given ZonedDateTime - returns ZonedDateTime at start of day`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-05T10:00:00-04:00[America/New_York]")

        val startOfDay = zonedDateTime.startOfDay()

        assertThat(startOfDay.hour).isEqualTo(0)
        assertThat(startOfDay.minute).isEqualTo(0)
        assertThat(startOfDay.second).isEqualTo(0)
    }

    @Test
    fun `nextWorkingDay - given ZonedDateTime on weekend - returns ZonedDateTime on next Monday`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-04T10:00:00-04:00[America/New_York]") // Sunday

        val nextWorkingDay = zonedDateTime.nextWorkingDay()

        assertThat(nextWorkingDay.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    }

    @Test
    fun `nextWorkingDay - given ZonedDateTime on weekday - returns ZonedDateTime on next day`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-05T10:00:00-04:00[America/New_York]") // Monday

        val nextWorkingDay = zonedDateTime.nextWorkingDay()

        assertThat(nextWorkingDay.dayOfWeek).isEqualTo(DayOfWeek.TUESDAY)
    }

    @Test
    fun `nextWorkingDayOrSame - given ZonedDateTime on weekend - returns ZonedDateTime on next Monday`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-04T10:00:00-04:00[America/New_York]") // Sunday

        val nextWorkingDayOrSame = zonedDateTime.nextWorkingDayOrSame()

        assertThat(nextWorkingDayOrSame.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    }

    @Test
    fun `nextWorkingDayOrSame - given ZonedDateTime on weekday - returns same ZonedDateTime`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-05T10:00:00-04:00[America/New_York]") // Monday

        val nextWorkingDayOrSame = zonedDateTime.nextWorkingDayOrSame()

        assertThat(nextWorkingDayOrSame).isEqualTo(zonedDateTime)
    }

    @Test
    fun `nextWorkingHourOrSame - given ZonedDateTime on weekend - returns same ZonedDateTime`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-04T10:00:00-04:00[America/New_York]") // Sunday

        val nextWorkingHourOrSame = zonedDateTime.nextWorkingHourOrSame()

        assertThat(nextWorkingHourOrSame).isEqualTo(zonedDateTime)
    }

    @Test
    fun `nextWorkingHourOrSame - given ZonedDateTime on weekday during working hours - returns same ZonedDateTime`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-05T10:00:00-04:00[America/New_York]") // Monday

        val nextWorkingHourOrSame = zonedDateTime.nextWorkingHourOrSame()

        assertThat(nextWorkingHourOrSame).isEqualTo(zonedDateTime)
    }

    @Test
    fun `nextWorkingHourOrSame - given ZonedDateTime on weekday after working hours - returns ZonedDateTime at start of next working day`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-05T18:00:00-04:00[America/New_York]") // Monday

        val nextWorkingHourOrSame = zonedDateTime.nextWorkingHourOrSame()

        assertThat(nextWorkingHourOrSame.dayOfWeek).isEqualTo(DayOfWeek.TUESDAY)
        assertThat(nextWorkingHourOrSame.hour).isEqualTo(9)
    }

    @Test
    fun `nextWorkingHourOrSame - given ZonedDateTime on weekday before working hours - returns ZonedDateTime at start of same working day`() {
        val zonedDateTime: ZonedDateTime = ZonedDateTime.parse("2022-09-05T06:00:00-04:00[America/New_York]") // Monday

        val nextWorkingHourOrSame = zonedDateTime.nextWorkingHourOrSame()

        assertThat(nextWorkingHourOrSame.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(nextWorkingHourOrSame.hour).isEqualTo(9)
    }

    // endregion Tests by AI
}
