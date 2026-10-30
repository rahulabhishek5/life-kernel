package com.lifekernel.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifekernel.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tasks per DATABASE_SCHEMA.md §6.2.
 */
@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity): Int

    @Delete
    suspend fun deleteTask(task: TaskEntity): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long): Int

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun observeTaskById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE parent_id = :parentId ORDER BY sort_order ASC, id ASC")
    suspend fun getSubtasks(parentId: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE parent_id = :parentId ORDER BY sort_order ASC, id ASC")
    fun observeSubtasks(parentId: Long): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE parent_id IS NULL
        ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END, due_date ASC, priority DESC, id ASC
        """
    )
    fun observeAllTopLevelTasks(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE parent_id IS NULL
        ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END, due_date ASC, priority DESC, id ASC
        """
    )
    suspend fun getAllTopLevelTasks(): List<TaskEntity>

    @Query(
        """
        SELECT * FROM tasks
        WHERE status = 0 AND parent_id IS NULL AND due_date IS NOT NULL AND due_date <= :today
        ORDER BY due_date ASC, priority DESC, id ASC
        """
    )
    fun observeOpenTasksDueOnOrBefore(today: String): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE status = 0 AND parent_id IS NULL AND due_date = :date
        ORDER BY priority DESC, id ASC
        """
    )
    fun observeOpenTasksForDate(date: String): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE status = 0 AND parent_id IS NULL AND due_date IS NULL
        ORDER BY priority DESC, id ASC
        """
    )
    fun observeOpenTasksWithoutDueDate(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT tasks.* FROM tasks
        INNER JOIN task_completions ON tasks.id = task_completions.task_id
        WHERE task_completions.completed_on = :localDate
        ORDER BY task_completions.completed_at DESC
        """
    )
    fun observeCompletedTasksForDate(localDate: String): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: Int, updatedAt: Long): Int

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllTasks(): List<TaskEntity>
}
