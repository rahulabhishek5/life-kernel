package com.lifekernel.app.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class TagRepositoryIntegrationTest {

    private lateinit var database: LifeKernelDatabase
    private lateinit var clock: FixedClock
    private lateinit var repository: TagRepositoryImpl
    private val testInstant = Instant.parse("2026-10-25T12:00:00Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = LifeKernelDatabase.createInMemory(context)
        clock = FixedClock(testInstant)
        repository = TagRepositoryImpl(database, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createTag_caseInsensitiveReuse() = runBlocking {
        val firstResult = repository.createTag("Focus")
        assertTrue(firstResult.isSuccess)
        val tag1 = firstResult.getOrNull()!!
        assertEquals("focus", tag1.name)

        val secondResult = repository.createTag("focus")
        assertTrue(secondResult.isSuccess)
        val tag2 = secondResult.getOrNull()!!
        assertEquals(tag1.id, tag2.id)

        val allTags = repository.observeAllTags().first()
        assertEquals(1, allTags.size)
    }

    @Test
    fun deleteOrphanTags_removesUnusedTagsOnly() = runBlocking {
        val tag1 = repository.createTag("ActiveTag").getOrNull()!!
        val tag2 = repository.createTag("OrphanTag").getOrNull()!!

        // Associate tag1 with a task
        val taskId = database.taskDao().insertTask(
            TaskEntity(
                title = "Linked task",
                createdAt = testInstant.toEpochMilli(),
                updatedAt = testInstant.toEpochMilli()
            )
        )
        database.taskTagDao().insertTaskTag(TaskTagEntity(taskId = taskId, tagId = tag1.id))

        val deletedCountResult = repository.deleteOrphanTags()
        assertTrue(deletedCountResult.isSuccess)
        assertEquals(1, deletedCountResult.getOrNull())

        assertNotNull(repository.getTagById(tag1.id))
        assertNull(repository.getTagById(tag2.id))
    }

    @Test
    fun syncTaskTags_addsTagsAndEnforcesLowercaseUniqueness() = runBlocking {
        val taskId = database.taskDao().insertTask(
            TaskEntity(
                title = "Tagged task",
                createdAt = testInstant.toEpochMilli(),
                updatedAt = testInstant.toEpochMilli()
            )
        )

        val syncResult = repository.syncTaskTags(taskId, listOf("Work", "work", "  URGENT  "))
        assertTrue(syncResult.isSuccess)
        val tags = syncResult.getOrNull()!!

        // "Work" and "work" deduplicated to single "work" tag
        assertEquals(2, tags.size)
        assertEquals("urgent", tags[0].name)
        assertEquals("work", tags[1].name)

        val tagsForTask = repository.getTagsForTask(taskId)
        assertEquals(2, tagsForTask.size)

        // Verify SQLite tags table has exactly 2 rows
        val allTags = repository.observeAllTags().first()
        assertEquals(2, allTags.size)
    }

    @Test
    fun syncTaskTags_removingTagPrunesOrphanTagAutomatically() = runBlocking {
        val taskId = database.taskDao().insertTask(
            TaskEntity(
                title = "Task for orphan test",
                createdAt = testInstant.toEpochMilli(),
                updatedAt = testInstant.toEpochMilli()
            )
        )

        // Attach two tags
        repository.syncTaskTags(taskId, listOf("keep-tag", "remove-tag"))
        assertEquals(2, repository.observeAllTags().first().size)

        // Remove "remove-tag"
        val updateResult = repository.syncTaskTags(taskId, listOf("keep-tag"))
        assertTrue(updateResult.isSuccess)

        val remainingTags = repository.getTagsForTask(taskId)
        assertEquals(1, remainingTags.size)
        assertEquals("keep-tag", remainingTags[0].name)

        // "remove-tag" must be pruned from SQLite tags table automatically
        assertNull(repository.getTagByName("remove-tag"))
        assertNotNull(repository.getTagByName("keep-tag"))
        assertEquals(1, repository.observeAllTags().first().size)
    }

    @Test
    fun deleteTask_prunesOrphanTagsViaTaskRepository() = runBlocking {
        val taskRepo = TaskRepositoryImpl(database, clock)
        val taskId = database.taskDao().insertTask(
            TaskEntity(
                title = "Task to be deleted",
                createdAt = testInstant.toEpochMilli(),
                updatedAt = testInstant.toEpochMilli()
            )
        )

        repository.syncTaskTags(taskId, listOf("transient-tag"))
        assertNotNull(repository.getTagByName("transient-tag"))

        // Delete the task via TaskRepository
        val deleteResult = taskRepo.deleteTask(taskId)
        assertTrue(deleteResult.isSuccess)

        // transient-tag had 0 other associations, so it must be pruned from tags
        assertNull(repository.getTagByName("transient-tag"))
        assertEquals(0, repository.observeAllTags().first().size)
    }
}
