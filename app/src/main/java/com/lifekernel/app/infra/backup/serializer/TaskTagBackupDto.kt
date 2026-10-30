package com.lifekernel.app.infra.backup.serializer

import com.lifekernel.app.data.local.entity.TaskTagEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serialization DTO for the `task_tags` join table in JSONL format per DATABASE_SCHEMA.md §6.4 & §11.1.
 */
@Serializable
data class TaskTagBackupDto(
    @SerialName("task_id")
    val taskId: Long,

    @SerialName("tag_id")
    val tagId: Long
)

fun TaskTagEntity.toBackupDto(): TaskTagBackupDto = TaskTagBackupDto(
    taskId = taskId,
    tagId = tagId
)

fun TaskTagBackupDto.toEntity(): TaskTagEntity = TaskTagEntity(
    taskId = taskId,
    tagId = tagId
)
