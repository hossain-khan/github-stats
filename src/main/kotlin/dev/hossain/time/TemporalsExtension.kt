package dev.hossain.time

import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAdjuster

/**
 * Contains some utility, helpful [TemporalAdjuster].
 * Some adjusters are borrowed from [`org.threeten.extra`](https://www.threeten.org/threeten-extra/apidocs/org.threeten.extra/org/threeten/extra/Temporals.html).
 */
object TemporalsExtension {
    /**
     * Returns an adjuster that returns the next working day, ignoring Saturday and Sunday.
     *
     * Some territories have weekends that do not consist of Saturday and Sunday.
     * No implementation is supplied to support this, however an adjuster
     * can be easily written to do so.
     *
     * Example:
     *  - Current Date Time: Saturday 11 AM --> Monday 11 AM (nex working day - excluding weekends)
     *  - Current Date Time: Sunday 11 AM   --> Monday 11 AM (nex working day - excluding weekends)
     *  - Current Date Time: Monday 11 AM   --> Tuesday 11 AM (nex working day)
     *
     * @return the next working day adjuster, not null
     */
    fun nextWorkingDay(): TemporalAdjuster {
        return Adjuster.NEXT_WORKING
    }

    /**
     * Returns an adjuster that returns the next working day or same day if already working day, ignoring Saturday and Sunday.
     *
     *
     * Some territories have weekends that do not consist of Saturday and Sunday.
     * No implementation is supplied to support this, however an adjuster
     * can be easily written to do so.
     *
     * @return the next working day or same adjuster, not null
     */
    fun nextWorkingDayOrSame(): TemporalAdjuster {
        return Adjuster.NEXT_WORKING_OR_SAME
    }

    /**
     * Provides adjuster that gives next end of day working hour or same if it's already non-working hour.
     */
    fun nextNonWorkingHourOrSame(): TemporalAdjuster {
        return Adjuster.NEXT_NON_WORKING_HOUR_OR_SAME
    }

    /**
     * Adjuster to provide next working hour on a working day.
     *
     * Example:
     *  - Current Date Time: Saturday 11 AM --> Saturday 11 AM (Same - because on non-weekday)
     *  - Current Date Time: Sunday 11 AM   --> Sunday 11 AM (Same - because on non-weekday)
     *  - Current Date Time: Monday 11 AM   --> Monday 11 AM (Same - because it's already in working hour)
     *  - Current Date Time: Tuesday 8 PM   --> Wednesday 9 AM (Next day, because it was after hours)
     *  - Current Date Time: Tuesday 6 AM   --> Tuesday 9 AM (Same day but time changed to start of work)
     */
    fun nextWorkingHourOrSame(): TemporalAdjuster {
        return Adjuster.NEXT_WORKING_HOUR_OR_SAME
    }

    /**
     * Previous working start hour of the day irrespective of weekday or weekends.
     *
     * Example:
     *  - Current Date Time: Saturday 11 AM --> Saturday 9 AM
     *  - Current Date Time: Sunday 2 PM    --> Sunday 9 AM
     *  - Current Date Time: Monday 11 AM   --> Monday 9 AM
     *  - Current Date Time: Tuesday 8 PM   --> Tuesday 5 PM (End of the day for same day)
     *  - Current Date Time: Tuesday 6 AM   --> Monday 5 PM (End of the day for previous day)
     */
    fun prevWorkingHour(): TemporalAdjuster {
        return Adjuster.PREV_WORKING_HOUR
    }

    /**
     * Provides (approximate) time that indicates start of day at 12:00am of that day.
     *
     * Example:
     *  - Current Date Time: Saturday 11 AM --> Saturday 12 AM (Reset to 12:00 am)
     *  - Current Date Time: Sunday 11 AM   --> Sunday 12 AM (Reset to 12:00 am)
     *  - Current Date Time: Monday 2 PM    --> Monday 12 AM (Reset to 12:00 am)
     *  - Current Date Time: Tuesday 2:43:05 AM --> Tuesday 12:00:00 AM
     *  - Current Date Time: Wednesday 10:37:39 AM --> Wednesday 12:00:00 AM
     */
    fun startOfDay(): TemporalAdjuster {
        return Adjuster.START_OF_DAY
    }

    // -----------------------------------------------------------------------
    /**
     * Enum implementing the adjusters.
     */
    private enum class Adjuster : TemporalAdjuster {
        /** Next working day adjuster.  */
        NEXT_WORKING {
            override fun adjustInto(temporal: Temporal): Temporal {
                return when (temporal[ChronoField.DAY_OF_WEEK]) {
                    6 -> temporal.plus(2, ChronoUnit.DAYS)
                    5 -> temporal.plus(3, ChronoUnit.DAYS)
                    else -> temporal.plus(1, ChronoUnit.DAYS)
                }
            }
        },

