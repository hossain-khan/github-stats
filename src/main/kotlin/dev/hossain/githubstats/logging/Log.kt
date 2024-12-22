package dev.hossain.githubstats.logging

import dev.hossain.githubstats.BuildConfig

/**
 * A simple logger that logs to system output stream (console).
 */
object Log {
    /**
     * Verbose level message
     */
    const val VERBOSE = 1

    /**
     * Debug level message
     */
    const val DEBUG = 2

    /**
     * Info level message
     */
    const val INFO = 3

    /**
     * Warning level message
     */
    const val WARNING = 4

    /**
     * No long message
     */
    const val NONE = 5

    fun v(msg: String): Unit = log(VERBOSE, msg)

    fun d(msg: String): Unit = log(DEBUG, msg)

    fun i(msg: String): Unit = log(INFO, msg)

    fun w(msg: String): Unit = log(WARNING, msg)

    /**
     * Logs only if set log level
     */
    private fun log(
        logLevel: Int,
        logMessage: String,
    ) {
        if (logLevel >= BuildConfig.logLevel) {
            val blue = "\u001B[34m"
            val green = "\u001B[32m"
            val orange = "\u001B[38;2;255;165;0m"
            val resetColor = "\u001B[0m"
            when (logLevel) {
                VERBOSE -> println(logMessage)
                DEBUG -> println("${blue}$logMessage$resetColor")
                INFO -> println("${green}$logMessage$resetColor")
                WARNING -> println("${orange}$logMessage$resetColor")
                NONE -> println(logMessage)
            }
        }
    }
}
