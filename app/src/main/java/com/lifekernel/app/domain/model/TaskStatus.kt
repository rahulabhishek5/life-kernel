package com.lifekernel.app.domain.model

/**
 * Task status codes per DATABASE_SCHEMA.md §5:
 * 0 OPEN, 1 DONE.
 */
enum class TaskStatus(val code: Int) {
    OPEN(0),
    DONE(1);

    companion object {
        fun fromCode(code: Int): TaskStatus =
            entries.find { it.code == code } ?: OPEN
    }
}
