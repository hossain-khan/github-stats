package dev.hossain.githubstats.util

import dev.hossain.githubstats.BuildConfig.PROGRESS_UPDATE_SPAN
import dev.hossain.githubstats.model.Issue
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import org.koin.core.component.KoinComponent

/**
 * Provides progress bar updates for [prs] that are being analyzed.
 */
class PrAnalysisProgress(private val prs: List<Issue>) : KoinComponent {
    private lateinit var progressBar: ProgressBar

    /**
     * Provides a progress bar for provided [prs].
     */
    fun start() {
        progressBar =
            getKoin().get<ProgressBarBuilder>()
                .setInitialMax(prs.size.toLong())
                .build()
    }

    /**
     * Provides progress update after each [PROGRESS_UPDATE_SPAN] number of PRs.
     * @param index Current index of PR number that is being analyzed.
     */
    fun publish(index: Int) {
        if (index.rem(PROGRESS_UPDATE_SPAN) == 0) {
            println("\n")
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
