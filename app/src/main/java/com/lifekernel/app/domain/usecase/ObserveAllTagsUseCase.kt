package com.lifekernel.app.domain.usecase

import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe all tags sorted alphabetically per PRD §8.4.
 */
class ObserveAllTagsUseCase(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): Flow<List<Tag>> = tagRepository.observeAllTags()
}
