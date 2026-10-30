package com.lifekernel.app.domain.repository

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Tag aggregate root per ARCHITECTURE.md §5 and §6.
 *
 * Invariant: Returns domain models only; never exposes Room entities, DAOs, or database cursors.
 */
interface TagRepository {

    /**
     * Observes all tags ordered alphabetically by name (case-insensitive).
     */
    fun observeAllTags(): Flow<List<Tag>>

    /**
     * Fetches a tag by its unique [id], returning null if not found.
     */
    suspend fun getTagById(id: Long): Tag?

    /**
     * Fetches a tag by its [name] (case-insensitive), returning null if not found.
     */
    suspend fun getTagByName(name: String): Tag?

    /**
     * Creates a new tag with the given [name], or returns the existing tag if one already exists.
     */
    suspend fun createTag(name: String): AppResult<Tag>

    /**
     * Deletes a tag by its [id].
     */
    suspend fun deleteTag(id: Long): AppResult<Unit>

    /**
     * Deletes all tags that are not associated with any task, note, or resource.
     * Returns the count of deleted orphan tags.
     */
    suspend fun deleteOrphanTags(): AppResult<Int>

    /**
     * Observes tags linked to [taskId] ordered alphabetically by name.
     */
    fun observeTagsForTask(taskId: Long): Flow<List<Tag>>

    /**
     * Fetches tags linked to [taskId] ordered alphabetically by name.
     */
    suspend fun getTagsForTask(taskId: Long): List<Tag>

    /**
     * Atomically synchronizes the given list of [tagNames] with [taskId].
     * Normalizes names to lowercase, creates any non-existent tags, links them in task_tags,
     * unlinks removed tags, and prunes any orphan tags in a single atomic transaction.
     * Returns the updated list of tags on success.
     */
    suspend fun syncTaskTags(taskId: Long, tagNames: List<String>): AppResult<List<Tag>>
}
