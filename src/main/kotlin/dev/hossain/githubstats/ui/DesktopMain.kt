package dev.hossain.githubstats.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.hossain.githubstats.ui.components.GithubStatsApp
import dev.hossain.githubstats.ui.theme.AppTheme
import dev.hossain.githubstats.di.appModule
import org.koin.core.context.startKoin

/**
 * Main entry point for the GitHub Stats Desktop application.
 */
fun main() = application {
    // Start Koin DI
    startKoin {
        modules(appModule)
    }
    
    val windowState = rememberWindowState()
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "GitHub PR Stats",
        state = windowState
    ) {
        AppTheme {
            GithubStatsApp()
        }
    }
}