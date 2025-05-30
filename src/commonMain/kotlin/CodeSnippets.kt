import dev.hossain.ascii.Art
import dev.hossain.githubstats.PrStatsApplication // Assuming PrStatsApplication is a KoinComponent
import dev.hossain.githubstats.io.Client // Client will be refactored (expect/actual for GithubApiService)
import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Contains some code snippets to run the GitHub PR Stats application in alternative ways.
 * These snippets assume Koin has been initialized by the main application.
 * To run these, you would typically call them from a context where Koin is set up.
 */
object CodeSnippets : KoinComponent {

    // Injected PrStatsApplication - assuming it's registered in appModule
    // If PrStatsApplication is not a KoinComponent itself but created directly,
    // then this injection won't work and it needs to be provided by Koin modules.
    // For now, let's assume PrStatsApplication can be injected or created if it uses KoinComponent.
    // If PrStatsApplication itself injects dependencies, it MUST be a KoinComponent or injected.
    private val prStatsApplication: PrStatsApplication by inject()


    fun showTopContributors() = runBlocking {
        Log.i(Art.coffee())
        // NOTE: Client.githubApiService will be an `expect`ed interface.
        // The actual implementation (e.g. Ktor-based) will be injected by Koin.
        // For now, this direct call might still show errors until Client is refactored.
        Log.i("Running snippet: Show Top Contributors")
        val contributors = Client.githubApiService.topContributors(owner = "square", repo = "okhttp")
        println("Top contributors for square/okhttp: ${contributors.map { it.login }}")
        Log.w(Art.warnAboutReviewTime())
    }

    fun testSinglePrStats(prNumber: Int) = runBlocking {
        Log.i(Art.coffee())
        Log.i("Running snippet: Test Single PR Stats for PR #$prNumber")
        // Assuming PrStatsApplication is obtained from Koin
        prStatsApplication.generatePrStats(prNumber = prNumber)
        Log.w(Art.warnAboutReviewTime())
    }

    fun testSingleGitHubApi(owner: String, repo: String, prNumber: Int) = runBlocking {
        Log.i(Art.coffee())
        Log.i("Running snippet: Test Single GitHub API for $owner/$repo#$prNumber")
        // NOTE: Client.githubApiService will be an `expect`ed interface.
        val prReviewComments = Client.githubApiService
            .prSourceCodeReviewComments(owner = owner, repo = repo, prNumber = prNumber)
        println("The PR $owner/$repo#$prNumber has ${prReviewComments.size} review comments")
        Log.w(Art.warnAboutReviewTime())
    }
}

// Example of how you might call these snippets from your main execution context
// after Koin is initialized in Main.kt:
//
// fun main() {
//     Main.run() // This initializes Koin
//
//     // Then you can run snippets
//     CodeSnippets.showTopContributors()
//     CodeSnippets.testSinglePrStats(123)
// }
