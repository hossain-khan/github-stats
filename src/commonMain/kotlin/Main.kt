import dev.hossain.ascii.Art
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.di.appModule
import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin


/**
 * Runs PR stats on specified repository for each GitHub users defined in the config.
 * See **`[LOCAL_PROPERTIES_SAMPLE_FILE]`** for more information on configuration available.
 * Also check out [BuildConfig] for available runtime config for debugging.
 *
 * See `CodeSnippets` file for [PrStatsApplication] usage and other snippets.
 */
// Making main a KoinComponent to use inject directly
object Main : KoinComponent {
    private val statsGeneratorApplication: StatsGeneratorApplication by inject()

    fun run() {
        Log.i(Art.coffee())
        startKoin {
            modules(appModule) // Initializes dependency injection for the app
        }

        // Now that Main is a KoinComponent, injection works like this:
        // val statsGeneratorApplication: StatsGeneratorApplication by inject()
        // However, statsGeneratorApplication is now a member.

        runBlocking {
            // 💡 Generates stats for each user as PR author - for all PRs created by the user
            statsGeneratorApplication.generateAuthorStats()

            // 💡 Generates stats for each user as PR reviewer - for all PRs reviewed by the user
            statsGeneratorApplication.generateReviewerStats()
        }

        // Show disclaimer about the review time being inaccurate
        Log.w(Art.warnAboutReviewTime())
    }
}

// The actual main entry point for the application.
// For KMP, this main function might be platform-specific (e.g. in desktopMain, or not used for Android/iOS).
// For now, assuming this `main()` is for desktop execution context.
// It calls the run method of the KoinComponent object.
fun main() {
    Main.run()
}
