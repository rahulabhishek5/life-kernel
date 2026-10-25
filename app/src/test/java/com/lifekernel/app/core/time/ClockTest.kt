package com.lifekernel.app.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ClockTest {

    @Test
    fun defaultClock_returnsCurrentInstantAndEpochMillis() {
        val clock = DefaultClock()
        val before = System.currentTimeMillis()
        val instant = clock.nowInstant()
        val epochMillis = clock.nowEpochMillis()
        val after = System.currentTimeMillis()

        assertTrue(instant.toEpochMilli() in before..after)
        assertTrue(epochMillis in before..after)
    }

    @Test
    fun fixedClock_returnsDeterministicInstantAndDate() {
        // 2026-10-25T06:00:00Z
        val targetInstant = Instant.parse("2026-10-25T06:00:00Z")
        val clock = FixedClock(currentInstant = targetInstant)

        assertEquals(targetInstant, clock.nowInstant())
        assertEquals(targetInstant.toEpochMilli(), clock.nowEpochMillis())

        val utcDate = clock.today(ZoneId.of("UTC"))
        assertEquals(LocalDate.of(2026, 10, 25), utcDate)

        val utcTime = clock.nowLocalTime(ZoneId.of("UTC"))
        assertEquals(LocalTime.of(6, 0), utcTime)
    }

    @Test
    fun fixedClock_advancesByDuration() {
        val startInstant = Instant.parse("2026-10-25T12:00:00Z")
        val clock = FixedClock(currentInstant = startInstant)

        clock.advanceBy(Duration.ofHours(2))
        assertEquals(Instant.parse("2026-10-25T14:00:00Z"), clock.nowInstant())

        clock.advanceByMillis(500)
        assertEquals(Instant.parse("2026-10-25T14:00:00.500Z"), clock.nowInstant())
    }

    @Test
    fun fixedClock_zoneOffsetChangesDateAcrossMidnight() {
        // 2026-10-24T23:30:00Z -> In UTC it's Oct 24, in UTC+2 it's Oct 25
        val instant = Instant.parse("2026-10-24T23:30:00Z")
        val clock = FixedClock(currentInstant = instant)

        assertEquals(LocalDate.of(2026, 10, 24), clock.today(ZoneId.of("UTC")))
        assertEquals(LocalDate.of(2026, 10, 25), clock.today(ZoneId.of("Europe/Berlin")))
    }
}
