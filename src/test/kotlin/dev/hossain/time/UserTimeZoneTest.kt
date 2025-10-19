package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId

/**
 * Unit tests for [UserTimeZone].
 */
class UserTimeZoneTest {
    private lateinit var userTimeZone: UserTimeZone

    @BeforeEach
    fun setUp() {
        userTimeZone = UserTimeZone()
    }

    @Test
    fun `get - given user with configured timezone - returns configured timezone`() {
        // The UserTimeZone has hardcoded test users "user-id-1" and "user-id-2"
        // user-id-1 is configured with Toronto timezone
        val result = userTimeZone.get("user-id-1")

        assertThat(result).isEqualTo(Zone.city(UserCity.TORONTO))
        assertThat(result.toString()).isEqualTo("America/Toronto")
    }

    @Test
    fun `get - given user with configured timezone - returns correct Vancouver timezone`() {
        // user-id-2 is configured with Vancouver timezone
        val result = userTimeZone.get("user-id-2")

        assertThat(result).isEqualTo(Zone.city(UserCity.VANCOUVER))
        assertThat(result.toString()).isEqualTo("America/Vancouver")
    }

    @Test
    fun `get - given user without configured timezone - returns default New York timezone`() {
        val result = userTimeZone.get("unknown-user")

        assertThat(result).isEqualTo(Zone.city(UserCity.NEW_YORK))
        assertThat(result.toString()).isEqualTo("America/New_York")
    }

    @Test
    fun `get - given different unconfigured users - all return same default timezone`() {
        val result1 = userTimeZone.get("random-user-1")
        val result2 = userTimeZone.get("random-user-2")
        val result3 = userTimeZone.get("another-user")

        assertThat(result1).isEqualTo(result2)
        assertThat(result2).isEqualTo(result3)
        assertThat(result1).isEqualTo(Zone.city(UserCity.NEW_YORK))
    }

    @Test
    fun `get - given empty user id - returns default timezone`() {
        val result = userTimeZone.get("")

        assertThat(result).isEqualTo(Zone.city(UserCity.NEW_YORK))
    }

    @Test
    fun `get - timezone is valid ZoneId instance`() {
        val result = userTimeZone.get("test-user")

        assertThat(result).isInstanceOf(ZoneId::class.java)
        assertThat(result.id).isNotEmpty()
    }
}
