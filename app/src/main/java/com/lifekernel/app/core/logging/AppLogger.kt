package com.lifekernel.app.core.logging

import android.util.Log

/**
 * Privacy-preserving logging wrapper for Life Kernel.
 *
 * Invariants:
 * 1. Release builds NEVER log user content (titles, notes, URLs, amounts, etc.).
 * 2. In debug builds, logs may name operations, counts, or component lifecycles, but never personal content.
 * 3. No external crash-reporting or analytics SDKs are used.
 */
object AppLogger {
    @PublishedApi
    internal const val DEFAULT_TAG = "LifeKernel"

    var isDebuggable: Boolean = false
    var logSink: LogSink = AndroidLogSink

    /**
     * Debug-level logging. Discarded in release builds without evaluating the message lambda.
     */
    inline fun d(tag: String = DEFAULT_TAG, message: () -> String) {
        if (isDebuggable) {
            logSink.log(LogPriority.DEBUG, tag, message(), null)
        }
    }

    /**
     * Info-level logging. Discarded in release builds without evaluating the message lambda.
     */
    inline fun i(tag: String = DEFAULT_TAG, message: () -> String) {
        if (isDebuggable) {
            logSink.log(LogPriority.INFO, tag, message(), null)
        }
    }

    /**
     * Warning-level logging. Discarded in release builds without evaluating the message lambda.
     */
    inline fun w(tag: String = DEFAULT_TAG, throwable: Throwable? = null, message: () -> String) {
        if (isDebuggable) {
            logSink.log(LogPriority.WARN, tag, message(), throwable)
        }
    }

    /**
     * Error-level logging.
     * In debug builds: logs the provided message and stack trace.
     * In release builds: never evaluates [message] to prevent user content leakage;
     * only logs a sanitized technical summary derived from [throwable].
     */
    inline fun e(tag: String = DEFAULT_TAG, throwable: Throwable? = null, message: () -> String) {
        if (isDebuggable) {
            logSink.log(LogPriority.ERROR, tag, message(), throwable)
        } else {
            val sanitized = throwable?.let {
                "${it.javaClass.simpleName}: ${it.message ?: "no details"}"
            } ?: "Sanitized internal error"
            logSink.log(LogPriority.ERROR, tag, sanitized, throwable)
        }
    }
}

enum class LogPriority {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

fun interface LogSink {
    fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?)
}

object AndroidLogSink : LogSink {
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        when (priority) {
            LogPriority.DEBUG -> Log.d(tag, message, throwable)
            LogPriority.INFO -> Log.i(tag, message, throwable)
            LogPriority.WARN -> Log.w(tag, message, throwable)
            LogPriority.ERROR -> Log.e(tag, message, throwable)
        }
    }
}
