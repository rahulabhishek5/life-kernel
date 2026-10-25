package com.lifekernel.app.domain.model

/**
 * Task priority levels resolving DQ-01 and DB-V6 per DATABASE_SCHEMA.md §5.
 * Values: 0 NONE, 1 LOW, 2 MEDIUM, 3 HIGH.
 */
enum class TaskPriority(val code: Int) {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    companion object {
        fun fromCode(code: Int): TaskPriority =
            entries.find { it.code == code } ?: NONE
    }
}
