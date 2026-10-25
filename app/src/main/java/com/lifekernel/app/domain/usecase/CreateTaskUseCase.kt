package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskValidator
import com.lifekernel.app.domain.repository.TaskRepository

/**
 * Use case to validate and create a new task per DATABASE_SCHEMA.md §2.4 & §6.2.
 */
class CreateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {

    suspend operator fun invoke(
        task: Task,
        tagIds: List<Long> = emptyList()
    ): AppResult<Long> {
        val now = clock.nowInstant()
        val preparedTask = task.copy(
            createdAt = if (task.createdAt == java.time.Instant.EPOCH) now else task.createdAt,
            updatedAt = now
        )

        val validationResult = TaskValidator.validateTask(preparedTask)
        if (validationResult.isFailure) {
            return AppResult.failure(validationResult.errorOrNull()!!)
        }

        return taskRepository.createTask(preparedTask, tagIds)
    }
}
