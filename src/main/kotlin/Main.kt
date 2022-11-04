import dev.hossain.ascii.Art
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.di.appModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin

/**
 * Runs PR stats on specified repository for each GitHub users defined in the config.
 * See **`[LOCAL_PROPERTIES_SAMPLE_FILE]`** for more information on configuration available.
 * Also check out [BuildConfig] for available runtime config for debugging.
 */
fun main() {
    startKoin {
        modules(appModule) // Initializes dependency injection for the app
    }

    println(Art.coffee())

    runBlocking {
        val statsGeneratorApplication = StatsGeneratorApplication()

        // 💡 Generates stats for each user as PR author - for all PRs created by the user
        statsGeneratorApplication.generateAuthorStats()

        // 💡 Generates stats for each user as PR reviewer - for all PRs reviewed by the user
        statsGeneratorApplication.generateReviewerStats()
    }

    // ℹ️ Example code block to test single PR stats (uncomment to test by PR#)
//    runBlocking {
//        val prStatsApplication = PrStatsApplication()
//        prStatsApplication.generatePrStats(prNumber = 1) // Check single PR stats
//    }

    // ℹ️ Example code block to test single GitHub API from GithubApiService.
//    runBlocking {
//        // Sample parameter to get PR review comments from https://github.com/square/okhttp/pull/7415
//        val prReviewComments = dev.hossain.githubstats.io.Client.githubApiService
//            .prReviewComments(owner = "square", repo = "okhttp", prNumber = 7415)
//        println("The PR has ${prReviewComments.size} review comments")
//    }
}
