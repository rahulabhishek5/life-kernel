package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.repository.TaskRepository
import java.time.LocalDate

/**
 * Use case to atomically mark a task as completed per DATABASE_SCHEMA.md §6.3 & §9.
 */
class CompleteTaskUseCase(
    private val taskRepository: TaskRepository
) {

    suspend operator fun invoke(
        taskId: Long,
        occurrenceDate: LocalDate? = null
    ): AppResult<Unit> {
        return taskRepository.completeTask(taskId, occurrenceDate)
    }
}
