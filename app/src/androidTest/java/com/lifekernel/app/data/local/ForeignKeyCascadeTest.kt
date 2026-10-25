package com.lifekernel.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Foreign key enforcement and cascade verification test resolving DB-V1 per DATABASE_SCHEMA.md §15.
 */
@RunWith(AndroidJUnit4::class)
class ForeignKeyCascadeTest {

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
    fun foreignKey_rejectsInvalidParentId() {
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                taskDao.insertTask(
                    TaskEntity(
                        title = "Invalid Subtask",
                        parentId = 999999L, // Does not exist
                        createdAt = 1000L,
                        updatedAt = 1000L
                    )
                )
            }
        }
    }

    @Test
    fun foreignKey_rejectsInvalidTaskIdOnCompletion() {
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                taskCompletionDao.insertCompletion(
                    TaskCompletionEntity(
                        taskId = 999999L, // Does not exist
                        occurrenceDate = "2026-10-25",
                        completedAt = 1790000000000L,
                        completedOn = "2026-10-25"
                    )
                )
            }
        }
    }

    @Test
    fun deletingTask_cascadesToSubtasksCompletionsAndTags() = runBlocking {
        // 1. Insert parent task
        val parentId = taskDao.insertTask(
            TaskEntity(
                title = "Parent Task",
                dueDate = "2026-10-25",
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        // 2. Insert subtask
        val subtaskId = taskDao.insertTask(
            TaskEntity(
                title = "Subtask",
                parentId = parentId,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        // 3. Insert task completion
        val completionId = taskCompletionDao.insertCompletion(
            TaskCompletionEntity(
                taskId = parentId,
                occurrenceDate = "2026-10-25",
                completedAt = 1790000000000L,
                completedOn = "2026-10-25"
            )
        )

        // 4. Insert tag and link
        val tagId = tagDao.insertTag(TagEntity(name = "Urgent", createdAt = 1000L))
        taskTagDao.insertTaskTag(TaskTagEntity(taskId = parentId, tagId = tagId))

        // Verify all exist prior to deletion
        assertNotNull(taskDao.getTaskById(parentId))
        assertNotNull(taskDao.getTaskById(subtaskId))
        assertEquals(1, taskCompletionDao.getCompletionsForTask(parentId).size)
        assertEquals(1, taskTagDao.getTagsForTask(parentId).size)

        // 5. Delete parent task
        taskDao.deleteTaskById(parentId)

        // 6. Verify cascading deletes
        assertNull("Parent task must be deleted", taskDao.getTaskById(parentId))
        assertNull("Subtask must be cascade deleted", taskDao.getTaskById(subtaskId))
        assertEquals(
            "Task completion must be cascade deleted",
            0,
            taskCompletionDao.getCompletionsForTask(parentId).size
        )
        assertEquals(
            "Task tag join must be cascade deleted",
            0,
            taskTagDao.getTagsForTask(parentId).size
        )

        // Tag itself must remain untouched
        assertNotNull("Tag itself must not be deleted when task is deleted", tagDao.getTagById(tagId))
    }

    @Test
    fun deletingTag_cascadesToTaskTagsWithoutDeletingTask() = runBlocking {
        val taskId = taskDao.insertTask(
            TaskEntity(title = "Independent Task", createdAt = 1000L, updatedAt = 1000L)
        )
        val tagId = tagDao.insertTag(TagEntity(name = "Temporary", createdAt = 1000L))
        taskTagDao.insertTaskTag(TaskTagEntity(taskId = taskId, tagId = tagId))

        assertEquals(1, taskTagDao.getTagsForTask(taskId).size)

        // Delete tag
        tagDao.deleteTagById(tagId)

        // Tag join must be removed, task must remain
        assertNull(tagDao.getTagById(tagId))
        assertEquals(0, taskTagDao.getTagsForTask(taskId).size)
        assertNotNull(taskDao.getTaskById(taskId))
    }
}
