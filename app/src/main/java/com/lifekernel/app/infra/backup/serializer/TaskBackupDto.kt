package com.lifekernel.app.infra.backup.serializer

import com.lifekernel.app.data.local.entity.TaskEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serialization DTO for the `tasks` table in JSONL format per DATABASE_SCHEMA.md §6.2 & §11.1.
 */
@Serializable
data class TaskBackupDto(
    @SerialName("id")
    val id: Long,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("status")
    val status: Int = 0,

    @SerialName("priority")
    val priority: Int = 0,

    @SerialName("start_date")
    val startDate: String? = null,

    @SerialName("due_date")
    val dueDate: String? = null,

    @SerialName("parent_id")
    val parentId: Long? = null,

    @SerialName("sort_order")
    val sortOrder: Int = 0,

    @SerialName("recur_type")
    val recurType: Int = 0,

    @SerialName("recur_interval")
    val recurInterval: Int = 1,

    @SerialName("recur_weekdays")
    val recurWeekdays: Int = 0,

    @SerialName("recur_until")
    val recurUntil: String? = null,

    @SerialName("created_at")
    val createdAt: Long,

    @SerialName("updated_at")
    val updatedAt: Long
)

fun TaskEntity.toBackupDto(): TaskBackupDto = TaskBackupDto(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    startDate = startDate,
    dueDate = dueDate,
    parentId = parentId,
    sortOrder = sortOrder,
    recurType = recurType,
    recurInterval = recurInterval,
    recurWeekdays = recurWeekdays,
    recurUntil = recurUntil,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TaskBackupDto.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    startDate = startDate,
    dueDate = dueDate,
    parentId = parentId,
    sortOrder = sortOrder,
    recurType = recurType,
    recurInterval = recurInterval,
    recurWeekdays = recurWeekdays,
    recurUntil = recurUntil,
    createdAt = createdAt,
    updatedAt = updatedAt
)
