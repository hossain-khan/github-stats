import dev.hossain.ascii.Art
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.di.appModule
import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin

/**
 * Runs PR stats on specified repository for each GitHub users defined in the config.
 * See **`[LOCAL_PROPERTIES_SAMPLE_FILE]`** for more information on configuration available.
 * Also check out [BuildConfig] for available runtime config for debugging.
 *
 * See `CodeSnippets` file for [PrStatsApplication] usage and other snippets.
 */
fun main() {
    Log.i(Art.coffee())
    startKoin {
        modules(appModule) // Initializes dependency injection for the app
    }

    runBlocking {
        val statsGeneratorApplication = StatsGeneratorApplication()

        // 💡 Generates stats for each user as PR author - for all PRs created by the user
        statsGeneratorApplication.generateAuthorStats()

        // 💡 Generates stats for each user as PR reviewer - for all PRs reviewed by the user
        statsGeneratorApplication.generateReviewerStats()
    }
}
