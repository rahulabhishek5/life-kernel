package com.lifekernel.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for task-tag associations per DATABASE_SCHEMA.md §6.4.
 */
@Dao
interface TaskTagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskTag(taskTag: TaskTagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskTags(taskTags: List<TaskTagEntity>): List<Long>

    @Delete
    suspend fun deleteTaskTag(taskTag: TaskTagEntity): Int

    @Query("DELETE FROM task_tags WHERE task_id = :taskId")
    suspend fun deleteTagsForTask(taskId: Long): Int

    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN task_tags ON tags.id = task_tags.tag_id
        WHERE task_tags.task_id = :taskId
        ORDER BY tags.name COLLATE NOCASE ASC
        """
    )
    suspend fun getTagsForTask(taskId: Long): List<TagEntity>

    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN task_tags ON tags.id = task_tags.tag_id
        WHERE task_tags.task_id = :taskId
        ORDER BY tags.name COLLATE NOCASE ASC
        """
    )
    fun observeTagsForTask(taskId: Long): Flow<List<TagEntity>>

    @Query("SELECT task_id FROM task_tags WHERE tag_id = :tagId")
    suspend fun getTaskIdsForTag(tagId: Long): List<Long>

    @Query("SELECT * FROM task_tags ORDER BY task_id ASC, tag_id ASC")
    suspend fun getAllTaskTags(): List<TaskTagEntity>
}
