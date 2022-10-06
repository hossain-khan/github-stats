package dev.hossain.githubstats.di

import dev.hossain.githubstats.PrAuthorStats
import dev.hossain.githubstats.PrReviewerStats
import dev.hossain.githubstats.formatter.CsvFormatter
import dev.hossain.githubstats.formatter.FileWriterFormatter
import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepoImpl
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.LocalProperties
import dev.hossain.githubstats.util.PropertiesReader
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Application module setup for dependency injection using Koin.
 *
 * See https://insert-koin.io/docs/reference/koin-core/dsl for more info.
 */
val appModule = module {
    single { Client.githubService }
    single<PullRequestStatsRepo> { PullRequestStatsRepoImpl(get()) }
    single<PropertiesReader> { LocalProperties() }
    factory { LocalProperties() }
    factory { IssueSearchPager(get()) }
    factory { PrReviewerStats(get(), get()) }
    factory { PrAuthorStats(get(), get()) }
    factory { AppConfig(get()) }

    // Stats Formatters
    single { PicnicTableFormatter() } bind StatsFormatter::class
    single { CsvFormatter() } bind StatsFormatter::class
    single { FileWriterFormatter(PicnicTableFormatter()) } bind StatsFormatter::class
}
