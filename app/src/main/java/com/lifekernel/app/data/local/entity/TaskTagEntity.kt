package com.lifekernel.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Task-Tag join table entity per DATABASE_SCHEMA.md §6.4.
 * Composite PK (task_id, tag_id) with cascading deletion from both sides.
 */
@Entity(
    tableName = "task_tags",
    primaryKeys = ["task_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tag_id"])
    ]
)
data class TaskTagEntity(
    @ColumnInfo(name = "task_id")
    val taskId: Long,

    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
