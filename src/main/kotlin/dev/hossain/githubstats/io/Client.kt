package dev.hossain.githubstats.io

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.service.GithubService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.Properties

/**
 * Github client with retrofit service.
 */
object Client {
    private val httpClient = okHttpClient()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private fun okHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            // Only add HTTP logs for debug builds
            builder.addInterceptor(logging)
        }

        builder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("User-Agent", "Kotlin-Cli")
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", getAccessToken())

            chain.proceed(requestBuilder.build())
        }

        return builder.build()
    }

    val githubService: GithubService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
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

            if (properties.containsKey("access_token").not()) {
                throw IllegalStateException("Please provide access token in `local.properties`.")
            }

            return properties.getProperty("access_token")
        } else {
            throw IllegalStateException("Please provide access token in `local.properties`.")
        }
    }
}
