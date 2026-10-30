package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe or fetch a single task by its unique ID per ARCHITECTURE.md §3 & §6.
 *
 * Invariant: Plain Kotlin and domain models only. No android.* or room dependencies.
 */
class GetTaskByIdUseCase(
    private val taskRepository: TaskRepository
) {

    /**
     * Observes a reactive stream of the task with [id], emitting null if the task does not exist.
     */
    operator fun invoke(id: Long): Flow<Task?> {
        return taskRepository.observeTaskById(id)
    }

    /**
     * Fetches a single task by [id] synchronously as an [AppResult].
     */
    suspend fun get(id: Long): AppResult<Task> {
        val task = taskRepository.getTaskById(id)
        return if (task != null) {
            AppResult.success(task)
        } else {
            AppResult.failure(AppError.NotFound("Task not found with ID: $id"))
        }
    }
}
