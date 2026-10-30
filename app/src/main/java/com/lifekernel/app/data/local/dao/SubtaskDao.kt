package com.lifekernel.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifekernel.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for subtasks per DATABASE_SCHEMA.md §6.2.
 * Subtasks are stored in the tasks table with parent_id referencing their parent task.
 */
@Dao
interface SubtaskDao {

    @Query("SELECT * FROM tasks WHERE parent_id = :taskId ORDER BY sort_order ASC, id ASC")
    fun observeSubtasksForTask(taskId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE parent_id = :taskId ORDER BY sort_order ASC, id ASC")
    suspend fun getSubtasksForTask(taskId: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id AND parent_id IS NOT NULL LIMIT 1")
    suspend fun getSubtaskById(id: Long): TaskEntity?

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM tasks WHERE parent_id = :taskId")
    suspend fun getMaxPositionForTask(taskId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSubtask(subtask: TaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: TaskEntity): Int

    @Query("UPDATE tasks SET status = :status, updated_at = :updatedAt WHERE id = :id AND parent_id IS NOT NULL")
    suspend fun updateSubtaskCompletion(id: Long, status: Int, updatedAt: Long): Int

    @Query("UPDATE tasks SET sort_order = :position, updated_at = :updatedAt WHERE id = :id AND parent_id IS NOT NULL")
    suspend fun updateSubtaskPosition(id: Long, position: Int, updatedAt: Long): Int

    @Query("DELETE FROM tasks WHERE id = :id AND parent_id IS NOT NULL")
    suspend fun deleteSubtaskById(id: Long): Int
}
