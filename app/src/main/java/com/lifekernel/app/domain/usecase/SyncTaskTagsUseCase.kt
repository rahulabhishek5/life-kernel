package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.repository.TagRepository

/**
 * Use case to atomically synchronize task tags, automatically creating non-existent tags,
 * updating junction associations, and pruning orphan tags per PRD §8.4 and AC-TAG.
 */
class SyncTaskTagsUseCase(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(
        taskId: Long,
        tagNames: List<String>
    ): AppResult<List<Tag>> = tagRepository.syncTaskTags(taskId, tagNames)
}