        /**
         * Temporal that sets date time to beginning of the day at 12am same day.
         *
         * Example:
         * - Input:  Tuesday, February 22, 2022 at 2:43:05 AM Eastern Standard Time
         * - Output: Tuesday, February 22, 2022 at 12:00:00 AM Eastern Standard Time
         *
         * - Input:  Wednesday, September 21, 2022 at 10:37:39 AM Eastern Daylight Time
         * - Output: Wednesday, September 21, 2022 at 12:00:00 AM Eastern Daylight Time
         */
        START_OF_DAY {
            override fun adjustInto(temporal: Temporal): Temporal {
                // Sets the day to at 12:00AM
                return temporal.minus(temporal[ChronoField.HOUR_OF_DAY].toLong(), ChronoUnit.HOURS)
                    .minus(temporal[ChronoField.MINUTE_OF_HOUR].toLong(), ChronoUnit.MINUTES)
                    .minus(temporal[ChronoField.SECOND_OF_MINUTE].toLong(), ChronoUnit.SECONDS)
            }
        },

        /**
         * Adjuster that gives next end of day working hour or same if it's already non-working hour.
         * TODO - consider day too
         */
        NEXT_NON_WORKING_HOUR_OR_SAME {
            override fun adjustInto(temporal: Temporal): Temporal {
                return when (val hour = temporal[ChronoField.HOUR_OF_DAY]) {
                    in 0..17 -> {
                        // When it's within 12:00AM to 5:00PM - shift to after 5:00pm
                        temporal.plus(17 - hour.toLong(), ChronoUnit.HOURS)
                    }

                    else -> temporal
                }
            }
        },

        /**
         * Next working hour adjuster.
         * Provides next working hour or same if already in working hour.
         *
         * - 12:00am to 8:00AM -> 9:00 AM
         * - 05:00pm to 11:59pm -> Next day 9:00AM
         *
         * For example:
         * - Input: 7:30PM -> 9:00AM (next day)
         * - Input: 6:00AM -> 9:00AM (same day)
         * - Input: 2:00AM -> 9:00AM (same day)
         */
        NEXT_WORKING_HOUR_OR_SAME {
            override fun adjustInto(temporal: Temporal): Temporal {
                return when (val hour = temporal[ChronoField.HOUR_OF_DAY]) {
                    in 0..8 -> temporal.plus((9 - hour).toLong(), ChronoUnit.HOURS)
                    in 17..23 -> {
                        // Set the temporal to next day @ 9:00am
                        temporal.plus((24 - hour + 9).toLong(), ChronoUnit.HOURS)
                    }
                    else -> temporal
                }
            }
        },

        /**
         * Previous working start hour of the day.
         *
         * - 12:00am to 8:00am -> 5:00 PM previous day
         * - 05:00pm to 11:59pm -> 5:00 PM same day
         *
         * For example:
         * - Input: 7:30PM -> 5:00PM (same day)
         * - Input: 6:00AM -> 5:00PM (prev day)
         * - Input: 2:00AM -> 5:00PM (prev day)
         * - Input: 3:30PM -> 9:00AM (same day)
         */
        PREV_WORKING_HOUR {
            override fun adjustInto(temporal: Temporal): Temporal {
                return when (val hour = temporal[ChronoField.HOUR_OF_DAY]) {
                    in 9..17 -> temporal.minus((hour - 9).toLong(), ChronoUnit.HOURS)
                        .minus(temporal[ChronoField.MINUTE_OF_HOUR].toLong(), ChronoUnit.MINUTES)
                    in 0..8 -> {
                        // Set time to end of the day on previous day
                        temporal.minus((hour + 7).toLong(), ChronoUnit.HOURS)
                            .minus(temporal[ChronoField.MINUTE_OF_HOUR].toLong(), ChronoUnit.MINUTES)
                    }

                    in 18..23 -> {
                        // Set them to end of the day @ 5:00pm
                        temporal.minus((hour - 17).toLong(), ChronoUnit.HOURS)
                            .minus(temporal[ChronoField.MINUTE_OF_HOUR].toLong(), ChronoUnit.MINUTES)
                    }

                    else -> temporal
                }
            }
        },

        /** Next working day or same adjuster.  */
        NEXT_WORKING_OR_SAME {
            override fun adjustInto(temporal: Temporal): Temporal {
                return when (temporal[ChronoField.DAY_OF_WEEK]) {
                    6 -> temporal.plus(2, ChronoUnit.DAYS)
                    7 -> temporal.plus(1, ChronoUnit.DAYS)
                    else -> temporal
                }
            }
        },
    }
}
