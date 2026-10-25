package com.lifekernel.app.core.time

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Clock abstraction to allow deterministic testing of time-dependent logic.
 *
 * Invariant: Domain and data layers must obtain all current times/dates through this interface,
 * never calling System.currentTimeMillis(), Instant.now(), or LocalDate.now() directly.
 */
interface Clock {
    /**
     * Current instant in UTC.
     */
    fun nowInstant(): Instant

    /**
     * Current epoch millisecond timestamp in UTC.
     */
    fun nowEpochMillis(): Long = nowInstant().toEpochMilli()

    /**
     * Current calendar date in the specified [zoneId] (defaults to system default zone).
     */
    fun today(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
        nowInstant().atZone(zoneId).toLocalDate()

    /**
     * Current local time of day in the specified [zoneId] (defaults to system default zone).
     */
    fun nowLocalTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalTime =
        nowInstant().atZone(zoneId).toLocalTime()

    /**
     * Current local date-time in the specified [zoneId] (defaults to system default zone).
     */
    fun nowLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        nowInstant().atZone(zoneId).toLocalDateTime()
}

/**
 * Default clock implementation backed by [java.time.Clock.systemUTC].
 */
class DefaultClock(
    private val javaClock: java.time.Clock = java.time.Clock.systemUTC()
) : Clock {
    override fun nowInstant(): Instant = javaClock.instant()
}

/**
 * Deterministic clock implementation for tests with controllable time.
 */
class FixedClock(
    private var currentInstant: Instant = Instant.EPOCH,
    private val defaultZoneId: ZoneId = ZoneId.of("UTC")
) : Clock {
    override fun nowInstant(): Instant = currentInstant

    override fun today(zoneId: ZoneId): LocalDate =
        currentInstant.atZone(zoneId).toLocalDate()

    override fun nowLocalTime(zoneId: ZoneId): LocalTime =
        currentInstant.atZone(zoneId).toLocalTime()

    override fun nowLocalDateTime(zoneId: ZoneId): LocalDateTime =
        currentInstant.atZone(zoneId).toLocalDateTime()

    fun setInstant(instant: Instant) {
        currentInstant = instant
    }

    fun advanceBy(duration: Duration) {
        currentInstant = currentInstant.plus(duration)
    }

    fun advanceByMillis(millis: Long) {
        currentInstant = currentInstant.plusMillis(millis)
    }
}
