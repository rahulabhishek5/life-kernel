package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.repository.TaskRepository
import java.time.LocalDate

/**
 * Use case to atomically reopen a completed task per DATABASE_SCHEMA.md §6.3 & §9.
 */
class ReopenTaskUseCase(
    private val taskRepository: TaskRepository
) {

    suspend operator fun invoke(
        taskId: Long,
        occurrenceDate: LocalDate? = null
    ): AppResult<Unit> {
        return taskRepository.reopenTask(taskId, occurrenceDate)
    }
}
