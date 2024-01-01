package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test

/**
 * Contains unit tests for [InstantExtensionKt].
 */
class InstantExtensionKtTest {
    @Test
    fun `toZdt - given Instant - returns ZonedDateTime at New York timezone`() {
        val instant: Instant = Instant.parse("2022-09-05T10:00:00Z")

        val zonedDateTime = instant.toZdt()

        assertThat(zonedDateTime.zone.id).isEqualTo("America/New_York")
    }

    @Test
    fun `format - given Instant - returns formatted string in New York timezone`() {
        val instant: Instant = Instant.parse("2022-09-05T10:00:00Z")

        val formattedString = instant.format()

        // Expected: Monday, September 5, 2022 at 6:00:00 AM Eastern Daylight Time
        // However, splitted text because of different result in CI and local gradle run.
        assertThat(formattedString).contains("Monday, September 5, 2022")
        assertThat(formattedString).contains("6:00:00")
        assertThat(formattedString).contains("Eastern Daylight Time")
    }
}
