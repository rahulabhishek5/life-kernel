package com.lifekernel.app.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Task aggregate root domain model per DATABASE_SCHEMA.md §6.2.
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.OPEN,
    val priority: TaskPriority = TaskPriority.NONE,
    val startDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val parentId: Long? = null,
    val sortOrder: Int = 0,
    val recurrence: TaskRecurrence = TaskRecurrence(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val tags: List<Tag> = emptyList(),
    val subtasks: List<Task> = emptyList()
) {
    val isSubtask: Boolean
        get() = parentId != null

    val isCompleted: Boolean
        get() = status == TaskStatus.DONE
}
