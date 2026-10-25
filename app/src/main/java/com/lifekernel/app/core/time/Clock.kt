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
    val zoneId: ZoneId
        get() = ZoneId.systemDefault()

    /**
     * Current instant in UTC.
     */
    fun nowInstant(): Instant

    /**
     * Current epoch millisecond timestamp in UTC.
     */
    fun nowEpochMillis(): Long = nowInstant().toEpochMilli()

    /**
     * Current calendar date in the specified [zone] (defaults to clock's [zoneId]).
     */
    fun today(zone: ZoneId = zoneId): LocalDate =
        nowInstant().atZone(zone).toLocalDate()

    /**
     * Current local time of day in the specified [zone] (defaults to clock's [zoneId]).
     */
    fun nowLocalTime(zone: ZoneId = zoneId): LocalTime =
        nowInstant().atZone(zone).toLocalTime()

    /**
     * Current local date-time in the specified [zone] (defaults to clock's [zoneId]).
     */
    fun nowLocalDateTime(zone: ZoneId = zoneId): LocalDateTime =
        nowInstant().atZone(zone).toLocalDateTime()
}

/**
 * Default clock implementation backed by [java.time.Clock.systemUTC].
 */
class DefaultClock(
    private val javaClock: java.time.Clock = java.time.Clock.systemUTC(),
    override val zoneId: ZoneId = ZoneId.systemDefault()
) : Clock {
    override fun nowInstant(): Instant = javaClock.instant()
}

/**
 * Deterministic clock implementation for tests with controllable time.
 */
class FixedClock(
    private var currentInstant: Instant = Instant.EPOCH,
    override val zoneId: ZoneId = ZoneId.of("UTC")
) : Clock {
    override fun nowInstant(): Instant = currentInstant

    override fun today(zone: ZoneId): LocalDate =
        currentInstant.atZone(zone).toLocalDate()

    override fun nowLocalTime(zone: ZoneId): LocalTime =
        currentInstant.atZone(zone).toLocalTime()

    override fun nowLocalDateTime(zone: ZoneId): LocalDateTime =
        currentInstant.atZone(zone).toLocalDateTime()

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
