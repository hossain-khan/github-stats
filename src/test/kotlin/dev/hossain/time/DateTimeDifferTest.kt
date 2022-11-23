package dev.hossain.time

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import kotlin.time.Duration

internal class DateTimeDifferTest {
    /*
     * System default on my machine is: `America/Toronto`
     * Currently Eastern Daylight Time (EDT), UTC -4
     * Standard time (Eastern Standard Time (EST), UTC -5) starts Nov. 6, 2022
     */
    private val zoneId: ZoneId = ZoneId.of("America/Toronto")

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    // - Sample: Mon, 05 Sep 2022 10:00:00 -0400 @ https://timestampgenerator.com/1662386400/-04:00

    @Test
    fun `diff - given start time is after end time - throws error`() {
        val startTime: Instant = Instant.parse("2022-09-05T11:30:00-04:00")
        val endTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00")

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)
        }
    }

    @Test
    fun `diff - given start time and end time both in working day and hour - provides right diff`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
        val endTime: Instant = Instant.parse("2022-09-05T11:30:00-04:00") // 11:30am same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("1h 30m".duration())
    }

    @Test
    fun `diff - given start time and end time both in non-working day but in working hour - provides zero working hour`() {
        val startTime: Instant = Instant.parse("2022-09-03T10:00:00-04:00") // 10:00am Saturday
        val endTime: Instant = Instant.parse("2022-09-03T11:30:00-04:00") // 11:30am same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `diff - given start time and end time both in non-working day and hour and spans two days - provides zero working hour`() {
        val startTime: Instant = Instant.parse("2022-09-03T10:00:00-04:00") // 10:00am Saturday
        val endTime: Instant = Instant.parse("2022-09-04T20:00:00-04:00") // 8:00pm Sunday

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `diff - given start time and end time both in working day but end time is outside working hour - provides diff excluding non-working hours`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
        val endTime: Instant = Instant.parse("2022-09-05T20:00:00-04:00") // 8:00pm same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("7h".duration())
    }

    @Test
    fun `diff - given start time and end time both in working day but start time is outside working hour - provides diff excluding non-working hours`() {
        val startTime: Instant = Instant.parse("2022-09-05T06:00:00-04:00") // 06:00am
        val endTime: Instant = Instant.parse("2022-09-05T12:00:00-04:00") // 12:00pm same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("3h".duration())
    }

    @Test
    fun `diff - given start time and end time both in working day but start & end time is before working hour - provides almost diff zero`() {
        val startTime: Instant = Instant.parse("2022-09-05T06:00:00-04:00") // 06:00am
        val endTime: Instant = Instant.parse("2022-09-05T07:00:00-04:00") // 07:00am same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `diff - given start time and end time both in working day but start & end time is after working hour - provides almost diff zero`() {
        val startTime: Instant = Instant.parse("2022-09-05T18:00:00-04:00") // 06:00pm
        val endTime: Instant = Instant.parse("2022-09-05T21:00:00-04:00") // 09:00pm same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `diff - given start time and end time both in working day but start & end time is before and after working hour - provides working hour diff`() {
        val startTime: Instant = Instant.parse("2022-09-05T06:00:00-04:00") // 06:00am
        val endTime: Instant = Instant.parse("2022-09-05T21:00:00-04:00") // 09:00pm same day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("8h".duration())
    }

    /**
     * PR Ready for Review @ 2022-09-13T12:11:30Z
     *
     * UNIX Time [ms]
     * 1663071090000
     * W3C-DTF Date
     * 2022-09-13T08:11:30-04:00
     * ISO8601 Date
     * 20220913T081130-0400
     * ISO8601 Date (Extend)
     * 2022-09-13T08:11:30-04:00
     * ISO8601 Date (Week)
     * 2022-W37-2T08:11:30-04:00
     * ISO8601 Date (Ordinal)
     * 2022-256T08:11:30-04:00
     * RFC2822 Date
     * Tue, 13 Sep 2022 08:11:30 EDT
     * ctime Date
     * Tue Sep 13 08:11:30 2022
     *
     *
     * Review Submitted @ 2022-09-13T13:07:11Z
     *
     * UNIX Time [ms]
     * 1663074431000
     * W3C-DTF Date
     * 2022-09-13T09:07:11-04:00
     * ISO8601 Date
     * 20220913T090711-0400
     * ISO8601 Date (Extend)
     * 2022-09-13T09:07:11-04:00
     * ISO8601 Date (Week)
     * 2022-W37-2T09:07:11-04:00
     * ISO8601 Date (Ordinal)
     * 2022-256T09:07:11-04:00
     * RFC2822 Date
     * Tue, 13 Sep 2022 09:07:11 EDT
     * ctime Date
     * Tue Sep 13 09:07:11 2022
     */
    @Test
    fun `diff - given start time is before working hour and end time during work hour - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-13T12:11:30Z") // 08:11:30 AM
        val endTime: Instant = Instant.parse("2022-09-13T13:07:11Z") // 09:07:11 AM

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("7m".duration())
    }

    @Test
    fun `diff - given start time in working hour and end time is next day working hour - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
        val endTime: Instant = Instant.parse("2022-09-06T10:00:00-04:00") // 10:00am next day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("8h".duration())
    }

    @Test
    fun `diff - given start time in working hour and end time is next day after working hour - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am
        val endTime: Instant = Instant.parse("2022-09-06T20:00:00-04:00") // 08:00pm next day

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("15h".duration())
    }

    @Test
    fun `diff - given start time and end time spans multiple working days - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am Monday
        val endTime: Instant = Instant.parse("2022-09-08T10:00:00-04:00") // 10:00am 3 days layer

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("24h".duration())
    }

    @Test
    fun `diff - given start time and end time spans whole week - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am Monday
        val endTime: Instant = Instant.parse("2022-09-09T16:00:00-04:00") // 04:00pm Friday

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("38h".duration())
    }

    @Test
    fun `diff - given start time before weekend and end time after weekend - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-01T10:00:00-04:00") // 10:00am Thursday
        val endTime: Instant = Instant.parse("2022-09-05T10:00:00-04:00") // 10:00am Monday next week

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("16h".duration())
    }

    @Test
    fun `diff - given start time before weekend and end time few days after weekend - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-01T10:00:00-04:00") // 10:00am Thursday
        val endTime: Instant = Instant.parse("2022-09-07T10:00:00-04:00") // 10:00am Wednesday next week

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("32h".duration())
    }

    @Test
    fun `diff - given start time during weekend and end time after weekend - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-03T10:00:00-04:00") // 10:00am Saturday
        val endTime: Instant = Instant.parse("2022-09-05T11:00:00-04:00") // 11:00am Monday next week

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("2h".duration())
    }

    @Test
    fun `diff - given start time end of week outside working hour and end time after weekend - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-02T20:00:00-04:00") // 08:00pm Friday
        val endTime: Instant = Instant.parse("2022-09-05T11:00:00-04:00") // 11:00am Monday next week

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("2h".duration())
    }

    @Test
    fun `diff - given start time outside working hour week before and end time after weekend - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-01T20:00:00-04:00") // 08:00pm Thursday
        val endTime: Instant = Instant.parse("2022-09-05T11:00:00-04:00") // 11:00am Monday next week

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("10h".duration())
    }

    @Test
    fun `diff - given multiple weekends between start and end time - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-01T14:00:00-04:00") // 02:00pm Thursday
        val endTime: Instant = Instant.parse("2022-09-13T14:00:00-04:00") // 2:00pm Tuesday 2 weeks later

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("64h".duration())
    }

    @Test
    fun `diff - given multiple weekends between start and end time also during weekends - provides diff of working hour only`() {
        val startTime: Instant = Instant.parse("2022-09-03T10:00:00-04:00") // 10:00am Saturday
        val endTime: Instant = Instant.parse("2022-09-18T20:00:00-04:00") // 8:00pm Sunday 2 weeks later

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("80h".duration())
    }

    @Test
    fun `diff - given start time is during weekend and ends in the middle of week after hours - provides diff of working hour only`() {
        val startTime: Instant = "2022-09-17T22:28:41Z".toInstant() // Saturday, September 17, 2022 at 6:28:41 PM Eastern Daylight
        val endTime: Instant = "2022-09-22T11:51:40Z".toInstant() // Thursday, September 22, 2022 at 7:51:40 AM Eastern Daylight Time

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("24h".duration()) // Indicates 8h x 3 = 24 hours for 3 working days
        assertThat(diffWorkingHours).isEqualTo("1d".duration()) // This is same as 24 hours, which is 3 working days
    }

    @Test
    fun `diff - given start time is a day before weekend - provides diff of working hour only`() {
        val startTime: Instant = "2022-10-28T12:22:06Z".toInstant() // Friday, October 28, 2022 at 8:22:06 AM EDT
        val endTime: Instant = "2022-10-29T06:09:16Z".toInstant() // Saturday, October 29, 2022 at 2:09:16 AM EDT

        val diffWorkingHours = DateTimeDiffer.diffWorkingHours(startTime, endTime, zoneId)

        assertThat(diffWorkingHours).isEqualTo("8h".duration())
    }

    private fun String.duration(): Duration = Duration.parse(this)
}
