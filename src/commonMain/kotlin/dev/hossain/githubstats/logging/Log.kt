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

    // ANSI color codes for console output
    private const val COLOR_BLUE = "\u001B[34m"
    private const val COLOR_GREEN = "\u001B[32m"
    private const val COLOR_ORANGE = "\u001B[38;2;255;165;0m"
    private const val COLOR_RESET = "\u001B[0m"

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
            when (logLevel) {
                VERBOSE -> println(logMessage)
                DEBUG -> println("${COLOR_BLUE}$logMessage$COLOR_RESET")
                INFO -> println("${COLOR_GREEN}$logMessage$COLOR_RESET")
                WARNING -> println("${COLOR_ORANGE}$logMessage$COLOR_RESET")
                NONE -> println(logMessage)
            }
        }
    }
}
