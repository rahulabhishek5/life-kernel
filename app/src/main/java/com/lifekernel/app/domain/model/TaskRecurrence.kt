package com.lifekernel.app.domain.model

import java.time.LocalDate

/**
 * Recurrence types per DATABASE_SCHEMA.md §5:
 * 0 NONE, 1 DAILY, 2 WEEKLY, 3 MONTHLY.
 */
enum class RecurrenceType(val code: Int) {
    NONE(0),
    DAILY(1),
    WEEKLY(2),
    MONTHLY(3);

    companion object {
        fun fromCode(code: Int): RecurrenceType =
            entries.find { it.code == code } ?: NONE
    }
}

/**
 * Recurrence configuration for a recurring task series per DATABASE_SCHEMA.md §6.2.
 */
data class TaskRecurrence(
    val type: RecurrenceType = RecurrenceType.NONE,
    val interval: Int = 1,
    val weekdaysMask: Int = 0,
    val untilDate: LocalDate? = null
) {
    val isRecurring: Boolean
        get() = type != RecurrenceType.NONE
}
