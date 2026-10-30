package com.lifekernel.app.domain.model

import java.time.Instant

/**
 * Subtask domain model per PRD §8.3, FR-TASK-002, and DATABASE_SCHEMA.md §6.2.
 *
 * Invariant:
 * - Subtasks represent checklist items of a single parent task (depth is strictly 1).
 * - Completion state is decoupled from parent task status.
 */
data class Subtask(
    val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val position: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val createdInstant: Instant get() = Instant.ofEpochMilli(createdAt)
    val updatedInstant: Instant get() = Instant.ofEpochMilli(updatedAt)
}
