package com.lifekernel.app.data.repository

import androidx.room.withTransaction
import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity
import com.lifekernel.app.data.repository.mappers.toDomain
import com.lifekernel.app.data.repository.mappers.toEntity
import com.lifekernel.app.domain.model.RecurrenceType
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

/**
 * Room-backed implementation of [TaskRepository] per ARCHITECTURE.md §5 & §6 and DATABASE_SCHEMA.md §9.
 *
 * Invariant: All mutations spanning multiple tables run in atomic Room transactions.
 * All operations execute off the main thread on [dispatcher].
 */
class TaskRepositoryImpl(
    private val database: LifeKernelDatabase,
    private val clock: Clock,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository {

    override fun observeOpenTasksDueOnOrBefore(date: LocalDate): Flow<List<Task>> {
        val dateStr = date.toString()
        return database.taskDao().observeOpenTasksDueOnOrBefore(dateStr)
            .map { entities ->
                entities.map { entity ->
                    val tags = database.taskTagDao().getTagsForTask(entity.id).map { it.toDomain() }
                    val subtasks = database.taskDao().getSubtasks(entity.id).map { it.toDomain() }
                    entity.toDomain(tags = tags, subtasks = subtasks)
                }
            }
            .flowOn(dispatcher)
    }

    override fun observeOpenTasksForDate(date: LocalDate): Flow<List<Task>> {
        val dateStr = date.toString()
        return database.taskDao().observeOpenTasksForDate(dateStr)
            .map { entities ->
                entities.map { entity ->
                    val tags = database.taskTagDao().getTagsForTask(entity.id).map { it.toDomain() }
                    val subtasks = database.taskDao().getSubtasks(entity.id).map { it.toDomain() }
                    entity.toDomain(tags = tags, subtasks = subtasks)
                }
            }
            .flowOn(dispatcher)
    }

    override fun observeOpenTasksWithoutDueDate(): Flow<List<Task>> {
        return database.taskDao().observeOpenTasksWithoutDueDate()
            .map { entities ->
                entities.map { entity ->
                    val tags = database.taskTagDao().getTagsForTask(entity.id).map { it.toDomain() }
                    val subtasks = database.taskDao().getSubtasks(entity.id).map { it.toDomain() }
                    entity.toDomain(tags = tags, subtasks = subtasks)
                }
            }
            .flowOn(dispatcher)
    }

    override fun observeCompletedTasksForDate(date: LocalDate): Flow<List<Task>> {
        val dateStr = date.toString()
        return database.taskDao().observeCompletedTasksForDate(dateStr)
            .map { entities ->
                entities.map { entity ->
                    val tags = database.taskTagDao().getTagsForTask(entity.id).map { it.toDomain() }
                    val subtasks = database.taskDao().getSubtasks(entity.id).map { it.toDomain() }
                    entity.toDomain(tags = tags, subtasks = subtasks)
                }
            }
            .flowOn(dispatcher)
    }

    override fun observeCompletedCountForDate(date: LocalDate): Flow<Int> {
        val dateStr = date.toString()
        return database.taskCompletionDao().observeCompletedCountForDate(dateStr)
            .flowOn(dispatcher)
    }

    override fun observeAllTasks(): Flow<List<Task>> {
        return database.taskDao().observeAllTopLevelTasks()
            .map { entities ->
                entities.map { entity ->
                    val tags = database.taskTagDao().getTagsForTask(entity.id).map { it.toDomain() }
                    val subtasks = database.taskDao().getSubtasks(entity.id).map { it.toDomain() }
                    entity.toDomain(tags = tags, subtasks = subtasks)
                }
            }
            .flowOn(dispatcher)
    }

    override suspend fun getAllTasks(): List<Task> = withContext(dispatcher) {
        database.taskDao().getAllTopLevelTasks().map { entity ->
            val tags = database.taskTagDao().getTagsForTask(entity.id).map { it.toDomain() }
            val subtasks = database.taskDao().getSubtasks(entity.id).map { it.toDomain() }
            entity.toDomain(tags = tags, subtasks = subtasks)
        }
    }

    override fun observeTaskById(id: Long): Flow<Task?> {
        return database.taskDao().observeTaskById(id)
            .map { entity ->
                if (entity == null) {
                    null
                } else {
                    val tags = database.taskTagDao().getTagsForTask(id).map { it.toDomain() }
                    val subtasks = database.taskDao().getSubtasks(id).map { it.toDomain() }
                    entity.toDomain(tags = tags, subtasks = subtasks)
                }
            }
            .flowOn(dispatcher)
    }

    override fun observeSubtasks(parentId: Long): Flow<List<Task>> {
        return database.taskDao().observeSubtasks(parentId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)
    }

    override suspend fun getTaskById(id: Long): Task? = withContext(dispatcher) {
        val entity = database.taskDao().getTaskById(id) ?: return@withContext null
        val tags = database.taskTagDao().getTagsForTask(id).map { it.toDomain() }
        val subtasks = database.taskDao().getSubtasks(id).map { it.toDomain() }
        entity.toDomain(tags = tags, subtasks = subtasks)
    }

    override suspend fun createTask(
        task: Task,
        tagIds: List<Long>
    ): AppResult<Long> = withContext(dispatcher) {
        try {
            database.withTransaction {
                val entity = task.toEntity()
                val newId = database.taskDao().insertTask(entity)
                if (tagIds.isNotEmpty()) {
                    val taskTags = tagIds.map { tagId ->
                        TaskTagEntity(taskId = newId, tagId = tagId)
                    }
                    database.taskTagDao().insertTaskTags(taskTags)
                }
                AppResult.success(newId)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to create task", e))
        }
    }

    override suspend fun updateTask(
        task: Task,
        tagIds: List<Long>?
    ): AppResult<Unit> = withContext(dispatcher) {
        try {
            database.withTransaction {
                val existing = database.taskDao().getTaskById(task.id)
                    ?: return@withTransaction AppResult.failure(
                        AppError.NotFound("Task not found with ID: ${task.id}")
                    )

                val nowEpoch = clock.nowEpochMillis()
                val entity = task.toEntity().copy(
                    createdAt = existing.createdAt,
                    status = existing.status,
                    updatedAt = nowEpoch
                )
                database.taskDao().updateTask(entity)

                if (tagIds != null) {
                    database.taskTagDao().deleteTagsForTask(task.id)
                    if (tagIds.isNotEmpty()) {
                        val taskTags = tagIds.map { tagId ->
                            TaskTagEntity(taskId = task.id, tagId = tagId)
                        }
                        database.taskTagDao().insertTaskTags(taskTags)
                    }
                    database.tagDao().deleteOrphanTags()
                }

                AppResult.success(Unit)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to update task ${task.id}", e))
        }
    }

    override suspend fun deleteTask(taskId: Long): AppResult<Unit> = withContext(dispatcher) {
        try {
            database.withTransaction {
                val deletedCount = database.taskDao().deleteTaskById(taskId)
                if (deletedCount == 0) {
                    return@withTransaction AppResult.failure(
                        AppError.NotFound("Task not found with ID: $taskId")
                    )
                }
                // Cascading delete cleans child completions, subtasks, and task tags automatically.
                // Clean orphaned tags left unused:
                database.tagDao().deleteOrphanTags()
                AppResult.success(Unit)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to delete task $taskId", e))
        }
    }

    override suspend fun completeTask(
        taskId: Long,
        occurrenceDate: LocalDate?
    ): AppResult<Unit> = withContext(dispatcher) {
        try {
            database.withTransaction {
                val task = database.taskDao().getTaskById(taskId)
                    ?: return@withTransaction AppResult.failure(
                        AppError.NotFound("Task not found with ID: $taskId")
                    )

                val isRecurring = task.recurType != RecurrenceType.NONE.code
                if (!isRecurring && task.status == TaskStatus.DONE.code) {
                    return@withTransaction AppResult.failure(
                        AppError.Validation("Task $taskId is already completed")
                    )
                }

                val occurrenceDateStr = occurrenceDate?.toString() ?: task.dueDate
                if (occurrenceDateStr != null) {
                    val existingCompletions = database.taskCompletionDao().getCompletionsForTask(taskId)
                    if (existingCompletions.any { it.occurrenceDate == occurrenceDateStr }) {
                        return@withTransaction AppResult.failure(
                            AppError.Validation("Task $taskId already completed for occurrence $occurrenceDateStr")
                        )
                    }
                }

                val nowEpoch = clock.nowEpochMillis()
                val todayLocal = clock.today().toString()

                val completionEntity = TaskCompletionEntity(
                    taskId = taskId,
                    occurrenceDate = occurrenceDateStr,
                    completedAt = nowEpoch,
                    completedOn = todayLocal
                )
                database.taskCompletionDao().insertCompletion(completionEntity)

                database.taskDao().updateTaskStatus(
                    id = taskId,
                    status = TaskStatus.DONE.code,
                    updatedAt = nowEpoch
                )

                AppResult.success(Unit)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to complete task $taskId", e))
        }
    }

    override suspend fun reopenTask(
        taskId: Long,
        occurrenceDate: LocalDate?
    ): AppResult<Unit> = withContext(dispatcher) {
        try {
            database.withTransaction {
                val task = database.taskDao().getTaskById(taskId)
                    ?: return@withTransaction AppResult.failure(
                        AppError.NotFound("Task not found with ID: $taskId")
                    )

                if (task.status == TaskStatus.OPEN.code) {
                    return@withTransaction AppResult.failure(
                        AppError.Validation("Task $taskId is already open")
                    )
                }

                val occurrenceDateStr = occurrenceDate?.toString() ?: task.dueDate
                if (occurrenceDateStr != null) {
                    val deleted = database.taskCompletionDao().deleteCompletionForOccurrence(taskId, occurrenceDateStr)
                    if (deleted == 0) {
                        database.taskCompletionDao().deleteCompletionForNonRecurring(taskId)
                    }
                } else {
                    database.taskCompletionDao().deleteCompletionForNonRecurring(taskId)
                }

                val nowEpoch = clock.nowEpochMillis()
                database.taskDao().updateTaskStatus(
                    id = taskId,
                    status = TaskStatus.OPEN.code,
                    updatedAt = nowEpoch
                )

                AppResult.success(Unit)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to reopen task $taskId", e))
        }
    }
}
