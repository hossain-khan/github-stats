package dev.hossain.time

import dev.hossain.time.UserCity.ATLANTA
import dev.hossain.time.UserCity.CHICAGO
import dev.hossain.time.UserCity.DETROIT
import dev.hossain.time.UserCity.NEW_YORK
import dev.hossain.time.UserCity.PHOENIX
import dev.hossain.time.UserCity.SAN_FRANCISCO
import dev.hossain.time.UserCity.TORONTO
import dev.hossain.time.UserCity.VANCOUVER
import java.time.ZoneId

/**
 * Time zone related configs and constants.
 * @see UserTimeZone
 */
object Zone {
    /**
     * Convenient map to get [ZoneId] for some known locations.
     * REF: https://mkyong.com/java8/java-display-all-zoneid-and-its-utc-offset/
     */
    val cities =
        mapOf(
            ATLANTA to ZoneId.of("America/New_York"),
            CHICAGO to ZoneId.of("America/Chicago"),
            DETROIT to ZoneId.of("America/Detroit"),
            NEW_YORK to ZoneId.of("America/New_York"),
            PHOENIX to ZoneId.of("America/Phoenix"),
            SAN_FRANCISCO to ZoneId.of("America/Los_Angeles"),
            TORONTO to ZoneId.of("America/Toronto"),
            VANCOUVER to ZoneId.of("America/Vancouver"),
        )

    /**
     * Provides [ZoneId] based on known [cityName] defined in [UserCity].
     * @throws NullPointerException if [cityName] is not in [cities]
     */
    fun city(cityName: String): ZoneId = requireNotNull(cities[cityName]) { "Please add $cityName to Zone.cities map first." }
}
