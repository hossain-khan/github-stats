package dev.hossain.githubstats.di

import dev.hossain.githubstats.ui.viewmodel.GitHubStatsViewModel
import org.koin.dsl.module

/**
 * Dependency injection module for the desktop application.
 */
val appModule = module {
    // Register the ViewModel
    single { GitHubStatsViewModel() }
}