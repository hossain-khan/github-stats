package dev.hossain.githubstats.io

import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.service.GithubService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Client {
    private val httpClient = okHttpClient()

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
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        retrofit.create(GithubService::class.java)
    }
}
