package dev.hossain.githubstats.io

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.cache.CacheStatsCollector
import dev.hossain.githubstats.cache.CacheStatsService
import dev.hossain.githubstats.cache.DatabaseCacheInterceptor
import dev.hossain.githubstats.cache.DatabaseCacheService
import dev.hossain.githubstats.cache.DatabaseManager
import dev.hossain.githubstats.cache.OkHttpCacheStatsInterceptor
import dev.hossain.githubstats.model.timeline.ClosedEvent
import dev.hossain.githubstats.model.timeline.CommentedEvent
import dev.hossain.githubstats.model.timeline.MergedEvent
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.model.timeline.UnknownEvent
import dev.hossain.githubstats.service.GithubApiService
import dev.hossain.githubstats.util.FileUtil.httpCacheDir
import dev.hossain.githubstats.util.LocalProperties
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * GitHub API client with Retrofit service to make API requests.
 * Supports both OkHttp caching and optional database-based response caching.
 */
class Client(
    private val cacheStatsService: CacheStatsService?,
) {
    private val localProperties = LocalProperties()
    private val httpClient = okHttpClient()

    // Test backdoor to allow setting base URL using mock server
    // By default, it's set to GitHub API base URL.
    internal var baseUrl: HttpUrl = "https://api.github.com/".toHttpUrlOrNull()!!

    companion object {
        // Backward compatibility for tests - provides static access
        private var testInstance: Client? = null

        /**
         * For test backward compatibility - allows setting base URL for mock server.
         */
        var baseUrl: HttpUrl
            get() = testInstance?.baseUrl ?: "https://api.github.com/".toHttpUrlOrNull()!!
            set(value) {
                if (testInstance == null) {
                    testInstance = Client(null)
                }
                testInstance?.baseUrl = value
            }

        /**
         * For test backward compatibility - provides access to GitHub API service.
         */
        val githubApiService: GithubApiService
            get() {
                if (testInstance == null) {
                    testInstance = Client(null)
                }
                return testInstance!!.githubApiService
            }
    }

    // JSON serialization using Moshi
    private val moshi =
        Moshi
            .Builder()
            .add(
                // https://github.com/square/moshi/blob/master/moshi-adapters/src/main/java/com/squareup/moshi/adapters/PolymorphicJsonAdapterFactory.kt
                PolymorphicJsonAdapterFactory
                    .of(TimelineEvent::class.java, "event")
                    .withSubtype(ClosedEvent::class.java, ClosedEvent.TYPE)
                    .withSubtype(CommentedEvent::class.java, CommentedEvent.TYPE)
                    .withSubtype(MergedEvent::class.java, MergedEvent.TYPE)
                    .withSubtype(ReadyForReviewEvent::class.java, ReadyForReviewEvent.TYPE)
                    .withSubtype(ReviewRequestedEvent::class.java, ReviewRequestedEvent.TYPE)
                    .withSubtype(ReviewedEvent::class.java, ReviewedEvent.TYPE)
                    .withDefaultValue(UnknownEvent()),
            ).addLast(KotlinJsonAdapterFactory())
            .build()

    /**
     * Builds OkHttp client with caching and debugging based on configuration.
     * Integrates both HTTP file caching and optional database-based JSON response caching.
     */
    private fun okHttpClient(): OkHttpClient {
        val logging =
            HttpLoggingInterceptor {
                // Use JVM console logger using error stream.
                System.err.println(it)
            }
        logging.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG_HTTP_REQUESTS) {
            // Only add HTTP logs for debug builds
            builder.addInterceptor(logging)
        }

        // Add database cache interceptor if database caching is enabled
        // This must come BEFORE OkHttpCacheStatsInterceptor to avoid double-counting
        setupDatabaseCaching(builder)

        // Add cache statistics interceptor if provided
        // This tracks OkHttp cache hits/misses for requests that weren't served by database cache
        cacheStatsService?.let { statsService ->
            builder.addInterceptor(OkHttpCacheStatsInterceptor(statsService))
        }

        // Sets up global header for the GitHub API requests
        builder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder =
                originalRequest
                    .newBuilder()
                    .header("User-Agent", "Kotlin-Cli")
                    .header("Accept", "application/vnd.github.v3+json")
                    // https://docs.github.com/en/rest/overview/other-authentication-methods
                    .header("Authorization", "Bearer ${getAccessToken()}")

            chain.proceed(requestBuilder.build())
        }

        // https://square.github.io/okhttp/features/caching/
        builder.cache(
            Cache(
                directory = httpCacheDir(),
                maxSize = BuildConfig.HTTP_CACHE_SIZE,
            ),
        )

        return builder.build()
    }

    /**
     * Sets up database-based response caching if configured in local properties.
     * This provides persistent caching of JSON responses alongside OkHttp caching.
     */
    private fun setupDatabaseCaching(builder: OkHttpClient.Builder) {
        try {
            if (localProperties.isDatabaseCacheEnabled()) {
                val database = DatabaseManager.initializeDatabase(localProperties)
                if (database != null) {
                    val cacheService =
                        DatabaseCacheService(
                            database = database,
                            expirationHours = localProperties.getDbCacheExpirationHours(),
                        )
                    val cacheInterceptor =
                        DatabaseCacheInterceptor(
                            cacheService = cacheService,
                            cacheStatsService = cacheStatsService ?: CacheStatsCollector(), // Fallback if not injected
                        )
                    builder.addInterceptor(cacheInterceptor)
                }
            }
        } catch (e: Exception) {
            // Log warning but don't fail the application if database caching setup fails
            System.err.println("Warning: Failed to setup database caching, continuing with HTTP caching only: ${e.message}")
        }
    }

    val githubApiService: GithubApiService by lazy {
        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        retrofit.create(GithubApiService::class.java)
    }

    /**
     * Provides access token from `[LOCAL_PROPERTIES_FILE]` config file.
     */
    private fun getAccessToken(): String =
        requireNotNull(localProperties.getProperty("access_token")) {
            "GitHub access token config is required in $LOCAL_PROPERTIES_FILE"
        }
}
