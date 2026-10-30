package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.model.TagValidator
import com.lifekernel.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class TagUseCasesTest {

    private lateinit var fakeRepository: FakeTagRepository
    private lateinit var observeAllTagsUseCase: ObserveAllTagsUseCase
    private lateinit var getTagsForTaskUseCase: GetTagsForTaskUseCase
    private lateinit var syncTaskTagsUseCase: SyncTaskTagsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeTagRepository()
        observeAllTagsUseCase = ObserveAllTagsUseCase(fakeRepository)
        getTagsForTaskUseCase = GetTagsForTaskUseCase(fakeRepository)
        syncTaskTagsUseCase = SyncTaskTagsUseCase(fakeRepository)
    }

    @Test
    fun observeAllTags_emitsSortedAlphabetically() = runBlocking {
        fakeRepository.createTag("work")
        fakeRepository.createTag("alpha")
        fakeRepository.createTag("beta")

        val tags = observeAllTagsUseCase().first()
        assertEquals(3, tags.size)
        assertEquals("alpha", tags[0].name)
        assertEquals("beta", tags[1].name)
        assertEquals("work", tags[2].name)
    }

    @Test
    fun syncTaskTags_createsNewTagsAndAssociates() = runBlocking {
        val taskId = 100L
        val result = syncTaskTagsUseCase(taskId, listOf("Urgent", "Personal"))

        assertTrue(result.isSuccess)
        val tags = result.getOrNull()!!
        assertEquals(2, tags.size)
        assertEquals("personal", tags[0].name)
        assertEquals("urgent", tags[1].name)

        val taskTags = getTagsForTaskUseCase(taskId)
        assertEquals(2, taskTags.size)
    }

    @Test
    fun syncTaskTags_unlinksRemovedTagsAndCleansOrphans() = runBlocking {
        val taskId = 101L
        // Initially attach two tags
        syncTaskTagsUseCase(taskId, listOf("work", "urgent"))
        assertEquals(2, fakeRepository.allTagsCount())

        // Now update to only contain "work"
        val updatedResult = syncTaskTagsUseCase(taskId, listOf("work"))
        assertTrue(updatedResult.isSuccess)

        val remainingTags = getTagsForTaskUseCase(taskId)
        assertEquals(1, remainingTags.size)
        assertEquals("work", remainingTags[0].name)

        // Orphan "urgent" must be pruned from repository
        assertEquals(1, fakeRepository.allTagsCount())
        assertNull(fakeRepository.getTagByName("urgent"))
        assertNotNull(fakeRepository.getTagByName("work"))
    }

    @Test
    fun syncTaskTags_sharedTagNotPrunedIfUsedByAnotherTask() = runBlocking {
        val task1 = 201L
        val task2 = 202L

        // Both tasks share tag "shared"
        syncTaskTagsUseCase(task1, listOf("shared", "task1-only"))
        syncTaskTagsUseCase(task2, listOf("shared", "task2-only"))

        assertEquals(3, fakeRepository.allTagsCount())

        // Remove "shared" from task1
        val result1 = syncTaskTagsUseCase(task1, listOf("task1-only"))
        assertTrue(result1.isSuccess)

        // "shared" still used by task2, so it must NOT be pruned
        assertEquals(3, fakeRepository.allTagsCount())
        assertNotNull(fakeRepository.getTagByName("shared"))

        // Now remove "shared" from task2
        val result2 = syncTaskTagsUseCase(task2, listOf("task2-only"))
        assertTrue(result2.isSuccess)

        // Now "shared" has 0 associations, so it must be pruned
        assertEquals(2, fakeRepository.allTagsCount())
        assertNull(fakeRepository.getTagByName("shared"))
    }

    @Test
    fun syncTaskTags_invalidTagName_rejectsAndLeavesDataUnchanged() = runBlocking {
        val taskId = 301L
        syncTaskTagsUseCase(taskId, listOf("valid-tag"))

        val invalidResult = syncTaskTagsUseCase(taskId, listOf("invalid tag with spaces"))
        assertTrue(invalidResult.isFailure)
        assertTrue(invalidResult.errorOrNull() is AppError.Validation)

        val tags = getTagsForTaskUseCase(taskId)
        assertEquals(1, tags.size)
        assertEquals("valid-tag", tags[0].name)
    }

    private class FakeTagRepository : TagRepository {
        private var nextId = 1L
        private val tagsMap = mutableMapOf<Long, Tag>()
        private val taskTagsMap = mutableMapOf<Long, MutableSet<Long>>()
        private val tagsStateFlow = MutableStateFlow<List<Tag>>(emptyList())

        private fun notifyState() {
            tagsStateFlow.value = tagsMap.values.sortedBy { it.name.lowercase() }
        }

        fun allTagsCount(): Int = tagsMap.size

        override fun observeAllTags(): Flow<List<Tag>> = tagsStateFlow.asStateFlow()

        override fun observeTagsForTask(taskId: Long): Flow<List<Tag>> {
            return tagsStateFlow.map {
                val tagIds = taskTagsMap[taskId] ?: emptySet()
                tagIds.mapNotNull { tagsMap[it] }.sortedBy { it.name.lowercase() }
            }
        }

        override suspend fun getTagsForTask(taskId: Long): List<Tag> {
            val tagIds = taskTagsMap[taskId] ?: emptySet()
            return tagIds.mapNotNull { tagsMap[it] }.sortedBy { it.name.lowercase() }
        }

        override suspend fun getTagById(id: Long): Tag? = tagsMap[id]

        override suspend fun getTagByName(name: String): Tag? {
            val normalized = name.trim().lowercase()
            return tagsMap.values.find { it.name.equals(normalized, ignoreCase = true) }
        }

        override suspend fun createTag(name: String): AppResult<Tag> {
            val validation = TagValidator.validateTagName(name)
            if (validation.isFailure) return AppResult.failure(validation.errorOrNull()!!)
            val normalized = validation.getOrNull()!!

            val existing = getTagByName(normalized)
            if (existing != null) return AppResult.success(existing)

            val newTag = Tag(id = nextId++, name = normalized, createdAt = Instant.EPOCH)
            tagsMap[newTag.id] = newTag
            notifyState()
            return AppResult.success(newTag)
        }

        override suspend fun deleteTag(id: Long): AppResult<Unit> {
            val removed = tagsMap.remove(id)
            return if (removed != null) {
                taskTagsMap.values.forEach { it.remove(id) }
                notifyState()
                AppResult.success(Unit)
            } else {
                AppResult.failure(AppError.NotFound("Tag not found: $id"))
            }
        }

        override suspend fun deleteOrphanTags(): AppResult<Int> {
            val usedTagIds = taskTagsMap.values.flatten().toSet()
            val orphanIds = tagsMap.keys.filter { it !in usedTagIds }
            orphanIds.forEach { tagsMap.remove(it) }
            if (orphanIds.isNotEmpty()) notifyState()
            return AppResult.success(orphanIds.size)
        }

        override suspend fun syncTaskTags(
            taskId: Long,
            tagNames: List<String>
        ): AppResult<List<Tag>> {
            val validated = mutableListOf<String>()
            for (name in tagNames) {
                val result = TagValidator.validateTagName(name)
                if (result.isFailure) return AppResult.failure(result.errorOrNull()!!)
                val norm = result.getOrNull()!!
                if (norm !in validated) validated.add(norm)
            }

            val tagIds = mutableSetOf<Long>()
            for (norm in validated) {
                var tag = getTagByName(norm)
                if (tag == null) {
                    val createResult = createTag(norm)
                    tag = createResult.getOrNull()!!
                }
                tagIds.add(tag.id)
            }

            taskTagsMap[taskId] = tagIds
            deleteOrphanTags()
            notifyState()
            return AppResult.success(getTagsForTask(taskId))
        }
    }
}
