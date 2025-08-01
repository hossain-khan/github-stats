package dev.hossain.githubstats.di

import StatsGeneratorApplication
import dev.hossain.githubstats.AppConstants
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.cache.CacheStatsCollector
import dev.hossain.githubstats.cache.CacheStatsFormatter
import dev.hossain.githubstats.cache.CacheStatsService
import dev.hossain.githubstats.formatter.CsvFormatter
import dev.hossain.githubstats.formatter.FileWriterFormatter
import dev.hossain.githubstats.formatter.HtmlChartFormatter
import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepoImpl
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
 * Application module setup for dependency injection using Koin.
 *
 * See https://insert-koin.io/docs/reference/koin-core/dsl for more info.
 */
val appModule =
    module {
        // Cache statistics service for tracking cache performance
        single<CacheStatsService> { CacheStatsCollector() }
        single { CacheStatsFormatter() }

        // Network and local services for stat generation
        single { Client(cacheStatsService = get()) }
        single { get<Client>().githubApiService }
        single<PullRequestStatsRepo> {
            PullRequestStatsRepoImpl(
                githubApiService = get(),
                timelinesPager = get(),
                userTimeZone = get(),
                cacheStatsService = get(),
            )
        }
        factory {
            IssueSearchPagerService(
                githubApiService = get(),
                errorProcessor = get(),
            )
        }
        factory {
            TimelineEventsPagerService(
                githubApiService = get(),
                errorProcessor = get(),
            )
        }
        factory {
            PrReviewerStatsService(
                pullRequestStatsRepo = get(),
                issueSearchPager = get(),
                appConfig = get(),
                errorProcessor = get(),
            )
        }
        factory {
            PrAuthorStatsService(
                pullRequestStatsRepo = get(),
                issueSearchPager = get(),
                appConfig = get(),
                errorProcessor = get(),
            )
        }
        single { ErrorProcessor() }
        single { UserTimeZone() }

        single {
            StatsGeneratorApplication(
                prReviewerStatsService = get(),
                prAuthorStatsService = get(),
                resources = get(),
                appConfig = get(),
                formatters = getAll(),
                cacheStatsService = get(),
                cacheStatsFormatter = get(),
            )
        }

        // Localization
        single { ResourceBundle.getBundle("strings", Locale.getDefault()) }
        factory { ResourcesImpl(resourceBundle = get()) } bind Resources::class

        // Config to load local properties
        factory { AppConfig(localProperties = get()) }
        factory { LocalProperties() }
        single<PropertiesReader> { LocalProperties() }

        // Binds all the different stats formatters
        single { PicnicTableFormatter() } bind StatsFormatter::class
        single { CsvFormatter() } bind StatsFormatter::class
        single { FileWriterFormatter(PicnicTableFormatter()) } bind StatsFormatter::class
        single { HtmlChartFormatter() } bind StatsFormatter::class

        // Progress Bar
        factory {
            ProgressBarBuilder()
                .setTaskName(AppConstants.PROGRESS_LABEL)
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
                .setConsumer(ConsoleProgressBarConsumer(System.out))
        }
    }
