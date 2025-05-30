package dev.hossain.time

// This file originally contained JVM-specific TemporalAdjuster implementations.
// Its functionality has been replaced by the `expect` functions in `ExpectedTemporals.kt`
// and their corresponding `actual` implementations on platform-specific source sets (e.g., jvmMain).
// For commonMain, this file is no longer needed in its original form.

/*
Original content example (now to be moved to actual jvmMain implementations):

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAdjuster

internal object TemporalsExtension {
    // ... (all the static TemporalAdjuster fields and methods) ...
}
*/

// Keeping this file for now with a comment, but it could be deleted from commonMain
// if all usages are updated to use the new expect/actual mechanism.
// For KMP, commonMain should not contain platform-specific implementations.
