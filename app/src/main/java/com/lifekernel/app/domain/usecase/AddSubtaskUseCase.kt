package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.model.SubtaskValidator
import com.lifekernel.app.domain.repository.SubtaskRepository

/**
 * Use case to validate and add a subtask to a parent task per PRD §8.3 & FR-TASK-002.
 */
class AddSubtaskUseCase(
    private val subtaskRepository: SubtaskRepository
) {
    suspend operator fun invoke(taskId: Long, title: String): AppResult<Long> {
        val validation = SubtaskValidator.validateTitle(title)
        if (validation.isFailure) {
            return AppResult.failure(validation.errorOrNull()!!)
        }
        return subtaskRepository.addSubtask(taskId, validation.getOrNull()!!)
    }
}
