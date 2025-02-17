package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import kotlin.time.Duration

/**
 * Contains unit tests for [Duration] extensions.
 */
class DurationExtensionKtTest {
    @Test
    fun `toWorkingHour - given duration less than working hours - returns same duration`() {
        val duration: Duration = "2h".duration()

        val result = duration.toWorkingHour()

        assertThat(result).isEqualTo("2h")
    }

    @Test
    fun `toWorkingHour - given duration less than working hours - returns same duration with min`() {
        val duration = "2h 32m".duration()

        val result = duration.toWorkingHour()

        assertThat(result).isEqualTo("2h 32m")
    }

    @Test
    fun `toWorkingHour - given duration equal to working hours - returns one day`() {
        val duration = "8h".duration()

        val result = duration.toWorkingHour()

        assertThat(result).isEqualTo("1 day [Based on 8h on a working day]")
    }

    @Test
    fun `toWorkingHour - given duration more than working hours but less than two working days - returns days and hours`() {
        val duration = "10h".duration()

        val result = duration.toWorkingHour()

        assertThat(result).isEqualTo("1 day and 2h [Based on 8h on a working day]")
    }

    @Test
    fun `toWorkingHour - given duration equal to two working days - returns two days`() {
        val duration = "16h".duration()

        val result = duration.toWorkingHour()

        assertThat(result).isEqualTo("2 days [Based on 8h on a working day]")
    }

    @Test
    fun `toWorkingHour - given duration equal to two working days - returns two days and hours and mins`() {
        val duration = "18h 48m".duration()

        val result = duration.toWorkingHour()

        assertThat(result).isEqualTo("2 days and 2h 48m [Based on 8h on a working day]")
    }

    @Test
    fun `toWorkingHour - given duration of one day - returns three days`() {
        val duration = "24h".duration()

        val result = duration.toWorkingHour()

        assertThat(result).isEqualTo("3 days [Based on 8h on a working day]")
    }

    @Test
    fun `toWorkingHour - given duration of one day and custom working hours - returns custom days`() {
        val duration = "24h".duration()

        val result = duration.toWorkingHour(6)

        assertThat(result).isEqualTo("4 days [Based on 6h on a working day]")
    }

    private fun String.duration(): Duration = Duration.parse(this)
}
