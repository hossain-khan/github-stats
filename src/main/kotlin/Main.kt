import dev.hossain.ascii.Art
import dev.hossain.githubstats.AppConstants.LOCAL_PROPERTIES_SAMPLE_FILE
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.di.appModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin

/**
 * Runs PR stats on specified repository for specific autor.
 * See **`[LOCAL_PROPERTIES_SAMPLE_FILE]`** for more information on configuration available.
 * Also check out [BuildConfig] for available runtime config for debugging.
 */
fun main() {
    startKoin {
        modules(appModule)
    }

    val statsGeneratorApplication = StatsGeneratorApplication()

    println(Art.coffee())

    runBlocking {
        // Generates stats for user as PR author - for all PRs created by the user
        statsGeneratorApplication.generateAuthorStats()

        // Generates stats for user as PR reviewer - for all PRs reviewed by the user
        statsGeneratorApplication.generateReviewerStats()
    }

    // Test single PR stats (disabled by default - uncomment to test by PR#)
    /*runBlocking {
        val prStatsApplication = PrStatsApplication()
        val authorsZoneId: ZoneId = requireNotNull(Zone.cities["Toronto"])
        prStatsApplication.generatePrStats(34030, authorsZoneId)
    }*/
}
