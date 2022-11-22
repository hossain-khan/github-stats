import dev.hossain.ascii.Art
import dev.hossain.githubstats.di.appModule
import dev.hossain.githubstats.logging.Log
import org.koin.core.context.GlobalContext.startKoin

/**
 * Contains some code snippets to run the GitHub PR Stats application in alternative ways.
 * Uncomment the block of code to run them.
 */
fun main() {
    Log.i(Art.coffee())
    startKoin {
        modules(appModule) // Initializes dependency injection for the app
    }

    // =========================================================================
    // ℹ️ Example code block to test single PR stats (uncomment to test by PR#)
    // =========================================================================
//    runBlocking {
//        val prStatsApplication = PrStatsApplication()
//        prStatsApplication.generatePrStats(prNumber = 1) // Check single PR stats
//    }

    // =========================================================================
    // ℹ️ Example code block to test single GitHub API from GithubApiService.
    // =========================================================================
//    runBlocking {
//        // Sample parameter to get PR review comments from https://github.com/square/okhttp/pull/7415
//        val prReviewComments = dev.hossain.githubstats.io.Client.githubApiService
//            .prSourceCodeReviewComments(owner = "square", repo = "okhttp", prNumber = 7415)
//        println("The PR has ${prReviewComments.size} review comments")
//    }
}
