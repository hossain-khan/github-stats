package dev.hossain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

// These expect functions will represent the core logic previously in TemporalsExtension.kt
// The actual implementations will handle the date/time manipulations.
// They operate on an Instant and a TimeZone to correctly apply zoned logic.

expect fun Instant.applyStartOfDay(timeZone: TimeZone): Instant
expect fun Instant.applyNextWorkingDay(timeZone: TimeZone): Instant
expect fun Instant.applyNextWorkingDayOrSame(timeZone: TimeZone): Instant
expect fun Instant.applyNextWorkingHourOrSame(timeZone: TimeZone): Instant
expect fun Instant.applyNextNonWorkingHourOrSame(timeZone: TimeZone): Instant
expect fun Instant.applyPrevWorkingHour(timeZone: TimeZone): Instant
