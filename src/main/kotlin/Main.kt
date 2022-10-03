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
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Runs PR stats on specified repository for specific autor.
 * See **`local_sample.properties`** for more information on configuration available.
 * Also check out [BuildConfig] for available runtime config for debugging.
 */
fun main() {
    val formatters: List<StatsFormatter> = listOf(
        PicnicTableFormatter(),
        CsvFormatter(),
        FileWriterFormatter(PicnicTableFormatter())
    )
    val localProperties = LocalProperties()
    val repoOwner: String = localProperties.getRepoOwner()
    val repoId: String = localProperties.getRepoId()
    val prAuthorUserIds = localProperties.getAuthors().split(",").map { it.trim() }

    println("Getting PR stats for $prAuthorUserIds authors from '$repoId' repository.")

    runBlocking {
        prAuthorUserIds.forEach { authorId ->
            println("■ Building stats for `$authorId`.")
            val reportBuildTime = measureTimeMillis {
                val issueSearchPager = IssueSearchPager(githubService)
                val pullStats = PullStats(githubService)
                val authorStats = PrAuthorStats(issueSearchPager, pullStats)
                val prAuthorStats = authorStats.authorStats(repoOwner, repoId, authorId)

                formatters.forEach {
                    println(it.formatAuthorStats(prAuthorStats))
                }
            }
            if (BuildConfig.DEBUG) {
                println("\nⓘ Stats generation for `$authorId` took ${reportBuildTime.milliseconds}")
            }
            println("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }
    }
}
