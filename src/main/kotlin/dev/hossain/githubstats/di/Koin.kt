package dev.hossain.githubstats.di

import dev.hossain.githubstats.AppConstants
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.formatter.CsvFormatter
import dev.hossain.githubstats.formatter.FileWriterFormatter
import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepoImpl
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.githubstats.util.LocalProperties
import dev.hossain.githubstats.util.PropertiesReader
import dev.hossain.time.UserTimeZone
import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBarBuilder
import me.tongfei.progressbar.ProgressBarStyle
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Application module setup for dependency injection using Koin.
 *
 * See https://insert-koin.io/docs/reference/koin-core/dsl for more info.
 */
val appModule = module {
    // Network and local services for stat generation
    single { Client.githubApiService }
    single<PullRequestStatsRepo> { PullRequestStatsRepoImpl(get(), get()) }
    factory { IssueSearchPager(get(), get()) }
    factory { PrReviewerStatsService(get(), get()) }
    factory { PrAuthorStatsService(get(), get()) }
    single { ErrorProcessor() }
    single { UserTimeZone() }

    // Config to load local properties
    factory { AppConfig(get()) }
    factory { LocalProperties() }
    single<PropertiesReader> { LocalProperties() }

    // Stats Formatters
    single { PicnicTableFormatter() } bind StatsFormatter::class
    single { CsvFormatter() } bind StatsFormatter::class
    single { FileWriterFormatter(PicnicTableFormatter()) } bind StatsFormatter::class

    // Progress Bar
    factory {
        ProgressBarBuilder()
            .setTaskName(AppConstants.PROGRESS_LABEL)
            .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
            .setConsumer(ConsoleProgressBarConsumer(System.out))
    }
}
