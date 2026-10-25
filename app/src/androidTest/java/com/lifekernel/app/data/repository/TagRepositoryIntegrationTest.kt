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
        assertEquals("Focus", tag1.name)

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
}
