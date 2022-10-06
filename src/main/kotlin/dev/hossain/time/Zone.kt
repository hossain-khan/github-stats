package dev.hossain.time

import java.time.ZoneId

object Zone {
    /**
     * Convenient map to get [ZoneId] for some known locations.
     * REF: https://mkyong.com/java8/java-display-all-zoneid-and-its-utc-offset/
     */
    val cities = mapOf(
        "Atlanta" to ZoneId.of("America/New_York"),
        "New York" to ZoneId.of("America/New_York"),
        "San Francisco" to ZoneId.of("America/Los_Angeles"),
        "Toronto" to ZoneId.of("America/Toronto"),
        "Vancouver" to ZoneId.of("America/Vancouver")
    )
}
