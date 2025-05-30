package dev.hossain.platform

import me.tongfei.progressbar.ConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder as JvmProgressBarBuilder // Alias
import me.tongfei.progressbar.ProgressBarStyle

// Actual ProgressBar now takes the real JVM ProgressBar instance
actual class PlatformProgressBar actual constructor(private val jvmBar: ProgressBar) {
    actual fun stepBy(n: Long) {
        jvmBar.stepBy(n)
    }

    actual fun stepTo(n: Long) {
        jvmBar.stepTo(n)
    }

    actual fun maxHint(n: Long) {
        jvmBar.maxHint(n)
    }

    actual fun close() {
        jvmBar.close()
    }
    actual fun current(): Long = jvmBar.current
    
    actual companion object {
        actual fun builder(): PlatformProgressBarBuilder = PlatformProgressBarBuilder()
    }
}

actual class PlatformProgressBarBuilder {
    internal val jvmBuilder = JvmProgressBarBuilder() // Actual JvmProgressBarBuilder

    actual fun setTaskName(taskName: String): PlatformProgressBarBuilder {
        jvmBuilder.setTaskName(taskName)
        return this
    }

    actual fun setInitialMax(initialMax: Long): PlatformProgressBarBuilder {
        jvmBuilder.setInitialMax(initialMax)
        return this
    }
    
    // Example of how other methods would be added
    fun setStyle(style: ProgressBarStyle): PlatformProgressBarBuilder {
        jvmBuilder.setStyle(style)
        return this
    }
    
    fun setConsumer(consumer: ConsoleProgressBarConsumer): PlatformProgressBarBuilder {
        jvmBuilder.setConsumer(consumer)
        return this
    }


    actual fun build(): PlatformProgressBar {
        // Now this correctly builds the JVM ProgressBar and passes it to PlatformProgressBar's constructor
        val builtJvmBar = jvmBuilder.build()
        return PlatformProgressBar(builtJvmBar)
    }
}
