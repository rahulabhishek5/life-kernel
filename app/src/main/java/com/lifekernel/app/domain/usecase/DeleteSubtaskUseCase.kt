package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.repository.SubtaskRepository

/**
 * Use case to delete a subtask while maintaining position continuity per PRD §8.3.
 */
class DeleteSubtaskUseCase(
    private val subtaskRepository: SubtaskRepository
) {
    suspend operator fun invoke(subtaskId: Long): AppResult<Unit> {
        return subtaskRepository.deleteSubtask(subtaskId)
    }
}
