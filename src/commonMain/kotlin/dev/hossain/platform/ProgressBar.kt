package dev.hossain.platform

/**
 * Expected functionality for a progress bar.
 * Instances are created by [PlatformProgressBarBuilder].
 */
expect class PlatformProgressBar {
    fun stepBy(n: Long)
    fun stepTo(n: Long)
    fun maxHint(n: Long)
    fun close()
    fun current(): Long

    companion object {
        fun builder(): PlatformProgressBarBuilder
    }
}

/**
 * Expected functionality for a progress bar builder.
 */
expect class PlatformProgressBarBuilder {
    fun setTaskName(taskName: String): PlatformProgressBarBuilder
    fun setInitialMax(initialMax: Long): PlatformProgressBarBuilder
    // Add other builder methods from the original me.tongfei.progressbar.ProgressBarBuilder as needed
    // e.g., setStyle(), setConsumer(), etc.
    // For now, keeping it simple.
    fun build(): PlatformProgressBar
}
