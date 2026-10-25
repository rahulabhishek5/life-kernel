package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.repository.TaskRepository

/**
 * Use case to delete a task per DATABASE_SCHEMA.md §6.2 & §9.
 */
class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {

    suspend operator fun invoke(taskId: Long): AppResult<Unit> {
        return taskRepository.deleteTask(taskId)
    }
}
