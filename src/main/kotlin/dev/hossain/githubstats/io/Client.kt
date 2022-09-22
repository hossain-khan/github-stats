package dev.hossain.githubstats.io

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.service.GithubService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

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
        return builder.build()
    }

    val githubService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(GithubService::class.java)
    }
}
