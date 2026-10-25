package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskValidator
import com.lifekernel.app.domain.repository.TaskRepository

/**
 * Use case to validate and update an existing task per DATABASE_SCHEMA.md §2.4 & §6.2.
 */
class UpdateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {

    suspend operator fun invoke(
        task: Task,
        tagIds: List<Long>? = null
    ): AppResult<Unit> {
        val preparedTask = task.copy(
            updatedAt = clock.nowInstant()
        )

        val validationResult = TaskValidator.validateTask(preparedTask)
        if (validationResult.isFailure) {
            return AppResult.failure(validationResult.errorOrNull()!!)
        }

        return taskRepository.updateTask(preparedTask, tagIds)
    }
}
