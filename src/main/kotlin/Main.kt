import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrAuthorStats
import dev.hossain.githubstats.PrReviewerStats
import dev.hossain.githubstats.PullStats
import dev.hossain.githubstats.formatter.CsvFormatter
import dev.hossain.githubstats.formatter.FileWriterFormatter
import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.io.Client.githubService
import dev.hossain.githubstats.service.IssueSearchPager
import dev.hossain.githubstats.util.LocalProperties
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Runs PR stats on specified repository for specific autor.
 * See **`local_sample.properties`** for more information on configuration available.
 * Also check out [BuildConfig] for available runtime config for debugging.
 */
fun main() {
    /**
     * Convenient map to get [ZoneId] for some known locations.
     * REF: https://mkyong.com/java8/java-display-all-zoneid-and-its-utc-offset/
     */
    val zoneIds = mapOf(
        "Atlanta" to ZoneId.of("America/New_York"),
        "New York" to ZoneId.of("America/New_York"),
        "San Francisco" to ZoneId.of("America/Los_Angeles"),
        "Toronto" to ZoneId.of("America/Toronto"),
        "Vancouver" to ZoneId.of("America/Vancouver")
    )

    val authorsZoneId: ZoneId = requireNotNull(zoneIds["Toronto"])

    val localProperties = LocalProperties()
    val repoOwner: String = localProperties.getRepoOwner()
    val repoId: String = localProperties.getRepoId()
    val dateLimit: String = localProperties.getDateLimit()
    val prAuthorUserIds = localProperties.getAuthors().split(",")
        .filter { it.isNotEmpty() }
        .map { it.trim() }

    val formatters: List<StatsFormatter> = listOf(
        PicnicTableFormatter(authorsZoneId, dateLimit),
        CsvFormatter(dateLimit),
        FileWriterFormatter(PicnicTableFormatter(authorsZoneId, dateLimit))
    )

    println(Art.coffee())
    println("Getting PR stats for $prAuthorUserIds authors from '$repoId' repository for time zone $authorsZoneId since $dateLimit.")

    runBlocking {
        prAuthorUserIds.forEach { authorId ->
            println("■ Building stats for `$authorId`.")
            val reportBuildTime = measureTimeMillis {
                val issueSearchPager = IssueSearchPager(githubService)
                val pullStats = PullStats(githubService)
                val authorStats = PrAuthorStats(issueSearchPager, pullStats)
                val prAuthorStats: List<AuthorReviewStats> = authorStats.authorStats(
                    owner = repoOwner,
                    repo = repoId,
                    author = authorId,
                    zoneId = authorsZoneId,
                    dateLimit = dateLimit
                )

                formatters.forEach {
                    println(it.formatAuthorStats(prAuthorStats))
                }
            }
            if (BuildConfig.DEBUG) {
                println("\nⓘ Stats generation for `$authorId` took ${reportBuildTime.milliseconds}")
            }
            println("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }

        prAuthorUserIds.forEach { usedId ->
            val issueSearchPager = IssueSearchPager(githubService)
            val pullStats = PullStats(githubService)
            val reviewerStats = PrReviewerStats(issueSearchPager, pullStats)
            val prReviewerReviewStats = reviewerStats.reviewerStats(
                owner = repoOwner,
                repo = repoId,
                reviewer = usedId,
                zoneId = authorsZoneId,
                dateLimit = dateLimit
            )
            formatters.forEach {
                println(it.formatReviewerStats(prReviewerReviewStats))
            }

            println("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }
    }
}
