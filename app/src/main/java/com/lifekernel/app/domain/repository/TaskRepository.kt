package com.lifekernel.app.domain.repository

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for Task aggregate root per ARCHITECTURE.md §5 and §6.
 *
 * Invariant: Returns domain models only; never exposes Room entities, DAOs, or database cursors.
 */
interface TaskRepository {

    /**
     * Observes open, non-subtask tasks whose due date is on or before [date] (overdue + due today),
     * ordered by due date ascending, priority descending, id ascending.
     */
    fun observeOpenTasksDueOnOrBefore(date: LocalDate): Flow<List<Task>>

    /**
     * Observes open, non-subtask tasks whose due date matches [date],
     * ordered by priority descending, id ascending.
     */
    fun observeOpenTasksForDate(date: LocalDate): Flow<List<Task>>

    /**
     * Observes open, non-subtask tasks without any due date,
     * ordered by priority descending, id ascending.
     */
    fun observeOpenTasksWithoutDueDate(): Flow<List<Task>>

    /**
     * Observes tasks completed on [date] (based on the local date snapshot completed_on),
     * ordered by completed_at descending.
     */
    fun observeCompletedTasksForDate(date: LocalDate): Flow<List<Task>>

    /**
     * Observes the count of completions recorded on [date].
     */
    fun observeCompletedCountForDate(date: LocalDate): Flow<Int>

    /**
     * Observes all non-subtask tasks across all dates, ordered by due date ascending,
     * priority descending, id ascending.
     */
    fun observeAllTasks(): Flow<List<Task>>

    /**
     * Fetches all non-subtask tasks across all dates.
     */
    suspend fun getAllTasks(): List<Task>

    /**
     * Observes a single task by its [id], emitting null if the task does not exist.
     */
    fun observeTaskById(id: Long): Flow<Task?>

    /**
     * Observes direct subtasks for [parentId], ordered by sort_order ascending, id ascending.
     */
    fun observeSubtasks(parentId: Long): Flow<List<Task>>

    /**
     * Fetches a single task by its [id], returning null if not found.
     */
    suspend fun getTaskById(id: Long): Task?

    /**
     * Creates a new task and associates it with [tagIds] in a single transaction.
     * Returns the created task ID on success.
     */
    suspend fun createTask(task: Task, tagIds: List<Long> = emptyList()): AppResult<Long>

    /**
     * Updates an existing task and optionally synchronizes its tags in a single transaction.
     */
    suspend fun updateTask(task: Task, tagIds: List<Long>? = null): AppResult<Unit>

    /**
     * Deletes a task by [taskId].
     * Cascades delete subtasks, completions, and tag associations, followed by orphan tag cleanup.
     */
    suspend fun deleteTask(taskId: Long): AppResult<Unit>

    /**
     * Atomically marks a task as completed: inserts a completion record with UTC instant
     * and local date snapshot, and updates task status to DONE.
     * Re-completing an already completed task is rejected.
     */
    suspend fun completeTask(taskId: Long, occurrenceDate: LocalDate? = null): AppResult<Unit>

    /**
     * Atomically reopens a completed task: removes the completion record and reverts status to OPEN.
     * Reopening an already open task is rejected.
     */
    suspend fun reopenTask(taskId: Long, occurrenceDate: LocalDate? = null): AppResult<Unit>
}
