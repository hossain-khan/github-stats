import dev.hossain.ascii.Art
import dev.hossain.githubstats.AuthorReviewStats
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.PrAuthorStats
import dev.hossain.githubstats.PrReviewerStats
import dev.hossain.githubstats.di.appModule
import dev.hossain.githubstats.formatter.StatsFormatter
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.time.Zone
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import java.time.ZoneId
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.milliseconds

/**
 * Runs PR stats on specified repository for specific autor.
 * See **`local_sample.properties`** for more information on configuration available.
 * Also check out [BuildConfig] for available runtime config for debugging.
 */
fun main() {
    startKoin {
        modules(appModule)
    }

    val statsGeneratorApplication = StatsGeneratorApplication()

    println(Art.coffee())

    runBlocking {
        val authorsZoneId: ZoneId = requireNotNull(Zone.cities["Toronto"])

        statsGeneratorApplication.generateAuthorStats(authorsZoneId)

        statsGeneratorApplication.generateReviewerStats(authorsZoneId)
    }
}

class StatsGeneratorApplication : KoinComponent {
    private val prReviewerStatsService: PrReviewerStats by inject()
    private val prAuthorStatsService: PrAuthorStats by inject()
    private val appConfig: AppConfig by inject()
    private val formatters: List<StatsFormatter> = getKoin().getAll()

    suspend fun generateAuthorStats(authorsZoneId: ZoneId) {
        val (repoOwner, repoId, dateLimit, userIds) = appConfig.get()
        userIds.forEach { authorId ->
            println("■ Building stats for `$authorId` as PR author.\n")
            val authorReportBuildTime = measureTimeMillis {
                val prAuthorStats: List<AuthorReviewStats> = prAuthorStatsService.authorStats(
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
                println("\nⓘ Stats generation for `$authorId` took ${authorReportBuildTime.milliseconds}")
            }
            println("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }
    }

    suspend fun generateReviewerStats(authorsZoneId: ZoneId) {
        val (repoOwner, repoId, dateLimit, userIds) = appConfig.get()
        userIds.forEach { usedId ->
            val reviewerReportBuildTime = measureTimeMillis {
                println("■ Building stats for `$usedId` as PR reviewer.\n")
                val prReviewerReviewStats = prReviewerStatsService.reviewerStats(
                    owner = repoOwner,
                    repo = repoId,
                    reviewer = usedId,
                    zoneId = authorsZoneId,
                    dateLimit = dateLimit
                )
                formatters.forEach {
                    println(it.formatReviewerStats(prReviewerReviewStats))
                }
            }

            if (BuildConfig.DEBUG) {
                println("\nⓘ Stats generation for `$usedId` took ${reviewerReportBuildTime.milliseconds}")
            }
            println("\n─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─\n")
        }
    }
}
