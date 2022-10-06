package dev.hossain.githubstats.di

import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepoImpl
import dev.hossain.githubstats.service.GithubService
import dev.hossain.githubstats.util.LocalProperties
import dev.hossain.githubstats.util.PropertiesReader
import org.koin.dsl.module

/**
 * Application module setup for dependency injection.
 */
val appModule = module {
    single<GithubService> { Client.githubService }
    single<PullRequestStatsRepo> { PullRequestStatsRepoImpl(get()) }
    single<PropertiesReader> { LocalProperties() }
}
