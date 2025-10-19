package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for [UserCity] enum.
 */
class UserCityTest {
    @Test
    fun `UserCity enum has expected cities`() {
        val cities = UserCity.entries

        assertThat(cities).hasSize(9)
        assertThat(cities).contains(UserCity.ATLANTA)
        assertThat(cities).contains(UserCity.CHICAGO)
        assertThat(cities).contains(UserCity.DETROIT)
        assertThat(cities).contains(UserCity.NEW_YORK)
        assertThat(cities).contains(UserCity.PHOENIX)
        assertThat(cities).contains(UserCity.PARIS)
        assertThat(cities).contains(UserCity.SAN_FRANCISCO)
        assertThat(cities).contains(UserCity.TORONTO)
        assertThat(cities).contains(UserCity.VANCOUVER)
    }

    @Test
    fun `cityName property returns correct names`() {
        assertThat(UserCity.ATLANTA.cityName).isEqualTo("Atlanta")
        assertThat(UserCity.CHICAGO.cityName).isEqualTo("Chicago")
        assertThat(UserCity.DETROIT.cityName).isEqualTo("Detroit")
        assertThat(UserCity.NEW_YORK.cityName).isEqualTo("New York")
        assertThat(UserCity.PHOENIX.cityName).isEqualTo("Phoenix")
        assertThat(UserCity.PARIS.cityName).isEqualTo("Paris")
        assertThat(UserCity.SAN_FRANCISCO.cityName).isEqualTo("San Francisco")
        assertThat(UserCity.TORONTO.cityName).isEqualTo("Toronto")
        assertThat(UserCity.VANCOUVER.cityName).isEqualTo("Vancouver")
    }

    @Test
    fun `valueOf returns correct enum for string`() {
        assertThat(UserCity.valueOf("NEW_YORK")).isEqualTo(UserCity.NEW_YORK)
        assertThat(UserCity.valueOf("TORONTO")).isEqualTo(UserCity.TORONTO)
        assertThat(UserCity.valueOf("VANCOUVER")).isEqualTo(UserCity.VANCOUVER)
    }

    @Test
    fun `enum values can be used in when expressions`() {
        val city = UserCity.PARIS

        val result =
            when (city) {
                UserCity.ATLANTA -> "US East"
                UserCity.CHICAGO -> "US Central"
                UserCity.DETROIT -> "US East"
                UserCity.NEW_YORK -> "US East"
                UserCity.PHOENIX -> "US West"
                UserCity.PARIS -> "Europe"
                UserCity.SAN_FRANCISCO -> "US West"
                UserCity.TORONTO -> "Canada East"
                UserCity.VANCOUVER -> "Canada West"
            }

        assertThat(result).isEqualTo("Europe")
    }
}
