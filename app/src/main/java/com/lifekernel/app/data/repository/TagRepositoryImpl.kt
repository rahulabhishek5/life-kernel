package com.lifekernel.app.data.repository

import androidx.room.withTransaction
import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity
import com.lifekernel.app.data.repository.mappers.toDomain
import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.model.TaskValidator
import com.lifekernel.app.domain.repository.TagRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Room-backed implementation of [TagRepository] per ARCHITECTURE.md §5 & §6.
 */
class TagRepositoryImpl(
    private val database: LifeKernelDatabase,
    private val clock: Clock,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TagRepository {

    override fun observeAllTags(): Flow<List<Tag>> {
        return database.tagDao().observeAllTags()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)
    }

    override suspend fun getTagById(id: Long): Tag? = withContext(dispatcher) {
        database.tagDao().getTagById(id)?.toDomain()
    }

    override suspend fun getTagByName(name: String): Tag? = withContext(dispatcher) {
        database.tagDao().getTagByName(name.trim())?.toDomain()
    }

    override suspend fun createTag(name: String): AppResult<Tag> = withContext(dispatcher) {
        val validation = TaskValidator.validateTagName(name)
        if (validation.isFailure) {
            return@withContext AppResult.failure(validation.errorOrNull()!!)
        }

        val trimmedName = validation.getOrNull()!!
        val existing = database.tagDao().getTagByName(trimmedName)
        if (existing != null) {
            return@withContext AppResult.success(existing.toDomain())
        }

        try {
            val nowEpoch = clock.nowEpochMillis()
            val entity = TagEntity(name = trimmedName, createdAt = nowEpoch)
            val newId = database.tagDao().insertTag(entity)
            AppResult.success(entity.copy(id = newId).toDomain())
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to create tag: $trimmedName", e))
        }
    }

    override suspend fun deleteTag(id: Long): AppResult<Unit> = withContext(dispatcher) {
        try {
            val deleted = database.tagDao().deleteTagById(id)
            if (deleted == 0) {
                AppResult.failure(AppError.NotFound("Tag not found with ID: $id"))
            } else {
                AppResult.success(Unit)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to delete tag $id", e))
        }
    }

    override suspend fun deleteOrphanTags(): AppResult<Int> = withContext(dispatcher) {
        try {
            val count = database.tagDao().deleteOrphanTags()
            AppResult.success(count)
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to delete orphan tags", e))
        }
    }

    override fun observeTagsForTask(taskId: Long): Flow<List<Tag>> {
        return database.taskTagDao().observeTagsForTask(taskId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)
    }

    override suspend fun getTagsForTask(taskId: Long): List<Tag> = withContext(dispatcher) {
        database.taskTagDao().getTagsForTask(taskId).map { it.toDomain() }
    }

    override suspend fun syncTaskTags(
        taskId: Long,
        tagNames: List<String>
    ): AppResult<List<Tag>> = withContext(dispatcher) {
        val validatedNames = mutableListOf<String>()
        for (name in tagNames) {
            val validation = TaskValidator.validateTagName(name)
            if (validation.isFailure) {
                return@withContext AppResult.failure(validation.errorOrNull()!!)
            }
            val normalized = validation.getOrNull()!!
            if (!validatedNames.contains(normalized)) {
                validatedNames.add(normalized)
            }
        }

        try {
            database.withTransaction {
                val task = database.taskDao().getTaskById(taskId)
                    ?: return@withTransaction AppResult.failure(
                        AppError.NotFound("Task not found with ID: $taskId")
                    )

                val tagIds = mutableListOf<Long>()
                val nowEpoch = clock.nowEpochMillis()
                for (name in validatedNames) {
                    val existing = database.tagDao().getTagByName(name)
                    val tagId = if (existing != null) {
                        existing.id
                    } else {
                        val entity = TagEntity(name = name, createdAt = nowEpoch)
                        database.tagDao().insertTag(entity)
                    }
                    tagIds.add(tagId)
                }

                database.taskTagDao().deleteTagsForTask(taskId)
                if (tagIds.isNotEmpty()) {
                    val taskTags = tagIds.map { tagId ->
                        TaskTagEntity(taskId = taskId, tagId = tagId)
                    }
                    database.taskTagDao().insertTaskTags(taskTags)
                }

                database.tagDao().deleteOrphanTags()

                val updatedTags = database.taskTagDao().getTagsForTask(taskId).map { it.toDomain() }
                AppResult.success(updatedTags)
            }
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to sync tags for task $taskId", e))
        }
    }
}
