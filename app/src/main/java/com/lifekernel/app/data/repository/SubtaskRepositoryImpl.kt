package com.lifekernel.app.data.repository

import androidx.room.withTransaction
import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.data.repository.mappers.toSubtask
import com.lifekernel.app.domain.model.Subtask
import com.lifekernel.app.domain.model.SubtaskValidator
import com.lifekernel.app.domain.repository.SubtaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Room-backed implementation of [SubtaskRepository] per ARCHITECTURE.md §3, §5, §6.
 *
 * Invariants:
 * - Mutations run in atomic Room transactions (database.withTransaction).
 * - All DB operations dispatched on [dispatcher] (off-main-thread).
 * - Subtask completion state is decoupled from parent task status (FR-TASK-002, AC-TASK).
 * - Deleting a subtask re-indexes remaining subtasks to maintain position continuity.
 */
class SubtaskRepositoryImpl(
    private val database: LifeKernelDatabase,
    private val clock: Clock,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SubtaskRepository {

    override fun observeSubtasksForTask(taskId: Long): Flow<List<Subtask>> {
        return database.subtaskDao().observeSubtasksForTask(taskId)
            .map { entities -> entities.map { it.toSubtask() } }
            .flowOn(dispatcher)
    }

    override suspend fun getSubtasksForTask(taskId: Long): List<Subtask> = withContext(dispatcher) {
        database.subtaskDao().getSubtasksForTask(taskId).map { it.toSubtask() }
    }

    override suspend fun getSubtaskById(subtaskId: Long): Subtask? = withContext(dispatcher) {
        database.subtaskDao().getSubtaskById(subtaskId)?.toSubtask()
    }

    override suspend fun addSubtask(
        taskId: Long,
        title: String
    ): AppResult<Long> = withContext(dispatcher) {
        val validation = SubtaskValidator.validateTitle(title)
        if (validation.isFailure) {
            return@withContext AppResult.failure(validation.errorOrNull()!!)
        }
        val validatedTitle = validation.getOrNull()!!

        try {
            database.withTransaction {
                val parent = database.taskDao().getTaskById(taskId)
                    ?: return@withTransaction AppResult.failure(
                        AppError.NotFound("Parent task not found with ID: $taskId")
                    )

                if (parent.parentId != null) {
                    return@withTransaction AppResult.failure(
                        AppError.Validation(
                            message = "Subtasks cannot have their own subtasks (maximum depth is 1)",
                            field = "subtasks"
                        )
                    )
                }

                if (parent.recurType != 0) {
                    return@withTransaction AppResult.failure(
                        AppError.Validation(
                            message = "Recurring tasks cannot have subtasks",
                            field = "subtasks"
                        )
                    )
                }

                val currentMax = database.subtaskDao().getMaxPositionForTask(taskId)
                val nextPosition = if (currentMax < 0) 0 else currentMax + 1
                val nowEpoch = clock.nowEpochMillis()

                val entity = TaskEntity(
                    title = validatedTitle,
                    description = null,
                    status = 0,
                    priority = 0,
                    startDate = null,
                    dueDate = null,
                    parentId = taskId,
                    sortOrder = nextPosition,
                    recurType = 0,
                    recurInterval = 1,
                    recurWeekdays = 0,
                    recurUntil = null,
                    createdAt = nowEpoch,
                    updatedAt = nowEpoch
                )

                val newId = database.subtaskDao().insertSubtask(entity)
                AppResult.success(newId)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to add subtask for task $taskId", e))
        }
    }

    override suspend fun toggleSubtask(subtaskId: Long): AppResult<Unit> = withContext(dispatcher) {
        try {
            database.withTransaction {
                val existing = database.subtaskDao().getSubtaskById(subtaskId)
                    ?: return@withTransaction AppResult.failure(
                        AppError.NotFound("Subtask not found with ID: $subtaskId")
                    )

                val newStatus = if (existing.status == 1) 0 else 1
                val nowEpoch = clock.nowEpochMillis()

                database.subtaskDao().updateSubtaskCompletion(subtaskId, newStatus, nowEpoch)
                AppResult.success(Unit)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to toggle subtask $subtaskId", e))
        }
    }

    override suspend fun deleteSubtask(subtaskId: Long): AppResult<Unit> = withContext(dispatcher) {
        try {
            database.withTransaction {
                val existing = database.subtaskDao().getSubtaskById(subtaskId)
                    ?: return@withTransaction AppResult.failure(
                        AppError.NotFound("Subtask not found with ID: $subtaskId")
                    )

                val parentId = existing.parentId
                    ?: return@withTransaction AppResult.failure(
                        AppError.NotFound("Subtask $subtaskId is missing parent task reference")
                    )

                database.subtaskDao().deleteSubtaskById(subtaskId)

                // Maintain position continuity without gaps
                val remaining = database.subtaskDao().getSubtasksForTask(parentId)
                val nowEpoch = clock.nowEpochMillis()
                remaining.forEachIndexed { index, subtaskEntity ->
                    if (subtaskEntity.sortOrder != index) {
                        database.subtaskDao().updateSubtaskPosition(subtaskEntity.id, index, nowEpoch)
                    }
                }

                AppResult.success(Unit)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to delete subtask $subtaskId", e))
        }
    }

    override suspend fun reorderSubtasks(
        taskId: Long,
        subtaskIds: List<Long>
    ): AppResult<Unit> = withContext(dispatcher) {
        try {
            database.withTransaction {
                val nowEpoch = clock.nowEpochMillis()
                subtaskIds.forEachIndexed { index, id ->
                    database.subtaskDao().updateSubtaskPosition(id, index, nowEpoch)
                }
                AppResult.success(Unit)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to reorder subtasks for task $taskId", e))
        }
    }
}
