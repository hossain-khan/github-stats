package dev.hossain.githubstats.cache

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [OkHttpCacheStatsInterceptor].
 */
class OkHttpCacheStatsInterceptorTest {
    private lateinit var cacheStatsService: CacheStatsService
    private lateinit var interceptor: OkHttpCacheStatsInterceptor
    private lateinit var chain: Interceptor.Chain

    @BeforeEach
    fun setUp() {
        cacheStatsService = mockk(relaxed = true)
        interceptor = OkHttpCacheStatsInterceptor(cacheStatsService)
        chain = mockk()
    }

    @Test
    fun `intercept - given GitHub API request from cache - records cache hit`() {
        val request =
            Request
                .Builder()
                .url("https://api.github.com/repos/test/repo/pulls/123")
                .build()

        val cacheResponse =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()

        val response =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .cacheResponse(cacheResponse)
                .build()

        every { chain.request() } returns request
        every { chain.proceed(request) } returns response

        val result = interceptor.intercept(chain)

        assertThat(result).isEqualTo(response)
        verify(exactly = 1) { cacheStatsService.recordOkHttpCacheHit() }
        verify(exactly = 0) { cacheStatsService.recordNetworkRequest() }
    }

    @Test
    fun `intercept - given GitHub API request from network - records network request`() {
        val request =
            Request
                .Builder()
                .url("https://api.github.com/repos/test/repo/pulls/123")
                .build()

        val networkResponse =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()

        val response =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .networkResponse(networkResponse)
                .build()

        every { chain.request() } returns request
        every { chain.proceed(request) } returns response

        val result = interceptor.intercept(chain)

        assertThat(result).isEqualTo(response)
        verify(exactly = 0) { cacheStatsService.recordOkHttpCacheHit() }
        verify(exactly = 1) { cacheStatsService.recordNetworkRequest() }
    }

    @Test
    fun `intercept - given non-GitHub API request - does not record stats`() {
        val request =
            Request
                .Builder()
                .url("https://example.com/api/data")
                .build()

        val response =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()

        every { chain.request() } returns request
        every { chain.proceed(request) } returns response

        val result = interceptor.intercept(chain)

        assertThat(result).isEqualTo(response)
        verify(exactly = 0) { cacheStatsService.recordOkHttpCacheHit() }
        verify(exactly = 0) { cacheStatsService.recordNetworkRequest() }
    }

    @Test
    fun `intercept - given cache response with Age header - records cache hit`() {
        val request =
            Request
                .Builder()
                .url("https://api.github.com/repos/test/repo")
                .build()

        val cacheResponse =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()

        val response =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .header("Age", "123")
                .cacheResponse(cacheResponse)
                .build()

        every { chain.request() } returns request
        every { chain.proceed(request) } returns response

        val result = interceptor.intercept(chain)

        assertThat(result).isEqualTo(response)
        verify(exactly = 1) { cacheStatsService.recordOkHttpCacheHit() }
    }

    @Test
    fun `intercept - given response without cache or network indicators - records network request`() {
        val request =
            Request
                .Builder()
                .url("https://api.github.com/users/test")
                .build()

        val response =
            Response
                .Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()

        every { chain.request() } returns request
        every { chain.proceed(request) } returns response

        val result = interceptor.intercept(chain)

        assertThat(result).isEqualTo(response)
        verify(exactly = 1) { cacheStatsService.recordNetworkRequest() }
    }
}
