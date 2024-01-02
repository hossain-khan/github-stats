package dev.hossain.time

import dev.hossain.githubstats.logging.Log
import dev.hossain.time.UserCity.NEW_YORK
import dev.hossain.time.UserCity.TORONTO
import dev.hossain.time.UserCity.VANCOUVER
import dev.hossain.time.Zone.city
import java.time.ZoneId

/**
 * Configuration file for user's time zone that is used to calculate the PR review time
 * based on PR reviewer's local time zone.
 *
 * @see Zone
 * @see DateTimeDiffer
 */
class UserTimeZone {
    // Default time zone ID for PR review time calculation
    private val defaultZoneId: ZoneId = city(NEW_YORK)

    /**
     * Configuration for time zone id for each user.
     * If no user config is defined, it will default to [defaultZoneId].
     *
     * NOTE: All zone id cities are defined in [Zone.cities] using city names from [UserCity].
     * Add missing city [Zone.cities] or use [ZoneId.of] to create new time zone id for user.
     *
     * Example map entry for User Zone:
     * ```
     * "user-id" to city(VANCOUVER),
     * // or
     * "user-id" to ZoneId.of("America/Los_Angeles")
     * ```
     *
     * @see UserCity
     */
    private val userZones: Map<String, ZoneId> = mapOf(
        "user-id-1" to city(TORONTO),
        "user-id-2" to city(VANCOUVER),
    )

    /**
     * Provides user's time zone id, if configured in [userZones], otherwise [defaultZoneId] is used.
     */
    fun get(userId: String): ZoneId {
        val userZoneId = userZones[userId]
        return if (userZoneId != null) {
            Log.v("Found configured $userZoneId time zone for '$userId'.")
            userZoneId
        } else {
            Log.v(
                "Using default $defaultZoneId timezone for '$userId'. " +
                    "Use ${UserTimeZone::class.java.simpleName} to configure time zone for '$userId'.",
            )
            defaultZoneId
        }
    }
}
