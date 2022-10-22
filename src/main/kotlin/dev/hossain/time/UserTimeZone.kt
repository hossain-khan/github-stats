package dev.hossain.time

import dev.hossain.githubstats.BuildConfig
import dev.hossain.time.Zone.city
import java.time.ZoneId

class UserTimeZone {
    // Default time zone ID for PR review time calculation
    private val defaultZoneId: ZoneId = city("New York")

    /**
     * Configuration for time zone id for each user.
     * If no user config is defined, it will default to [defaultZoneId].
     *
     * NOTE: All zone id cities are defined in [Zone.cities].
     * Add missing city [Zone.cities] or use [ZoneId.of] to create new time zone id for user.
     *
     * Example map entry for User Zone:
     * ```
     * "user-id" to city("Vancouver"),
     * // or
     * "user-id" to ZoneId.of("America/Los_Angeles")
     * ```
     */
    private val userZones: Map<String, ZoneId> = mapOf(
        "user-id-1" to city("Toronto"),
        "user-id-2" to city("Vancouver")
    )

    /**
     * Provides user's time zone id, if configured in [userZones], otherwise [defaultZoneId] is used.
     */
    fun userTimeZone(userId: String): ZoneId {
        val usersTimeZoneId = userZones[userId]
        return if (usersTimeZoneId != null) {
            if (BuildConfig.DEBUG) {
                println("Using user specific time zone: $usersTimeZoneId for user `$userId`")
            }
            usersTimeZoneId
        } else {
            if (BuildConfig.DEBUG) {
                println("Using default time zone: $defaultZoneId for user `$userId`")
            }
            defaultZoneId
        }
    }
}
