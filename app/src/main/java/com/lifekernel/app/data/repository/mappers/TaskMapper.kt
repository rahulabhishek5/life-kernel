package com.lifekernel.app.data.repository.mappers

import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.domain.model.RecurrenceType
import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskRecurrence
import com.lifekernel.app.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate

/**
 * Bidirectional mappers between [TaskEntity] and domain [Task].
 */
fun TaskEntity.toDomain(
    tags: List<Tag> = emptyList(),
    subtasks: List<Task> = emptyList()
): Task = Task(
    id = id,
    title = title,
    description = description,
    status = TaskStatus.fromCode(status),
    priority = TaskPriority.fromCode(priority),
    startDate = startDate?.let { LocalDate.parse(it) },
    dueDate = dueDate?.let { LocalDate.parse(it) },
    parentId = parentId,
    sortOrder = sortOrder,
    recurrence = TaskRecurrence(
        type = RecurrenceType.fromCode(recurType),
        interval = recurInterval,
        weekdaysMask = recurWeekdays,
        untilDate = recurUntil?.let { LocalDate.parse(it) }
    ),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
    tags = tags,
    subtasks = subtasks
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    status = status.code,
    priority = priority.code,
    startDate = startDate?.toString(),
    dueDate = dueDate?.toString(),
    parentId = parentId,
    sortOrder = sortOrder,
    recurType = recurrence.type.code,
    recurInterval = recurrence.interval,
    recurWeekdays = recurrence.weekdaysMask,
    recurUntil = recurrence.untilDate?.toString(),
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli()
)
