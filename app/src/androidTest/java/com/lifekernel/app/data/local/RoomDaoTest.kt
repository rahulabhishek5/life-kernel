package com.lifekernel.app.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.data.local.dao.TagDao
import com.lifekernel.app.data.local.dao.TaskCompletionDao
import com.lifekernel.app.data.local.dao.TaskDao
import com.lifekernel.app.data.local.dao.TaskTagDao
import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDaoTest {

    private lateinit var database: LifeKernelDatabase
    private lateinit var tagDao: TagDao
    private lateinit var taskDao: TaskDao
    private lateinit var taskCompletionDao: TaskCompletionDao
    private lateinit var taskTagDao: TaskTagDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = LifeKernelDatabase.createInMemory(context)
        tagDao = database.tagDao()
        taskDao = database.taskDao()
        taskCompletionDao = database.taskCompletionDao()
        taskTagDao = database.taskTagDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun tagDao_insertAndQuery() = runBlocking {
        val tagId = tagDao.insertTag(TagEntity(name = "Personal", createdAt = 1000L))
        val retrieved = tagDao.getTagById(tagId)

        assertNotNull(retrieved)
        assertEquals("Personal", retrieved?.name)
        assertEquals(1000L, retrieved?.createdAt)

        // Case-insensitive query
        val byLower = tagDao.getTagByName("personal")
        assertNotNull(byLower)
        assertEquals(tagId, byLower?.id)
    }

    @Test
    fun tagDao_deleteOrphanTags() = runBlocking {
        val tag1Id = tagDao.insertTag(TagEntity(name = "Used", createdAt = 1000L))
        val tag2Id = tagDao.insertTag(TagEntity(name = "Orphan", createdAt = 1000L))

        val taskId = taskDao.insertTask(
            TaskEntity(
                title = "Task with tag",
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )
        taskTagDao.insertTaskTag(TaskTagEntity(taskId = taskId, tagId = tag1Id))

        val deletedCount = tagDao.deleteOrphanTags()
        assertEquals(1, deletedCount)

        assertNotNull(tagDao.getTagById(tag1Id))
        assertNull(tagDao.getTagById(tag2Id))
    }

    @Test
    fun taskDao_crudAndSubtasks() = runBlocking {
        val parentId = taskDao.insertTask(
            TaskEntity(
                title = "Parent Task",
                priority = 3,
                dueDate = "2026-10-25",
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        val subtask1Id = taskDao.insertTask(
            TaskEntity(
                title = "Subtask 1",
                parentId = parentId,
                sortOrder = 0,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )
        val subtask2Id = taskDao.insertTask(
            TaskEntity(
                title = "Subtask 2",
                parentId = parentId,
                sortOrder = 1,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        val subtasks = taskDao.getSubtasks(parentId)
        assertEquals(2, subtasks.size)
        assertEquals(subtask1Id, subtasks[0].id)
        assertEquals(subtask2Id, subtasks[1].id)

        // Update task status
        taskDao.updateTaskStatus(parentId, status = 1, updatedAt = 2000L)
        val updatedParent = taskDao.getTaskById(parentId)
        assertEquals(1, updatedParent?.status)
        assertEquals(2000L, updatedParent?.updatedAt)
    }

    @Test
    fun taskDao_observeOpenTasksDueTodayOrOverdue() = runBlocking {
        // Due today
        taskDao.insertTask(
            TaskEntity(
                title = "Due Today",
                dueDate = "2026-10-25",
                priority = 2,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )
        // Overdue
        taskDao.insertTask(
            TaskEntity(
                title = "Overdue",
                dueDate = "2026-10-20",
                priority = 3,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )
        // Future (should not appear)
        taskDao.insertTask(
            TaskEntity(
                title = "Future",
                dueDate = "2026-10-30",
                priority = 1,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )
        // Completed (status = 1, should not appear)
        taskDao.insertTask(
            TaskEntity(
                title = "Completed",
                status = 1,
                dueDate = "2026-10-25",
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        val openDue = taskDao.observeOpenTasksDueOnOrBefore("2026-10-25").first()
        assertEquals(2, openDue.size)
        // Order by due_date ASC, priority DESC
        assertEquals("Overdue", openDue[0].title)
        assertEquals("Due Today", openDue[1].title)
    }

    @Test
    fun taskCompletionDao_crudAndCount() = runBlocking {
        val taskId = taskDao.insertTask(
            TaskEntity(
                title = "Task to complete",
                dueDate = "2026-10-25",
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        val completionId = taskCompletionDao.insertCompletion(
            TaskCompletionEntity(
                taskId = taskId,
                occurrenceDate = "2026-10-25",
                completedAt = 1790000000000L,
                completedOn = "2026-10-25"
            )
        )

        val completions = taskCompletionDao.getCompletionsForTask(taskId)
        assertEquals(1, completions.size)
        assertEquals(completionId, completions[0].id)
        assertEquals("2026-10-25", completions[0].completedOn)

        val count = taskCompletionDao.observeCompletedCountForDate("2026-10-25").first()
        assertEquals(1, count)

        // Delete completion (undo)
        taskCompletionDao.deleteCompletionForOccurrence(taskId, "2026-10-25")
        val countAfterDelete = taskCompletionDao.getCompletedCountForDate("2026-10-25")
        assertEquals(0, countAfterDelete)
    }

    @Test
    fun taskTagDao_joinsAndQueries() = runBlocking {
        val tagAId = tagDao.insertTag(TagEntity(name = "Urgent", createdAt = 1000L))
        val tagBId = tagDao.insertTag(TagEntity(name = "Work", createdAt = 1000L))

        val taskId = taskDao.insertTask(
            TaskEntity(title = "Work Task", createdAt = 1000L, updatedAt = 1000L)
        )

        taskTagDao.insertTaskTag(TaskTagEntity(taskId = taskId, tagId = tagAId))
        taskTagDao.insertTaskTag(TaskTagEntity(taskId = taskId, tagId = tagBId))

        val tags = taskTagDao.getTagsForTask(taskId)
        assertEquals(2, tags.size)
        assertEquals("Urgent", tags[0].name)
        assertEquals("Work", tags[1].name)

        val taskIds = taskTagDao.getTaskIdsForTag(tagAId)
        assertEquals(1, taskIds.size)
        assertEquals(taskId, taskIds[0])
    }
}
