
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.ErrorProcessor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Checks single PR stats. Useful for validating single PR stats.
 */
class PrStatsApplication : KoinComponent {
    private val pullRequestStatsRepo: PullRequestStatsRepo by inject()
    private val appConfig: AppConfig by inject()
    private val formatters: List<StatsFormatter> = getKoin().getAll()
    private val errorProcessor: ErrorProcessor by inject()

    suspend fun generatePrStats(prNumber: Int) {
        val (repoOwner, repoId, _, _) = appConfig.get()
        Log.i("■ Building stats for PR#`$prNumber`.\n")
        val authorReportBuildTime =
            measureTimeMillis {
                val statsResult: PullRequestStatsRepo.StatsResult =
                    try {
                        pullRequestStatsRepo.stats(
                            repoOwner = repoOwner,
                            repoId = repoId,
                            prNumber = prNumber,
                        )
                    } catch (e: Exception) {
                        val error = errorProcessor.getDetailedError(e)
                        PullRequestStatsRepo.StatsResult.Failure(error)
                    }

                when (statsResult) {
                    is PullRequestStatsRepo.StatsResult.Success -> {
                        formatters.forEach {
                            println(it.formatSinglePrStats(statsResult.stats))
                        }
                    }
                    is PullRequestStatsRepo.StatsResult.Failure -> {
                        Log.w("⚠️ Failed to generate PR stats for PR#`$prNumber`. Error Message: ${statsResult.error.message}")
                    }
                }
            }

        Log.d("\nⓘ Stats generation for PR#`$prNumber` took ${authorReportBuildTime.milliseconds}")

        Log.i("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
    }
}
