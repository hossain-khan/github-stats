package time

import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAdjuster

/**
 * Some adjusters are borrowed from `org.threeten.extra`.
 * Cleanup required to remove duplicates.
 */
object TemporalsExtension {
    /**
     * Returns an adjuster that returns the next working day, ignoring Saturday and Sunday.
     *
     *
     * Some territories have weekends that do not consist of Saturday and Sunday.
     * No implementation is supplied to support this, however an adjuster
     * can be easily written to do so.
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

    fun nextWorkingHour(): TemporalAdjuster {
        return Adjuster.NEXT_WORKING_HOUR
    }

    fun nextWorkingHourOrSame(): TemporalAdjuster {
        return Adjuster.NEXT_WORKING_HOUR_OR_SAME
    }

    /**
     * Returns an adjuster that returns the previous working day, ignoring Saturday and Sunday.
     *
     *
     * Some territories have weekends that do not consist of Saturday and Sunday.
     * No implementation is supplied to support this, however an adjuster
     * can be easily written to do so.
     *
     * @return the previous working day adjuster, not null
     */
    fun previousWorkingDay(): TemporalAdjuster {
        return Adjuster.PREVIOUS_WORKING
    }

    /**
     * Returns an adjuster that returns the previous working day or same day if already working day, ignoring Saturday and Sunday.
     *
     *
     * Some territories have weekends that do not consist of Saturday and Sunday.
     * No implementation is supplied to support this, however an adjuster
     * can be easily written to do so.
     *
     * @return the previous working day or same adjuster, not null
     */
    fun previousWorkingDayOrSame(): TemporalAdjuster {
        return Adjuster.PREVIOUS_WORKING_OR_SAME
    }

    // -----------------------------------------------------------------------
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

        /** Next working hour adjuster.*/
        NEXT_WORKING_HOUR {
            override fun adjustInto(temporal: Temporal): Temporal {
                return when (val hour = temporal[ChronoField.HOUR_OF_DAY]) {
                    in 0..8 -> temporal.plus(hour.toLong(), ChronoUnit.HOURS)
                    in 18..23 -> temporal.plus(hour.toLong() - 17, ChronoUnit.HOURS)
                    else -> temporal.plus(1, ChronoUnit.HOURS)
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
                    in 18..23 -> {
                        // Set the temporal to next day @ 9:00am
                        temporal.plus((24 - hour + 9).toLong(), ChronoUnit.HOURS)
                    }

                    else -> temporal
                }
            }
        },

        /** Previous working day adjuster.  */
        PREVIOUS_WORKING {
            override fun adjustInto(temporal: Temporal): Temporal {
                return when (temporal[ChronoField.DAY_OF_WEEK]) {
                    1 -> temporal.minus(3, ChronoUnit.DAYS)
                    7 -> temporal.minus(2, ChronoUnit.DAYS)
                    else -> temporal.minus(1, ChronoUnit.DAYS)
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

        /** Previous working day or same adjuster.  */
        PREVIOUS_WORKING_OR_SAME {
            override fun adjustInto(temporal: Temporal): Temporal {
                return when (temporal[ChronoField.DAY_OF_WEEK]) {
                    6 -> temporal.minus(1, ChronoUnit.DAYS)
                    7 -> temporal.minus(2, ChronoUnit.DAYS)
                    else -> temporal
                }
            }
        }
    }
}