package com.lifekernel.app.data.repository.mappers

import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import com.lifekernel.app.domain.model.TaskCompletion
import java.time.Instant
import java.time.LocalDate

/**
 * Bidirectional mappers between [TaskCompletionEntity] and domain [TaskCompletion].
 */
fun TaskCompletionEntity.toDomain(): TaskCompletion = TaskCompletion(
    id = id,
    taskId = taskId,
    occurrenceDate = occurrenceDate?.let { LocalDate.parse(it) },
    completedAt = Instant.ofEpochMilli(completedAt),
    completedOn = LocalDate.parse(completedOn)
)

fun TaskCompletion.toEntity(): TaskCompletionEntity = TaskCompletionEntity(
    id = id,
    taskId = taskId,
    occurrenceDate = occurrenceDate?.toString(),
    completedAt = completedAt.toEpochMilli(),
    completedOn = completedOn.toString()
)
