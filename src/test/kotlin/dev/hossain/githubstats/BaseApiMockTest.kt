package dev.hossain.githubstats

import dev.hossain.githubstats.io.Client
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * Base test class for tests that use MockWebServer to mock GitHub API responses.
 * Provides common setup and teardown for MockWebServer and a helper function to load test resources.
 */
abstract class BaseApiMockTest {
    protected lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setUpMockServer() {
        mockWebServer = MockWebServer()
        mockWebServer.start(60000)
        Client.baseUrl = mockWebServer.url("/")
    }

    @AfterEach
    fun tearDownMockServer() {
        mockWebServer.shutdown()
    }

    /**
     * Loads test resource file content as String.
     * @param jsonResponseFile The name of the JSON file in the test resources directory.
     * @return The content of the file as a String.
     */
    protected fun respond(jsonResponseFile: String): String =
        requireNotNull(this::class.java.getResource("/$jsonResponseFile")) {
            "Test resource file not found: $jsonResponseFile"
        }.readText()
}
