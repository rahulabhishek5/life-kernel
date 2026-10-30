package com.lifekernel.app.data.repository.mappers

import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.domain.model.Subtask
import com.lifekernel.app.domain.model.TaskStatus

/**
 * Bidirectional mapper between [TaskEntity] and domain [Subtask].
 */
fun TaskEntity.toSubtask(): Subtask = Subtask(
    id = id,
    taskId = parentId ?: 0L,
    title = title,
    isCompleted = status == TaskStatus.DONE.code,
    position = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Subtask.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = null,
    status = if (isCompleted) TaskStatus.DONE.code else TaskStatus.OPEN.code,
    priority = 0,
    startDate = null,
    dueDate = null,
    parentId = taskId,
    sortOrder = position,
    recurType = 0,
    recurInterval = 1,
    recurWeekdays = 0,
    recurUntil = null,
    createdAt = createdAt,
    updatedAt = updatedAt
)
