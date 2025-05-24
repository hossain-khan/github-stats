
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import java.util.Locale
import java.util.ResourceBundle
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
    private val bundle = ResourceBundle.getBundle("strings", Locale.getDefault())

    suspend fun generatePrStats(prNumber: Int) {
        val (repoOwner, repoId, _, botUserIds) = appConfig.get()
        Log.i(String.format(bundle.getString("pr_stats_building_stats_for_pr"), prNumber))
        val authorReportBuildTime =
            measureTimeMillis {
                val statsResult: PullRequestStatsRepo.StatsResult =
                    try {
                        pullRequestStatsRepo.stats(
                            repoOwner = repoOwner,
                            repoId = repoId,
                            prNumber = prNumber,
                            botUserIds = botUserIds,
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
                        Log.w(String.format(bundle.getString("pr_stats_failed_to_generate"), prNumber, statsResult.errorInfo.errorMessage))
                    }
                }
            }

        Log.d(String.format(bundle.getString("pr_stats_generation_time"), prNumber, authorReportBuildTime.milliseconds))

        Log.i(bundle.getString("pr_stats_separator"))
    }
}
