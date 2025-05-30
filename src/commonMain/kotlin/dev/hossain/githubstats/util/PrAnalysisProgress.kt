package dev.hossain.githubstats.util

import dev.hossain.githubstats.BuildConfig.PROGRESS_UPDATE_SPAN
import dev.hossain.githubstats.model.Issue
import dev.hossain.platform.PlatformProgressBar
import dev.hossain.platform.PlatformProgressBarBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Provides progress bar updates for [prs] that are being analyzed using [PlatformProgressBar].
 */
class PrAnalysisProgress(
    private val prs: List<Issue>,
) : KoinComponent {
    private lateinit var progressBar: PlatformProgressBar

    /**
     * Provides a progress bar for provided [prs].
     */
    fun start() {
        // Koin provides PlatformProgressBarBuilder instance now
        val progressBarBuilder: PlatformProgressBarBuilder = get()
        progressBar =
            progressBarBuilder
                .setInitialMax(prs.size.toLong())
                .build()
    }

    /**
     * Provides progress update after each [PROGRESS_UPDATE_SPAN] number of PRs.
     * @param index Current index of PR number that is being analyzed.
     */
    fun publish(index: Int) {
        // The original code had a println("\n") here which might be for console formatting.
        // This might need to be handled differently depending on the actual progress bar implementation.
        // For common code, it's better to avoid direct println if it's tied to console output.
        // The actual progress bar implementation should handle its own rendering.
        // If a newline is truly needed for specific platforms, it should be part of the actual implementation.
        if (index.rem(PROGRESS_UPDATE_SPAN) == 0) {
            // Assuming the actual progress bar handles newlines or cursor management if needed.
            // println("\n") // Potentially remove or make platform-specific
            progressBar.stepTo(index + 1L)
        }
    }

    /**
     * Finishes progress bar progress update by closing it.
     */
    fun end() {
        progressBar.stepTo(prs.size.toLong())
        progressBar.close()
    }
}
