package com.lifekernel.app.infra.backup.serializer

import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serialization DTO for the `task_completions` table in JSONL format per DATABASE_SCHEMA.md §6.3 & §11.1.
 */
@Serializable
data class TaskCompletionBackupDto(
    @SerialName("id")
    val id: Long,

    @SerialName("task_id")
    val taskId: Long,

    @SerialName("occurrence_date")
    val occurrenceDate: String? = null,

    @SerialName("completed_at")
    val completedAt: Long,

    @SerialName("completed_on")
    val completedOn: String
)

fun TaskCompletionEntity.toBackupDto(): TaskCompletionBackupDto = TaskCompletionBackupDto(
    id = id,
    taskId = taskId,
    occurrenceDate = occurrenceDate,
    completedAt = completedAt,
    completedOn = completedOn
)

fun TaskCompletionBackupDto.toEntity(): TaskCompletionEntity = TaskCompletionEntity(
    id = id,
    taskId = taskId,
    occurrenceDate = occurrenceDate,
    completedAt = completedAt,
    completedOn = completedOn
)
