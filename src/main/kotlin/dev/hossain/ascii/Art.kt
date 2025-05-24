package dev.hossain.ascii

import dev.hossain.time.UserTimeZone
import java.util.Locale
import java.util.ResourceBundle

/**
 * Contains some ASCII art for fun! ^_^
 */
object Art {
    private val bundle = ResourceBundle.getBundle("strings", Locale.getDefault())

    fun coffee(): String {
        // Tea/Coffee art by Elissa Potier
        return bundle.getString("art_coffee")
    }

    /**
     * Meh! just 🤷
     */
    const val SHRUG = "¯\\_(ツ)_/¯"

    /**
     * Warns library user about the provided review time which can't be used literaly.
     * They are for reference only to get sense of time.
     *
     * There are many limitations on why accurate PR review time can't be calculated.
     * Such as:
     * - PR reviewer's time zone is not known (though it can be configured in [UserTimeZone]).
     * - Weekends may not be same in all countries
     * - Holidays or days off by PR reviewer are not considered
     * - Similar to holidays there is no way to input unexpected absence.
     * - The tool defined here to diff time likely has bugs/flaws.
     *
     * Art source: https://www.asciiart.eu/miscellaneous/signs
     */
    fun warnAboutReviewTime(): String = bundle.getString("art_warning_review_time")
}
