package dev.hossain.githubstats.logging

import dev.hossain.githubstats.BuildConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Tests for [Log].
 */
class LogTest {
    private val standardOut = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()

    @Test
    fun `logs verbose message when log level is verbose`() {
        System.setOut(PrintStream(outputStreamCaptor))
        BuildConfig.LOG_LEVEL = Log.VERBOSE
        Log.v("Verbose message")
        assertEquals("Verbose message\n", outputStreamCaptor.toString())
        System.setOut(standardOut)
    }

    @Test
    fun `does not log verbose message when log level is debug`() {
        System.setOut(PrintStream(outputStreamCaptor))
        BuildConfig.LOG_LEVEL = Log.DEBUG
        Log.v("Verbose message")
        assertEquals("", outputStreamCaptor.toString())
        System.setOut(standardOut)
    }

    @Test
    fun `logs debug message when log level is debug`() {
        System.setOut(PrintStream(outputStreamCaptor))
        BuildConfig.LOG_LEVEL = Log.DEBUG
        Log.d("Debug message")
        assertEquals("Debug message\n", outputStreamCaptor.toString())
        System.setOut(standardOut)
    }

    @Test
    fun `does not log debug message when log level is info`() {
        System.setOut(PrintStream(outputStreamCaptor))
        BuildConfig.LOG_LEVEL = Log.INFO
        Log.d("Debug message")
        assertEquals("", outputStreamCaptor.toString())
        System.setOut(standardOut)
    }

    @Test
    fun `logs info message when log level is info`() {
        System.setOut(PrintStream(outputStreamCaptor))
        BuildConfig.LOG_LEVEL = Log.INFO
        Log.i("Info message")
        assertEquals("Info message\n", outputStreamCaptor.toString())
        System.setOut(standardOut)
    }

    @Test
    fun `does not log info message when log level is warning`() {
        System.setOut(PrintStream(outputStreamCaptor))
        BuildConfig.LOG_LEVEL = Log.WARNING
        Log.i("Info message")
        assertEquals("", outputStreamCaptor.toString())
        System.setOut(standardOut)
    }

    @Test
    fun `logs warning message when log level is warning`() {
        System.setOut(PrintStream(outputStreamCaptor))
        BuildConfig.LOG_LEVEL = Log.WARNING
        Log.w("Warning message")
        assertEquals("Warning message\n", outputStreamCaptor.toString())
        System.setOut(standardOut)
    }

    @Test
    fun `does not log any message when log level is none`() {
        System.setOut(PrintStream(outputStreamCaptor))
        BuildConfig.LOG_LEVEL = Log.NONE
        Log.v("Verbose message")
        Log.d("Debug message")
        Log.i("Info message")
        Log.w("Warning message")
        assertEquals("", outputStreamCaptor.toString())
        System.setOut(standardOut)
    }
}