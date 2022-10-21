import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.util.AppConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.ZoneId
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Checks single PR stats. Useful for validating single PR stats.
 */
class PrStatsApplication : KoinComponent {
    private val pullRequestStatsRepo: PullRequestStatsRepo by inject()
    private val appConfig: AppConfig by inject()
    private val formatters: List<StatsFormatter> = getKoin().getAll()

    suspend fun generatePrStats(prNumber: Int, authorsZoneId: ZoneId) {
        val (repoOwner, repoId, _, _) = appConfig.get()
        println("■ Building stats for PR#`$prNumber`.\n")
        val authorReportBuildTime = measureTimeMillis {
            val statsResult: PullRequestStatsRepo.StatsResult = try {
                pullRequestStatsRepo.stats(
                    repoOwner = repoOwner,
                    repoId = repoId,
                    prNumber = prNumber,
                    zoneId = authorsZoneId
                )
            } catch (e: Exception) {
                println("Error getting PR#$prNumber. Got: ${e.message}")
                PullRequestStatsRepo.StatsResult.Failure(e)
            }

            if (statsResult is PullRequestStatsRepo.StatsResult.Success) {
                formatters.forEach {
                    println(it.formatPrStats(statsResult.stats))
                }
            } else {
                println("⚠️ Failed to generate PR stats for PR#`$prNumber`")
            }
        }
        if (BuildConfig.DEBUG) {
            println("\nⓘ Stats generation for PR#`$prNumber` took ${authorReportBuildTime.milliseconds}")
        }
        println("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
    }
}
