package com.lifekernel.app.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Room type converters for ISO date strings and UTC epoch milliseconds.
 * Includes defensive fallback handling for malformed values.
 */
class Converters {

    @TypeConverter
    fun fromEpochMillis(value: Long?): Instant? {
        return value?.let {
            try {
                Instant.ofEpochMilli(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun toEpochMillis(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromIsoDateString(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    @TypeConverter
    fun toIsoDateString(date: LocalDate?): String? {
        return date?.toString()
    }
}
