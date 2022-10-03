import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrAuthorStats
import dev.hossain.githubstats.PullStats
import dev.hossain.githubstats.formatter.CsvFormatter
import dev.hossain.githubstats.formatter.FileWriterFormatter
import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client.githubService
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.util.LocalProperties
import kotlinx.coroutines.runBlocking

/**
 * Runs PR stats on specified repository for specific autor.
 * See **`local_sample.properties`** for more information on configuration available.
 * Also check out [BuildConfig] for available runtime config for debugging.
 */
fun main(args: Array<String>) {
    println("Program arguments: ${args.joinToString()}")

    val issueSearchPager = IssueSearchPager(githubService)
    val pullStats = PullStats(githubService)
    val authorStats = PrAuthorStats(issueSearchPager, pullStats)
    val formatters: List<StatsFormatter> = listOf(
        PicnicTableFormatter(),
        CsvFormatter(),
        FileWriterFormatter(PicnicTableFormatter())
    )
    val localProperties = LocalProperties()
    val repoOwner: String = localProperties.getRepoOwner()
    val repoId: String = localProperties.getRepoId()
    val prAuthorUserId = "naomi-lgbt" // "naomi-lgbt", "ieahleen"

    println("Getting PR stats for author '$prAuthorUserId' from '$repoId' repository.")

    runBlocking {
        val prAuthorStats = authorStats.authorStats(repoOwner, repoId, prAuthorUserId)

        formatters.forEach {
            println(it.formatAuthorStats(prAuthorStats))
        }
    }
}
