package com.lifekernel.app.domain.usecase

import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe or fetch tags linked to a specific task per PRD §8.4.
 */
class GetTagsForTaskUseCase(
    private val tagRepository: TagRepository
) {
    fun observe(taskId: Long): Flow<List<Tag>> = tagRepository.observeTagsForTask(taskId)

    suspend operator fun invoke(taskId: Long): List<Tag> = tagRepository.getTagsForTask(taskId)
}
