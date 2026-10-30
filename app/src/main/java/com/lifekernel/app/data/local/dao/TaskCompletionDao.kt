package com.lifekernel.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for task completions per DATABASE_SCHEMA.md §6.3.
 */
@Dao
interface TaskCompletionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCompletion(completion: TaskCompletionEntity): Long

    @Delete
    suspend fun deleteCompletion(completion: TaskCompletionEntity): Int

    @Query("DELETE FROM task_completions WHERE task_id = :taskId AND occurrence_date = :occurrenceDate")
    suspend fun deleteCompletionForOccurrence(taskId: Long, occurrenceDate: String): Int

    @Query("DELETE FROM task_completions WHERE task_id = :taskId AND occurrence_date IS NULL")
    suspend fun deleteCompletionForNonRecurring(taskId: Long): Int

    @Query("SELECT * FROM task_completions WHERE task_id = :taskId ORDER BY completed_at DESC")
    suspend fun getCompletionsForTask(taskId: Long): List<TaskCompletionEntity>

    @Query("SELECT * FROM task_completions WHERE task_id = :taskId ORDER BY completed_at DESC")
    fun observeCompletionsForTask(taskId: Long): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions WHERE completed_on = :localDate ORDER BY completed_at DESC")
    suspend fun getCompletionsForDate(localDate: String): List<TaskCompletionEntity>

    @Query("SELECT COUNT(*) FROM task_completions WHERE completed_on = :localDate")
    fun observeCompletedCountForDate(localDate: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM task_completions WHERE completed_on = :localDate")
    suspend fun getCompletedCountForDate(localDate: String): Int

    @Query("SELECT * FROM task_completions ORDER BY id ASC")
    suspend fun getAllCompletions(): List<TaskCompletionEntity>
}
