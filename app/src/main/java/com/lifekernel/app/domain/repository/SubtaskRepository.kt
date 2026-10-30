package com.lifekernel.app.domain.repository

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.model.Subtask
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for subtask operations per ARCHITECTURE.md §3 & §6 and PRD §8.3.
 */
interface SubtaskRepository {

    /**
     * Observes subtasks for a specific parent task, ordered by position ascending.
     */
    fun observeSubtasksForTask(taskId: Long): Flow<List<Subtask>>

    /**
     * Retrieves subtasks for a specific parent task, ordered by position ascending.
     */
    suspend fun getSubtasksForTask(taskId: Long): List<Subtask>

    /**
     * Retrieves a single subtask by its ID, or null if not found.
     */
    suspend fun getSubtaskById(subtaskId: Long): Subtask?

    /**
     * Adds a subtask to the parent task, automatically assigning the next sequential position.
     */
    suspend fun addSubtask(taskId: Long, title: String): AppResult<Long>

    /**
     * Toggles the completion state of a subtask.
     * Invariant: Does not alter parent task completion state (FR-TASK-002).
     */
    suspend fun toggleSubtask(subtaskId: Long): AppResult<Unit>

    /**
     * Deletes a subtask and compacts remaining subtask positions to maintain continuity.
     */
    suspend fun deleteSubtask(subtaskId: Long): AppResult<Unit>

    /**
     * Updates positions of subtasks to match the provided ordered list of subtask IDs.
     */
    suspend fun reorderSubtasks(taskId: Long, subtaskIds: List<Long>): AppResult<Unit>
}
