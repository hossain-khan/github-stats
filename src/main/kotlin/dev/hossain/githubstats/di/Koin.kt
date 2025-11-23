package dev.hossain.githubstats.di

import StatsGeneratorApplication
import dev.hossain.githubstats.AppConstants
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.cache.CacheStatsCollector
import dev.hossain.githubstats.cache.CacheStatsFormatter
import dev.hossain.githubstats.cache.CacheStatsService
import dev.hossain.githubstats.client.ApiClientType
import dev.hossain.githubstats.client.GitHubApiClient
import dev.hossain.githubstats.client.GitHubApiClientFactory
import dev.hossain.githubstats.formatter.CsvFormatter
import dev.hossain.githubstats.formatter.FileWriterFormatter
import dev.hossain.githubstats.formatter.HtmlChartFormatter
import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepoImpl
import dev.hossain.githubstats.service.GithubApiService
import dev.hossain.githubstats.service.IssueSearchPagerService
import dev.hossain.githubstats.service.TimelineEventsPagerService
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.githubstats.util.LocalProperties
import dev.hossain.githubstats.util.PropertiesReader
import dev.hossain.i18n.Resources
import dev.hossain.i18n.ResourcesImpl
import dev.hossain.time.UserTimeZone
import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.Locale
import java.util.ResourceBundle

/**
 * Koin dependency injection module that configures all application dependencies.
 *
 * ## What is Koin?
 * Koin is a lightweight dependency injection framework for Kotlin. It allows us to:
 * - Define dependencies in one central location
 * - Automatically resolve and inject dependencies where needed
 * - Manage object lifecycles (singletons vs new instances)
 *
 * ## Dependency Scopes
 * This module uses two main scopes:
 *
 * ### `single { }`
 * Creates a **singleton** - only one instance exists for the entire application lifetime.
 * Used for stateful services, repositories, and shared resources.
 * Example: `single<CacheStatsService> { CacheStatsCollector() }`
 *
 * ### `factory { }`
 * Creates a **new instance** every time it's requested.
 * Used for stateless services or when each caller needs a fresh instance.
 * Example: `factory { IssueSearchPagerService(...) }`
 *
 * ## How It Works
 * Dependencies are resolved automatically using `get()` function:
 * ```kotlin
 * factory {
 *     PrReviewerStatsService(
 *         pullRequestStatsRepo = get(), // Koin automatically provides the PullRequestStatsRepo instance
 *         issueSearchPager = get(),
 *         ...
 *     )
 * }
 * ```
 *
 * ## Usage in Application
 * Initialize Koin in `Main.kt`:
 * ```kotlin
 * startKoin {
 *     modules(appModule)
 * }
 * ```
 *
 * Inject dependencies in classes:
 * ```kotlin
 * class MyService : KoinComponent {
 *     private val repository: PullRequestStatsRepo by inject()
 * }
 * ```
 *
 * @see <a href="https://insert-koin.io/docs/reference/koin-core/dsl">Koin DSL Documentation</a>
 */
val appModule =
    module {
        // ========================================================================================
        // Cache Statistics - Tracks HTTP cache hit/miss rates and performance metrics
        // ========================================================================================
        single<CacheStatsService> { CacheStatsCollector() }
        single { CacheStatsFormatter() }

        // ========================================================================================
        // GitHub API Client - Main interface for interacting with GitHub
        // ========================================================================================
        // Creates appropriate API client implementation (Retrofit or GitHub CLI) based on config
        single<GitHubApiClient> {
            val localProperties: LocalProperties = get()
            val clientType = ApiClientType.fromString(localProperties.getApiClientType())
            GitHubApiClientFactory.create(clientType, cacheStatsService = get())
        }

        // ========================================================================================
        // Core Services - Business logic for PR statistics and data processing
        // ========================================================================================
        // Repository for pull request statistics - handles data fetching and transformation
        single<PullRequestStatsRepo> {
            PullRequestStatsRepoImpl(
                apiClient = get(),
                timelinesPager = get(),
                userTimeZone = get(),
                cacheStatsService = get(),
            )
        }

        // Pagination service for GitHub issue search - handles paginated API responses
        factory {
            IssueSearchPagerService(
                apiClient = get(),
                errorProcessor = get(),
            )
        }

        // Pagination service for PR timeline events (reviews, comments, etc.)
        factory {
            TimelineEventsPagerService(
                apiClient = get(),
                errorProcessor = get(),
            )
        }

        // Service for generating statistics about PR reviewers (who reviewed what)
        factory {
            PrReviewerStatsService(
                pullRequestStatsRepo = get(),
                issueSearchPager = get(),
                appConfig = get(),
                errorProcessor = get(),
            )
        }

        // Service for generating statistics about PR authors (who created what)
        factory {
            PrAuthorStatsService(
                pullRequestStatsRepo = get(),
                issueSearchPager = get(),
                appConfig = get(),
                errorProcessor = get(),
            )
        }

        // Utility services for error handling and timezone management
        single { ErrorProcessor() }
        single { UserTimeZone() }

        // ========================================================================================
        // Application Coordinator - Orchestrates stats generation for all users
        // ========================================================================================
        single {
            StatsGeneratorApplication(
                prReviewerStatsService = get(),
                prAuthorStatsService = get(),
                resources = get(),
                appConfig = get(),
                formatters = getAll(), // Injects all registered StatsFormatter implementations
                cacheStatsService = get(),
                cacheStatsFormatter = get(),
            )
        }

        // ========================================================================================
        // Internationalization (i18n) - Localized strings and resources
        // ========================================================================================
        single { ResourceBundle.getBundle("strings", Locale.getDefault()) }
        factory { ResourcesImpl(resourceBundle = get()) } bind Resources::class

        // ========================================================================================
        // Configuration - Application settings and property management
        // ========================================================================================
        factory { AppConfig(localProperties = get()) }
        factory { LocalProperties() }
        single<PropertiesReader> { LocalProperties() }

        // ========================================================================================
        // Stats Formatters - Multiple output formats for generated statistics
        // ========================================================================================
        // Each formatter is bound to StatsFormatter interface so they can be injected as a list
        // using getAll() - see StatsGeneratorApplication above
        single { PicnicTableFormatter() } bind StatsFormatter::class // ASCII table output
        single { CsvFormatter() } bind StatsFormatter::class // CSV file output
        single { FileWriterFormatter(PicnicTableFormatter()) } bind StatsFormatter::class // Writes ASCII to file
        single { HtmlChartFormatter() } bind StatsFormatter::class // HTML with Google Charts

        // ========================================================================================
        // UI Components - Console progress indicators
        // ========================================================================================
        factory {
            ProgressBarBuilder()
                .setTaskName(AppConstants.PROGRESS_LABEL)
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setConsumer(ConsoleProgressBarConsumer(System.out))
        }
    }
