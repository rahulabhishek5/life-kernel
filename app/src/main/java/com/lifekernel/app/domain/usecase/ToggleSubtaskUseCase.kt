package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.repository.SubtaskRepository

/**
 * Use case to toggle the completion state of a subtask per PRD §8.3 & FR-TASK-002.
 * Invariant: Independent of parent task completion state (AC-TASK).
 */
class ToggleSubtaskUseCase(
    private val subtaskRepository: SubtaskRepository
) {
    suspend operator fun invoke(subtaskId: Long): AppResult<Unit> {
        return subtaskRepository.toggleSubtask(subtaskId)
    }
}
