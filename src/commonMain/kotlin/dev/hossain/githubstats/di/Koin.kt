package dev.hossain.githubstats.di

import StatsGeneratorApplication
import dev.hossain.githubstats.AppConstants
import dev.hossain.githubstats.PrAuthorStatsService
import dev.hossain.githubstats.PrReviewerStatsService
import dev.hossain.githubstats.formatter.StatsFormatter // Assuming StatsFormatter is a common interface
// Import expect classes for formatters if they are to be bound here, or use common interfaces
// For now, commenting out JVM specific formatters and will bind common interfaces if defined.
// import dev.hossain.githubstats.formatter.CsvFormatter
// import dev.hossain.githubstats.formatter.FileWriterFormatter
// import dev.hossain.githubstats.formatter.HtmlChartFormatter
// import dev.hossain.githubstats.formatter.PicnicTableFormatter
import dev.hossain.githubstats.io.Client
import dev.hossain.githubstats.repository.PullRequestStatsRepo
import dev.hossain.githubstats.repository.PullRequestStatsRepoImpl
import dev.hossain.githubstats.service.IssueSearchPagerService
import dev.hossain.githubstats.service.TimelineEventsPagerService
import dev.hossain.githubstats.util.AppConfig
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.githubstats.util.PropertiesReader // Changed to use the one from util package
import dev.hossain.i18n.Resources
import dev.hossain.i18n.ResourcesImpl
import dev.hossain.platform.ExpectedResourceBundle
import dev.hossain.platform.PlatformProgressBar
// import dev.hossain.platform.PlatformTableFormatter
// import dev.hossain.platform.PlatformCsvWriter
// import dev.hossain.platform.getPropertiesReader // Factory from platform package is no longer needed
import dev.hossain.platform.getResourceBundle
import dev.hossain.time.UserTimeZone
import org.koin.dsl.bind
import org.koin.dsl.module
// Remove JVM specific imports like java.util.Locale and java.util.ResourceBundle if not used by expect/actual

/**
 * Application module setup for dependency injection using Koin.
 */
val appModule =
    module {
        // Network and local services for stat generation
        single { Client.githubApiService } // Assuming Client.githubApiService is KMP compatible (uses Ktor)
        single<PullRequestStatsRepo> {
            PullRequestStatsRepoImpl(
                githubApiService = get(),
                timelinesPager = get(),
                userTimeZone = get(), // UserTimeZone might need expect/actual if it uses java.time specifics not in kotlinx-datetime
            )
        }
        factory {
            IssueSearchPagerService(
                githubApiService = get(),
                errorProcessor = get(),
            )
        }
        factory {
            TimelineEventsPagerService(
                githubApiService = get(),
                errorProcessor = get(),
            )
        }
        factory {
            PrReviewerStatsService(
                pullRequestStatsRepo = get(),
                issueSearchPager = get(),
                appConfig = get(),
                errorProcessor = get(),
            )
        }
        factory {
            PrAuthorStatsService(
                pullRequestStatsRepo = get(),
                issueSearchPager = get(),
                appConfig = get(),
                errorProcessor = get(),
            )
        }
        single { ErrorProcessor() } // Assuming ErrorProcessor is common
        single { UserTimeZone() } // Assuming UserTimeZone is now KMP compatible or uses expect/actual

        single {
            StatsGeneratorApplication(
                prReviewerStatsService = get(),
                prAuthorStatsService = get(),
                resources = get(),
                appConfig = get(),
                formatters = getAll(), // getAll<StatsFormatter>()
            )
        }

        // Localization
        // TODO: Need a way to get current locale tag in common or pass from platform
        val defaultLocaleTag = "en" // Fallback, platform should provide actual
        single<ExpectedResourceBundle> { getResourceBundle(baseBundleName = "strings", localeTag = defaultLocaleTag) }
        factory { ResourcesImpl(resourceBundle = get()) } bind Resources::class // ResourcesImpl needs to use ExpectedResourceBundle

        // Config to load local properties
        // AppConfig now expects dev.hossain.githubstats.util.PropertiesReader
        factory { AppConfig(propertiesReader = get()) }
        // Provide PropertiesReader (expect class from util package)
        // The actual constructor of PropertiesReader will be called on each platform.
        single { PropertiesReader(AppConstants.LOCAL_PROPERTIES_FILE) }


        // Binds all the different stats formatters
        // TODO: Replace these with expect/actual bindings or common implementations
        // single<StatsFormatter> { PicnicTableFormatter(getTableFormatter()) } // Example if PicnicTableFormatter adapts an expect
        // single<StatsFormatter> { CsvFormatter(getCsvWriter()) } // Example if CsvFormatter adapts an expect
        // For now, comment out JVM specific formatters. Actual formatters need to be KMP compatible.
        // This means PicnicTableFormatter, CsvFormatter, FileWriterFormatter, HtmlChartFormatter
        // need to be either re-written using common code, or use expect/actual pattern for their core logic.
        // If StatsFormatter is a common interface, their actual implementations will be provided by platform modules (jvm, android, etc.)
        // or commonized. For this step, we focus on making `commonMain` buildable.
        // If these formatters are essential for `StatsGeneratorApplication` to resolve,
        // we might need to provide dummy common implementations or expect/actual for them.
        // For now, Koin will fail if `getAll<StatsFormatter>()` is called and no bindings are present.
        // Let's assume for now that these will be provided by platform-specific modules or commonized later.
        // To make it build, we can provide empty list or dummy common formatters.
        // Or, if StatsFormatter is an expect class itself, then platform will provide.
        // For now, no StatsFormatter bindings will be made here, assuming they come from elsewhere or are not strictly needed for commonMain build.


        // Progress Bar
        factory { PlatformProgressBar.builder().setTaskName(AppConstants.PROGRESS_LABEL) }
        // The original ProgressBarBuilder also had .setStyle() and .setConsumer().
        // These would need to be added to PlatformProgressBarBuilder expect interface
        // and their actual implementations. For now, only setTaskName is available in the expect.
    }
