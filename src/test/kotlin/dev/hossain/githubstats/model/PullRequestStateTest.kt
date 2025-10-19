package dev.hossain.githubstats.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for [PullRequestState] enum.
 */
class PullRequestStateTest {
    @Test
    fun `PullRequestState has expected states`() {
        val states = PullRequestState.entries

        assertThat(states).hasSize(3)
        assertThat(states).contains(PullRequestState.OPEN)
        assertThat(states).contains(PullRequestState.CLOSED)
        assertThat(states).contains(PullRequestState.ALL)
    }

    @Test
    fun `valueOf returns correct state for string`() {
        assertThat(PullRequestState.valueOf("OPEN")).isEqualTo(PullRequestState.OPEN)
        assertThat(PullRequestState.valueOf("CLOSED")).isEqualTo(PullRequestState.CLOSED)
        assertThat(PullRequestState.valueOf("ALL")).isEqualTo(PullRequestState.ALL)
    }

    @Test
    fun `enum values can be used in when expressions`() {
        val state = PullRequestState.ALL

        val result =
            when (state) {
                PullRequestState.OPEN -> "still open"
                PullRequestState.CLOSED -> "was closed"
                PullRequestState.ALL -> "all states"
            }

        assertThat(result).isEqualTo("all states")
    }

    @Test
    fun `enum can be compared with equality`() {
        val state1 = PullRequestState.OPEN
        val state2 = PullRequestState.OPEN
        val state3 = PullRequestState.CLOSED

        assertThat(state1).isEqualTo(state2)
        assertThat(state1).isNotEqualTo(state3)
    }
}
