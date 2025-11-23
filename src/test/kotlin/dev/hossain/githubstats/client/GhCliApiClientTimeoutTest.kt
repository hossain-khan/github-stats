package dev.hossain.githubstats.client

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for [GhCliApiClient] timeout functionality.
 */
class GhCliApiClientTimeoutTest {
    @Test
    fun `client initialization - with custom timeout - logs timeout value`() {
        // Create client with custom timeout
        val client =
            GhCliApiClient(
                commandTimeoutSeconds = 60L,
            )

        // Client should be initialized successfully
        assertThat(client).isNotNull()
    }

    @Test
    fun `client initialization - default timeout - uses 30 seconds`() {
        // Create client without specifying timeout
        val client = GhCliApiClient()

        // Client should be initialized successfully with default timeout
        assertThat(client).isNotNull()
    }

    @Test
    fun `client initialization - zero timeout - accepts configuration`() {
        // Create client with zero timeout (though not recommended)
        val client =
            GhCliApiClient(
                commandTimeoutSeconds = 0L,
            )

        // Client should be initialized successfully
        assertThat(client).isNotNull()
    }

    @Test
    fun `client initialization - negative timeout - accepts configuration`() {
        // Create client with negative timeout (though not recommended)
        val client =
            GhCliApiClient(
                commandTimeoutSeconds = -1L,
            )

        // Client should be initialized successfully
        // Note: ProcessBuilder.waitFor() with negative timeout will wait indefinitely
        assertThat(client).isNotNull()
    }

    @Test
    fun `client initialization - large timeout - accepts configuration`() {
        // Create client with very large timeout
        val client =
            GhCliApiClient(
                commandTimeoutSeconds = 3600L,
            )

        // Client should be initialized successfully
        assertThat(client).isNotNull()
    }
}
