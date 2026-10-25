package com.lifekernel.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Task entity per DATABASE_SCHEMA.md §6.2.
 * Supports top-level tasks, single-depth subtasks, and recurring tasks.
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["status", "due_date"]),
        Index(value = ["parent_id"]),
        Index(value = ["due_date"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "status", defaultValue = "0")
    val status: Int = 0,

    @ColumnInfo(name = "priority", defaultValue = "0")
    val priority: Int = 0,

    @ColumnInfo(name = "start_date")
    val startDate: String? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: String? = null,

    @ColumnInfo(name = "parent_id")
    val parentId: Long? = null,

    @ColumnInfo(name = "sort_order", defaultValue = "0")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "recur_type", defaultValue = "0")
    val recurType: Int = 0,

    @ColumnInfo(name = "recur_interval", defaultValue = "1")
    val recurInterval: Int = 1,

    @ColumnInfo(name = "recur_weekdays", defaultValue = "0")
    val recurWeekdays: Int = 0,

    @ColumnInfo(name = "recur_until")
    val recurUntil: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
