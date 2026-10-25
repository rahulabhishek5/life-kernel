package com.lifekernel.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    @Test
    fun isoDateString_roundTrip() {
        val date = LocalDate.of(2026, 10, 25)
        val text = converters.toIsoDateString(date)
        assertEquals("2026-10-25", text)

        val restored = converters.fromIsoDateString(text)
        assertEquals(date, restored)
    }

    @Test
    fun isoDateString_nullOrBlankReturnsNull() {
        assertNull(converters.fromIsoDateString(null))
        assertNull(converters.fromIsoDateString(""))
        assertNull(converters.fromIsoDateString("   "))
        assertNull(converters.toIsoDateString(null))
    }

    @Test
    fun isoDateString_malformedTextReturnsNull() {
        assertNull(converters.fromIsoDateString("not-a-date"))
        assertNull(converters.fromIsoDateString("2026-02-31")) // invalid calendar date
        assertNull(converters.fromIsoDateString("25-10-2026")) // non-ISO format
        assertNull(converters.fromIsoDateString("2026/10/25"))
    }

    @Test
    fun epochMillis_roundTrip() {
        val instant = Instant.ofEpochMilli(1790000000000L)
        val millis = converters.toEpochMillis(instant)
        assertEquals(1790000000000L, millis)

        val restored = converters.fromEpochMillis(millis)
        assertEquals(instant, restored)
    }

    @Test
    fun epochMillis_nullReturnsNull() {
        assertNull(converters.fromEpochMillis(null))
        assertNull(converters.toEpochMillis(null))
    }

    @Test
    fun epochMillis_zeroAndEpochStart() {
        val epochStart = Instant.EPOCH
        val millis = converters.toEpochMillis(epochStart)
        assertEquals(0L, millis)

        val restored = converters.fromEpochMillis(0L)
        assertEquals(epochStart, restored)
    }
}
