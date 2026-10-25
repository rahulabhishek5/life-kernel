package com.lifekernel.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.data.local.dao.TagDao
import com.lifekernel.app.data.local.dao.TaskCompletionDao
import com.lifekernel.app.data.local.dao.TaskDao
import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import com.lifekernel.app.data.local.entity.TaskEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UniqueConstraintTest {

    private lateinit var database: LifeKernelDatabase
    private lateinit var tagDao: TagDao
    private lateinit var taskDao: TaskDao
    private lateinit var taskCompletionDao: TaskCompletionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = LifeKernelDatabase.createInMemory(context)
        tagDao = database.tagDao()
        taskDao = database.taskDao()
        taskCompletionDao = database.taskCompletionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun tagName_caseInsensitiveDuplicate_throwsConstraintException() {
        runBlocking {
            tagDao.insertTag(TagEntity(name = "Health", createdAt = 1000L))
        }

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                // Different case "health" must collide due to COLLATE NOCASE unique index
                tagDao.insertTag(TagEntity(name = "health", createdAt = 2000L))
            }
        }

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                tagDao.insertTag(TagEntity(name = "HEALTH", createdAt = 3000L))
            }
        }
    }

    @Test
    fun taskCompletion_duplicateOccurrenceDate_throwsConstraintException() {
        val taskId = runBlocking {
            taskDao.insertTask(
                TaskEntity(
                    title = "Daily Exercise",
                    dueDate = "2026-10-25",
                    createdAt = 1000L,
                    updatedAt = 1000L
                )
            )
        }

        runBlocking {
            taskCompletionDao.insertCompletion(
                TaskCompletionEntity(
                    taskId = taskId,
                    occurrenceDate = "2026-10-25",
                    completedAt = 1000L,
                    completedOn = "2026-10-25"
                )
            )
        }

        // Inserting second completion for same task on the same occurrence date must throw
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                taskCompletionDao.insertCompletion(
                    TaskCompletionEntity(
                        taskId = taskId,
                        occurrenceDate = "2026-10-25",
                        completedAt = 2000L,
                        completedOn = "2026-10-25"
                    )
                )
            }
        }
    }

    @Test
    fun taskCompletion_differentOccurrenceDates_succeeds() = runBlocking {
        val taskId = taskDao.insertTask(
            TaskEntity(
                title = "Recurring Habit Task",
                dueDate = "2026-10-25",
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        val id1 = taskCompletionDao.insertCompletion(
            TaskCompletionEntity(
                taskId = taskId,
                occurrenceDate = "2026-10-24",
                completedAt = 1000L,
                completedOn = "2026-10-24"
            )
        )
        val id2 = taskCompletionDao.insertCompletion(
            TaskCompletionEntity(
                taskId = taskId,
                occurrenceDate = "2026-10-25",
                completedAt = 2000L,
                completedOn = "2026-10-25"
            )
        )

        assertEquals(2, taskCompletionDao.getCompletionsForTask(taskId).size)
    }
}
