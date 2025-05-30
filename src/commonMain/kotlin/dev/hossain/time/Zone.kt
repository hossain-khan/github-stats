package dev.hossain.time

import dev.hossain.time.UserCity.ATLANTA
import dev.hossain.time.UserCity.CHICAGO
import dev.hossain.time.UserCity.DETROIT
import dev.hossain.time.UserCity.NEW_YORK
import dev.hossain.time.UserCity.PHOENIX
import dev.hossain.time.UserCity.SAN_FRANCISCO
import dev.hossain.time.UserCity.TORONTO
import dev.hossain.time.UserCity.VANCOUVER
import kotlinx.datetime.TimeZone // Use kotlinx.datetime.TimeZone

/**
 * Time zone related configs and constants using [kotlinx.datetime.TimeZone].
 * @see UserTimeZone
 */
object Zone {
    /**
     * Convenient map to get [TimeZone] for some known locations.
     * Time zone IDs are IANA time zone database names (e.g., "America/New_York").
     */
    val cities: Map<String, TimeZone> =
        mapOf(
            ATLANTA to TimeZone.of("America/New_York"),
            CHICAGO to TimeZone.of("America/Chicago"),
            DETROIT to TimeZone.of("America/Detroit"),
            NEW_YORK to TimeZone.of("America/New_York"),
            PHOENIX to TimeZone.of("America/Phoenix"),
            SAN_FRANCISCO to TimeZone.of("America/Los_Angeles"),
            TORONTO to TimeZone.of("America/Toronto"),
            VANCOUVER to TimeZone.of("America/Vancouver"),
        )

    /**
     * Provides [TimeZone] based on known [cityName] defined in [UserCity].
     * @throws IllegalArgumentException if [cityName] is not in [cities] map.
     */
    fun city(cityName: String): TimeZone = requireNotNull(cities[cityName]) { "Please add $cityName to Zone.cities map first." }
}
