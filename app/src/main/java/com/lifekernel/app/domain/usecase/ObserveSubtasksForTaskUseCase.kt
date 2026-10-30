package com.lifekernel.app.domain.usecase

import com.lifekernel.app.domain.model.Subtask
import com.lifekernel.app.domain.repository.SubtaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe subtasks for a parent task ordered by position ASC per PRD §8.3.
 */
class ObserveSubtasksForTaskUseCase(
    private val subtaskRepository: SubtaskRepository
) {
    operator fun invoke(taskId: Long): Flow<List<Subtask>> {
        return subtaskRepository.observeSubtasksForTask(taskId)
    }
}
