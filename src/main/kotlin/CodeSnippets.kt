import dev.hossain.ascii.Art
import dev.hossain.githubstats.di.appModule
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.service.GithubApiService
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.inject

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
    // ℹ️ Example code block to show top contributors from specified repository.
    // =========================================================================
    runBlocking {
        val githubApiService: GithubApiService by inject(GithubApiService::class.java)
        // Sample parameter to get top contributors from https://github.com/square/okhttp
        val contributors = githubApiService.topContributors(owner = "square", repo = "okhttp")

        println("Here are top contributors: ${contributors.map { it.login }}")
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
//        val githubApiService: GithubApiService by inject(GithubApiService::class.java)
//        // Sample parameter to get PR review comments from https://github.com/square/okhttp/pull/7415
//        val prReviewComments = githubApiService
//            .prSourceCodeReviewComments(owner = "square", repo = "okhttp", prNumber = 7415)
//        println("The PR has ${prReviewComments.size} review comments")
//    }

    // Show disclaimer about the review time being inaccurate
    Log.w(Art.warnAboutReviewTime())
}
