package dev.hossain.githubstats.io

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.model.timeline.ClosedEvent
import dev.hossain.githubstats.model.timeline.MergedEvent
import dev.hossain.githubstats.model.timeline.ReadyForReviewEvent
import dev.hossain.githubstats.model.timeline.ReviewRequestedEvent
import dev.hossain.githubstats.model.timeline.ReviewedEvent
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.model.timeline.UnknownEvent
import dev.hossain.githubstats.service.GithubService
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.Properties

/**
 * GitHub client with Retrofit service.
 */
object Client {
    private val httpClient = okHttpClient()

    // Test backdoor to set base URL using mock server
    internal var baseUrl: HttpUrl = "https://api.github.com/".toHttpUrlOrNull()!!

    private val moshi = Moshi.Builder()
        .add(
            // https://github.com/square/moshi/blob/master/moshi-adapters/src/main/java/com/squareup/moshi/adapters/PolymorphicJsonAdapterFactory.kt
            PolymorphicJsonAdapterFactory.of(TimelineEvent::class.java, "event")
                .withSubtype(ClosedEvent::class.java, ClosedEvent.TYPE)
                .withSubtype(MergedEvent::class.java, MergedEvent.TYPE)
                .withSubtype(ReadyForReviewEvent::class.java, ReadyForReviewEvent.TYPE)
                .withSubtype(ReviewRequestedEvent::class.java, ReviewRequestedEvent.TYPE)
                .withSubtype(ReviewedEvent::class.java, ReviewedEvent.TYPE)
                .withDefaultValue(UnknownEvent())
        )
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private fun okHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor {
            // Use JVM console logger using error stream.
            System.err.println(it)
        }
        logging.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            // Only add HTTP logs for debug builds
            builder.addInterceptor(logging)
        }

        // Sets up global header for the GitHub API requests
        builder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("User-Agent", "Kotlin-Cli")
                .header("Accept", "application/vnd.github.v3+json")
                // https://docs.github.com/en/rest/overview/other-authentication-methods
                .header("Authorization", "Bearer ${getAccessToken()}")

            chain.proceed(requestBuilder.build())
        }

        return builder.build()
    }

    val githubService: GithubService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(GithubService::class.java)
    }

    /**
     * Provides access token from `local.properties` config file.
     */
    private fun getAccessToken(): String {
        val propertiesFile = File("local.properties")
        if (propertiesFile.exists()) {
            val properties = Properties()
            properties.load(propertiesFile.inputStream())

            return properties.getProperty("access_token", "MISSING-TOKEN")
        } else {
            if (System.getenv("IS_GITHUB_CI") == "true") {
                return "CI-JOB-ON-GITHUB-ACTION"
            }
            throw IllegalStateException("Please provide access token in `local.properties`.")
        }
    }
}
