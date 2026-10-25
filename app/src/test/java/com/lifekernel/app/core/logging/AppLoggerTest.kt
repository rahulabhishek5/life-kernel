package com.lifekernel.app.core.logging

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppLoggerTest {

    private class TestLogSink : LogSink {
        val entries = mutableListOf<LogEntry>()

        data class LogEntry(
            val priority: LogPriority,
            val tag: String,
            val message: String,
            val throwable: Throwable?
        )

        override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
            entries.add(LogEntry(priority, tag, message, throwable))
        }
    }

    private val testSink = TestLogSink()

    @Before
    fun setUp() {
        AppLogger.logSink = testSink
    }

    @After
    fun tearDown() {
        AppLogger.isDebuggable = false
        AppLogger.logSink = AndroidLogSink
    }

    @Test
    fun debugMode_evaluatesAndLogsAllLevels() {
        AppLogger.isDebuggable = true

        AppLogger.d("TestTag") { "Debug message" }
        AppLogger.i("TestTag") { "Info message" }
        AppLogger.w("TestTag") { "Warn message" }
        AppLogger.e("TestTag") { "Error message" }

        assertEquals(4, testSink.entries.size)
        assertEquals(LogPriority.DEBUG, testSink.entries[0].priority)
        assertEquals("Debug message", testSink.entries[0].message)

        assertEquals(LogPriority.INFO, testSink.entries[1].priority)
        assertEquals("Info message", testSink.entries[1].message)

        assertEquals(LogPriority.WARN, testSink.entries[2].priority)
        assertEquals("Warn message", testSink.entries[2].message)

        assertEquals(LogPriority.ERROR, testSink.entries[3].priority)
        assertEquals("Error message", testSink.entries[3].message)
    }

    @Test
    fun releaseMode_discardsDebugAndInfoWithoutEvaluatingMessage() {
        AppLogger.isDebuggable = false
        var lambdaEvaluated = false

        AppLogger.d("TestTag") {
            lambdaEvaluated = true
            "Sensitive title: Buy groceries"
        }
        AppLogger.i("TestTag") {
            lambdaEvaluated = true
            "Sensitive note: Secret journal"
        }
        AppLogger.w("TestTag") {
            lambdaEvaluated = true
            "Sensitive warning"
        }

        assertFalse("Message lambda must not be evaluated in release", lambdaEvaluated)
        assertTrue("Sink must receive no entries in release for d/i/w", testSink.entries.isEmpty())
    }

    @Test
    fun releaseMode_errorLogsSanitizedSummaryWithoutEvaluatingUserMessage() {
        AppLogger.isDebuggable = false
        var sensitiveLambdaEvaluated = false

        val testException = IllegalStateException("Disk read timeout")
        AppLogger.e("TestTag", testException) {
            sensitiveLambdaEvaluated = true
            "User content that should not leak: Secret plan"
        }

        assertFalse("Message lambda must not be evaluated in release mode", sensitiveLambdaEvaluated)
        assertEquals(1, testSink.entries.size)
        val entry = testSink.entries[0]
        assertEquals(LogPriority.ERROR, entry.priority)
        assertEquals("IllegalStateException: Disk read timeout", entry.message)
        assertEquals(testException, entry.throwable)
    }
}
