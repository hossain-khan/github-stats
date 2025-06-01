package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneId

/**
 * Contains unit tests for [Zone].
 */
class ZoneTest {
    @Test
    fun `city returns correct ZoneId for known UserCity`() {
        assertThat(Zone.city(UserCity.ATLANTA)).isEqualTo(ZoneId.of("America/New_York"))
        assertThat(Zone.city(UserCity.CHICAGO)).isEqualTo(ZoneId.of("America/Chicago"))
        assertThat(Zone.city(UserCity.DETROIT)).isEqualTo(ZoneId.of("America/Detroit"))
        assertThat(Zone.city(UserCity.NEW_YORK)).isEqualTo(ZoneId.of("America/New_York"))
        assertThat(Zone.city(UserCity.PHOENIX)).isEqualTo(ZoneId.of("America/Phoenix"))
        assertThat(Zone.city(UserCity.SAN_FRANCISCO)).isEqualTo(ZoneId.of("America/Los_Angeles"))
        assertThat(Zone.city(UserCity.TORONTO)).isEqualTo(ZoneId.of("America/Toronto"))
        assertThat(Zone.city(UserCity.VANCOUVER)).isEqualTo(ZoneId.of("America/Vancouver"))
    }

    @Test
    fun `city throws NullPointerException for unknown UserCity`() {
        // Create a fake UserCity not in the Zone.cities map

        val exception = kotlin.runCatching { Zone.city(UserCity.PARIS) }.exceptionOrNull()
        assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(exception?.message).contains("Paris")
    }
}
