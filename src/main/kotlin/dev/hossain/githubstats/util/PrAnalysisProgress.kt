package dev.hossain.githubstats.util

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
    fun start(): ProgressBar {
        progressBar = getKoin().get<ProgressBarBuilder>()
            .setInitialMax(prs.size.toLong())
            .build()

        return progressBar
    }

    /**
     * Finishes progress bar progress update by closing it.
     */
    fun end() {
        progressBar.stepTo(prs.size.toLong())
        progressBar.close()
    }
}
