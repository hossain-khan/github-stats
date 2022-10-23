package dev.hossain.time

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
    val cities = mapOf(
        "Atlanta" to ZoneId.of("America/New_York"),
        "Chicago" to ZoneId.of("America/Chicago"),
        "Detroit" to ZoneId.of("America/Detroit"),
        "New York" to ZoneId.of("America/New_York"),
        "Phoenix" to ZoneId.of("America/Phoenix"),
        "San Francisco" to ZoneId.of("America/Los_Angeles"),
        "Toronto" to ZoneId.of("America/Toronto"),
        "Vancouver" to ZoneId.of("America/Vancouver")
    )

    /**
     * Provides [ZoneId] based on known [cityName] defined in [cities].
     * @throws NullPointerException if [cityName] is not in [cities]
     */
    fun city(cityName: String): ZoneId = cities[cityName]!!
}
