package com.lifekernel.app.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Task completion domain model per DATABASE_SCHEMA.md §6.3.
 * Preserves the exact local date snapshot [completedOn] alongside UTC instant [completedAt].
 */
data class TaskCompletion(
    val id: Long = 0,
    val taskId: Long,
    val occurrenceDate: LocalDate? = null,
    val completedAt: Instant,
    val completedOn: LocalDate
)
