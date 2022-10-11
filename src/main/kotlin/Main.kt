import dev.hossain.ascii.Art
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.di.appModule
import dev.hossain.time.Zone
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext.startKoin
import java.time.ZoneId

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

    // Test single PR stats (disabled by default)
    /*runBlocking {
        val prStatsApplication = PrStatsApplication()
        val authorsZoneId: ZoneId = requireNotNull(Zone.cities["Toronto"])
        prStatsApplication.generatePrStats(34030, authorsZoneId)
    }*/
}
