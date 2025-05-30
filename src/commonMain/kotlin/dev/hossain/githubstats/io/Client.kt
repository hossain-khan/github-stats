package dev.hossain.githubstats.io

// All JVM-specific imports are removed from commonMain.
// import com.squareup.moshi.Moshi
// import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
// import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
// import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_FILE
// import dev.hossain.githubstats.BuildConfig
// import dev.hossain.githubstats.model.timeline.* // Specific ones were listed
// import dev.hossain.githubstats.service.GithubApiService // This is now an expect interface
// import dev.hossain.githubstats.util.FileUtil.httpCacheDir // This needs to be KMP compatible
// import dev.hossain.githubstats.util.PropertiesReader // Changed to expect class
// import okhttp3.Cache
// import okhttp3.HttpUrl
// import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
// import okhttp3.OkHttpClient
// import okhttp3.logging.HttpLoggingInterceptor
// import retrofit2.Retrofit
// import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Placeholder for GitHub API client in commonMain.
 * The actual HTTP client implementation (e.g. Ktor based) and
 * GithubApiService instantiation will be provided by platform-specific `actual` declarations
 * and dependency injection (Koin).
 */
object Client {
    // The githubApiService is now expected to be injected via Koin using the
    // `expect interface GithubApiService`.
    // Therefore, the direct static accessor `Client.githubApiService` is removed from commonMain.
    // If a static accessor is truly desired for some reason, it would need to be an `expect val`.
    // However, DI is preferred.

    // All JVM-specific client setup (OkHttp, Moshi, Retrofit) has been removed from commonMain.
    // This logic will reside in `jvmMain` (or other platform-specific source sets)
    // as part of the `actual` implementation for `GithubApiService` and its dependencies.
}
