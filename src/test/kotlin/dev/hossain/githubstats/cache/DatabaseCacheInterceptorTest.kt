package dev.hossain.githubstats.cache

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for [DatabaseCacheInterceptor].
 */
class DatabaseCacheInterceptorTest {
    private lateinit var mockCacheService: DatabaseCacheService
    private lateinit var mockCacheStatsService: CacheStatsService
    private lateinit var interceptor: DatabaseCacheInterceptor
    private lateinit var mockChain: Interceptor.Chain

    @BeforeEach
    fun setUp() {
        mockCacheService = mockk(relaxed = true)
        mockCacheStatsService = mockk(relaxed = true)
        interceptor = DatabaseCacheInterceptor(mockCacheService, mockCacheStatsService)
        mockChain = mockk(relaxed = true)
    }

    @Test
    fun `intercept passes non-GET requests through chain without checking cache`() {
        val request =
            Request
                .Builder()
                .url("https://api.github.com/repos/testowner/testrepo/pulls")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()

        val expectedResponse =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(201)
                .message("Created")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()

        every { mockChain.request() } returns request
        every { mockChain.proceed(request) } returns expectedResponse

        val response = interceptor.intercept(mockChain)

        assertThat(response).isEqualTo(expectedResponse)
        coVerify(exactly = 0) { mockCacheService.getCachedResponse(any()) }
        verify(exactly = 0) { mockCacheStatsService.recordDatabaseCacheHit(any()) }
    }

    @Test
    fun `intercept passes non-GitHub API requests through chain without checking cache`() {
        val request =
            Request
                .Builder()
                .url("https://example.com/api/data")
                .get()
                .build()

        val expectedResponse =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()

        every { mockChain.request() } returns request
        every { mockChain.proceed(request) } returns expectedResponse

        val response = interceptor.intercept(mockChain)

        assertThat(response).isEqualTo(expectedResponse)
        coVerify(exactly = 0) { mockCacheService.getCachedResponse(any()) }
    }

    @Test
    fun `intercept serves from cache and records hit on cache hit`() {
        val url = "https://api.github.com/repos/testowner/testrepo/pulls/100"
        val request =
            Request
                .Builder()
                .url(url)
                .get()
                .build()

        val cachedJson = """{"id": 100, "title": "Cached PR"}"""
        coEvery { mockCacheService.getCachedResponse(url) } returns cachedJson
        every { mockChain.request() } returns request

        val response = interceptor.intercept(mockChain)

        assertThat(response.code).isEqualTo(200)
        assertThat(response.header("X-Cached-By")).isEqualTo("GitHubStats-DatabaseCache")
        assertThat(response.body!!.string()).isEqualTo(cachedJson)

        verify { mockCacheStatsService.recordDatabaseCacheHit(url) }
        verify(exactly = 0) { mockChain.proceed(any()) }
    }

    @Test
    fun `intercept records miss and caches successful JSON response on cache miss`() {
        val url = "https://api.github.com/repos/testowner/testrepo/pulls/101"
        val request =
            Request
                .Builder()
                .url(url)
                .get()
                .build()

        val networkJson = """{"id": 101, "title": "Network PR"}"""
        val networkResponse =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .header("Content-Type", "application/json; charset=utf-8")
                .body(networkJson.toResponseBody("application/json".toMediaType()))
                .build()

        coEvery { mockCacheService.getCachedResponse(url) } returns null
        every { mockChain.request() } returns request
        every { mockChain.proceed(request) } returns networkResponse

        val response = interceptor.intercept(mockChain)

        assertThat(response.code).isEqualTo(200)
        assertThat(response.body!!.string()).isEqualTo(networkJson)

        verify { mockCacheStatsService.recordDatabaseCacheMiss(url) }
        coVerify { mockCacheService.cacheResponse(url, networkJson, 200) }
    }

    @Test
    fun `intercept does not cache non-JSON response on cache miss`() {
        val url = "https://api.github.com/repos/testowner/testrepo/readme"
        val request =
            Request
                .Builder()
                .url(url)
                .get()
                .build()

        val textResponse =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .header("Content-Type", "text/plain")
                .body("Hello World".toResponseBody("text/plain".toMediaType()))
                .build()

        coEvery { mockCacheService.getCachedResponse(url) } returns null
        every { mockChain.request() } returns request
        every { mockChain.proceed(request) } returns textResponse

        val response = interceptor.intercept(mockChain)

        assertThat(response.code).isEqualTo(200)
        verify { mockCacheStatsService.recordDatabaseCacheMiss(url) }
        coVerify(exactly = 0) { mockCacheService.cacheResponse(any(), any(), any()) }
    }

    @Test
    fun `intercept falls back gracefully to chain when cache error occurs`() {
        val url = "https://api.github.com/repos/testowner/testrepo/pulls/102"
        val request =
            Request
                .Builder()
                .url(url)
                .get()
                .build()

        val fallbackResponse =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()

        coEvery { mockCacheService.getCachedResponse(url) } throws RuntimeException("DB error")
        every { mockChain.request() } returns request
        every { mockChain.proceed(request) } returns fallbackResponse

        val response = interceptor.intercept(mockChain)

        assertThat(response).isEqualTo(fallbackResponse)
        verify { mockChain.proceed(request) }
    }
}
