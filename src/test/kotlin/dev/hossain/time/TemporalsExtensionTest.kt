package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Unit tests for [TemporalsExtension].
 */
class TemporalsExtensionTest {
    @Test
    fun `nextWorkingDay - given Saturday - returns Monday`() {
        val saturday = LocalDateTime.of(2024, 1, 6, 11, 0) // Saturday 11 AM

        val result = saturday.with(TemporalsExtension.nextWorkingDay())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(result.dayOfMonth).isEqualTo(8)
        assertThat(result.hour).isEqualTo(11) // Time should be preserved
    }

    @Test
    fun `nextWorkingDay - given Sunday - returns Monday`() {
        val sunday = LocalDateTime.of(2024, 1, 7, 14, 30) // Sunday 2:30 PM

        val result = sunday.with(TemporalsExtension.nextWorkingDay())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(result.dayOfMonth).isEqualTo(8)
        assertThat(result.hour).isEqualTo(14) // Time should be preserved
    }

    @Test
    fun `nextWorkingDay - given Monday - returns Tuesday`() {
        val monday = LocalDateTime.of(2024, 1, 8, 11, 0) // Monday 11 AM

        val result = monday.with(TemporalsExtension.nextWorkingDay())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.TUESDAY)
        assertThat(result.dayOfMonth).isEqualTo(9)
    }

    @Test
    fun `nextWorkingDay - given Friday - returns Monday`() {
        val friday = LocalDateTime.of(2024, 1, 5, 16, 0) // Friday 4 PM

        val result = friday.with(TemporalsExtension.nextWorkingDay())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(result.dayOfMonth).isEqualTo(8)
    }

    @Test
    fun `nextWorkingDayOrSame - given Saturday - returns Monday`() {
        val saturday = LocalDateTime.of(2024, 1, 6, 11, 0)

        val result = saturday.with(TemporalsExtension.nextWorkingDayOrSame())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    }

    @Test
    fun `nextWorkingDayOrSame - given Sunday - returns Monday`() {
        val sunday = LocalDateTime.of(2024, 1, 7, 11, 0)

        val result = sunday.with(TemporalsExtension.nextWorkingDayOrSame())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    }

    @Test
    fun `nextWorkingDayOrSame - given Monday - returns same Monday`() {
        val monday = LocalDateTime.of(2024, 1, 8, 11, 0)

        val result = monday.with(TemporalsExtension.nextWorkingDayOrSame())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(result.dayOfMonth).isEqualTo(8) // Same day
    }

    @Test
    fun `nextWorkingDayOrSame - given Wednesday - returns same Wednesday`() {
        val wednesday = LocalDateTime.of(2024, 1, 10, 15, 0)

        val result = wednesday.with(TemporalsExtension.nextWorkingDayOrSame())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.WEDNESDAY)
        assertThat(result.dayOfMonth).isEqualTo(10) // Same day
    }

    @Test
    fun `nextWorkingHourOrSame - given Monday 11 AM - returns same time (already working hour)`() {
        val monday = LocalDateTime.of(2024, 1, 8, 11, 0) // Monday 11 AM

        val result = monday.with(TemporalsExtension.nextWorkingHourOrSame())

        assertThat(result.hour).isEqualTo(11)
        assertThat(result.dayOfMonth).isEqualTo(8)
    }

    @Test
    fun `nextWorkingHourOrSame - given Tuesday 8 PM - returns Wednesday 9 AM`() {
        val tuesday = LocalDateTime.of(2024, 1, 9, 20, 0) // Tuesday 8 PM (after hours)

        val result = tuesday.with(TemporalsExtension.nextWorkingHourOrSame())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.WEDNESDAY)
        assertThat(result.hour).isEqualTo(9) // Start of work day
        assertThat(result.minute).isEqualTo(0)
    }

    @Test
    fun `nextWorkingHourOrSame - given Tuesday 6 AM - returns Tuesday 9 AM`() {
        val tuesday = LocalDateTime.of(2024, 1, 9, 6, 0) // Tuesday 6 AM (before hours)

        val result = tuesday.with(TemporalsExtension.nextWorkingHourOrSame())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.TUESDAY)
        assertThat(result.dayOfMonth).isEqualTo(9)
        assertThat(result.hour).isEqualTo(9) // Start of work day
    }

    @Test
    fun `nextWorkingHourOrSame - given Saturday - returns same time (weekend preserved)`() {
        val saturday = LocalDateTime.of(2024, 1, 6, 11, 0) // Saturday 11 AM

        val result = saturday.with(TemporalsExtension.nextWorkingHourOrSame())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.SATURDAY)
        assertThat(result.hour).isEqualTo(11) // Same time on weekend
    }

    @Test
    fun `startOfDay - resets time to midnight`() {
        val dateTime = LocalDateTime.of(2024, 1, 10, 14, 43, 5) // Wednesday 2:43:05 PM

        val result = dateTime.with(TemporalsExtension.startOfDay())

        assertThat(result.dayOfMonth).isEqualTo(10)
        assertThat(result.hour).isEqualTo(0)
        assertThat(result.minute).isEqualTo(0)
        assertThat(result.second).isEqualTo(0)
    }

    @Test
    fun `startOfDay - given different times same day - all return same midnight`() {
        val morning = LocalDateTime.of(2024, 1, 10, 8, 30, 0)
        val afternoon = LocalDateTime.of(2024, 1, 10, 14, 45, 30)
        val evening = LocalDateTime.of(2024, 1, 10, 22, 15, 45)

        val result1 = morning.with(TemporalsExtension.startOfDay())
        val result2 = afternoon.with(TemporalsExtension.startOfDay())
        val result3 = evening.with(TemporalsExtension.startOfDay())

        assertThat(result1).isEqualTo(result2)
        assertThat(result2).isEqualTo(result3)
        assertThat(result1.hour).isEqualTo(0)
        assertThat(result1.minute).isEqualTo(0)
        assertThat(result1.second).isEqualTo(0)
    }

    @Test
    fun `prevWorkingHour - given Monday 11 AM - returns Monday 9 AM`() {
        val monday = LocalDateTime.of(2024, 1, 8, 11, 0) // Monday 11 AM

        val result = monday.with(TemporalsExtension.prevWorkingHour())

        assertThat(result.dayOfMonth).isEqualTo(8)
        assertThat(result.hour).isEqualTo(9) // Start of working hours
    }

    @Test
    fun `prevWorkingHour - given Tuesday 8 PM - returns Tuesday 5 PM (end of day)`() {
        val tuesday = LocalDateTime.of(2024, 1, 9, 20, 0) // Tuesday 8 PM

        val result = tuesday.with(TemporalsExtension.prevWorkingHour())

        assertThat(result.dayOfMonth).isEqualTo(9)
        assertThat(result.hour).isEqualTo(17) // 5 PM end of work day
    }

    @Test
    fun `prevWorkingHour - given Tuesday 6 AM - returns Monday 5 PM (previous day end)`() {
        val tuesday = LocalDateTime.of(2024, 1, 9, 6, 0) // Tuesday 6 AM (before work)

        val result = tuesday.with(TemporalsExtension.prevWorkingHour())

        assertThat(result.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(result.dayOfMonth).isEqualTo(8)
        assertThat(result.hour).isEqualTo(17) // 5 PM end of previous work day
    }

    @Test
    fun `nextNonWorkingHourOrSame - given working hour - returns end of work day`() {
        val monday = LocalDateTime.of(2024, 1, 8, 11, 0) // Monday 11 AM (during work)

        val result = monday.with(TemporalsExtension.nextNonWorkingHourOrSame())

        assertThat(result.dayOfMonth).isEqualTo(8)
        assertThat(result.hour).isEqualTo(17) // 5 PM end of work
    }

    @Test
    fun `nextNonWorkingHourOrSame - given after hours - returns same time`() {
        val monday = LocalDateTime.of(2024, 1, 8, 20, 0) // Monday 8 PM (after work)

        val result = monday.with(TemporalsExtension.nextNonWorkingHourOrSame())

        assertThat(result.dayOfMonth).isEqualTo(8)
        assertThat(result.hour).isEqualTo(20) // Same time (already non-working)
    }
}
