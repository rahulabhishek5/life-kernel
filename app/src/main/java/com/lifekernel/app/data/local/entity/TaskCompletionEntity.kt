package com.lifekernel.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Task completion record entity per DATABASE_SCHEMA.md §6.3.
 * Single source of truth for task and recurrence completions.
 */
@Entity(
    tableName = "task_completions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["task_id", "occurrence_date"], unique = true),
        Index(value = ["completed_on"]),
        Index(value = ["task_id"])
    ]
)
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "task_id")
    val taskId: Long,

    @ColumnInfo(name = "occurrence_date")
    val occurrenceDate: String? = null,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long,

    @ColumnInfo(name = "completed_on")
    val completedOn: String
)
