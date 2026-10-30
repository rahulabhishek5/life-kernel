package com.lifekernel.app.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.ZoneId

/**
 * On-device SQLite & Repository integration test for subtask operations per PRD §8.3 & DATABASE_SCHEMA.md §6.2.
 *
 * Verifies:
 * - Adding subtasks and reactive Flow position ASC ordering.
 * - Toggling completion state in SQLite (decoupled parent/child state).
 * - Deleting subtasks with position continuity compaction.
 * - Foreign key cascade: deleting parent task cascades to delete all child subtasks.
 */
@RunWith(AndroidJUnit4::class)
class SubtaskRepositoryIntegrationTest {

    private lateinit var database: LifeKernelDatabase
    private lateinit var clock: FixedClock
    private lateinit var subtaskRepository: SubtaskRepositoryImpl
    private lateinit var taskRepository: TaskRepositoryImpl

    private val testInstant = Instant.parse("2026-10-30T10:00:00Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = LifeKernelDatabase.createInMemory(context)
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        subtaskRepository = SubtaskRepositoryImpl(database, clock)
        taskRepository = TaskRepositoryImpl(database, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addSubtasks_storesInSQLiteAndOrdersByPositionAsc() = runBlocking {
        val parentTaskId = taskRepository.createTask(
            Task(
                title = "Parent Task",
                createdAt = testInstant,
                updatedAt = testInstant
            )
        ).getOrNull()!!

        val subtask1Id = subtaskRepository.addSubtask(parentTaskId, "First item").getOrNull()!!
        val subtask2Id = subtaskRepository.addSubtask(parentTaskId, "Second item").getOrNull()!!
        val subtask3Id = subtaskRepository.addSubtask(parentTaskId, "Third item").getOrNull()!!

        val subtasks = subtaskRepository.observeSubtasksForTask(parentTaskId).first()
        assertEquals(3, subtasks.size)
        assertEquals(0, subtasks[0].position)
        assertEquals(subtask1Id, subtasks[0].id)
        assertEquals("First item", subtasks[0].title)

        assertEquals(1, subtasks[1].position)
        assertEquals(subtask2Id, subtasks[1].id)
        assertEquals("Second item", subtasks[1].title)

        assertEquals(2, subtasks[2].position)
        assertEquals(subtask3Id, subtasks[2].id)
        assertEquals("Third item", subtasks[2].title)
    }

    @Test
    fun toggleSubtask_updatesCompletionInSQLiteWithoutAffectingParent() = runBlocking {
        val parentTaskId = taskRepository.createTask(
            Task(
                title = "Project checklist",
                createdAt = testInstant,
                updatedAt = testInstant
            )
        ).getOrNull()!!

        val subtaskId = subtaskRepository.addSubtask(parentTaskId, "Run migrations").getOrNull()!!

        var subtask = subtaskRepository.getSubtaskById(subtaskId)!!
        assertFalse("Subtask initially incomplete", subtask.isCompleted)

        // Toggle to completed
        val toggleResult = subtaskRepository.toggleSubtask(subtaskId)
        assertTrue(toggleResult.isSuccess)

        subtask = subtaskRepository.getSubtaskById(subtaskId)!!
        assertTrue("Subtask marked complete in SQLite", subtask.isCompleted)

        // Verify parent task remains OPEN (independent completion state)
        val parentTask = taskRepository.getTaskById(parentTaskId)!!
        assertEquals("Parent task status must remain OPEN", TaskStatus.OPEN, parentTask.status)
        assertFalse(parentTask.isCompleted)
    }

    @Test
    fun deleteSubtask_removesRowAndCompactsPositionsInSQLite() = runBlocking {
        val parentTaskId = taskRepository.createTask(
            Task(
                title = "Sprint Planning",
                createdAt = testInstant,
                updatedAt = testInstant
            )
        ).getOrNull()!!

        val id1 = subtaskRepository.addSubtask(parentTaskId, "Item 1").getOrNull()!!
        val id2 = subtaskRepository.addSubtask(parentTaskId, "Item 2").getOrNull()!!
        val id3 = subtaskRepository.addSubtask(parentTaskId, "Item 3").getOrNull()!!

        // Delete the middle subtask (Item 2)
        val deleteResult = subtaskRepository.deleteSubtask(id2)
        assertTrue(deleteResult.isSuccess)

        assertNull("Deleted subtask must not exist in SQLite", subtaskRepository.getSubtaskById(id2))

        val remaining = subtaskRepository.getSubtasksForTask(parentTaskId)
        assertEquals(2, remaining.size)
        assertEquals(id1, remaining[0].id)
        assertEquals(0, remaining[0].position)

        assertEquals(id3, remaining[1].id)
        assertEquals(1, remaining[1].position)
    }

    @Test
    fun foreignKeyCascade_deletingParentTaskDeletesAllSubtasksFromSQLite() = runBlocking {
        val parentTaskId = taskRepository.createTask(
            Task(
                title = "Task with cascade subtasks",
                createdAt = testInstant,
                updatedAt = testInstant
            )
        ).getOrNull()!!

        val subtask1Id = subtaskRepository.addSubtask(parentTaskId, "Subtask A").getOrNull()!!
        val subtask2Id = subtaskRepository.addSubtask(parentTaskId, "Subtask B").getOrNull()!!

        // Ensure subtasks exist
        assertNotNull(subtaskRepository.getSubtaskById(subtask1Id))
        assertNotNull(subtaskRepository.getSubtaskById(subtask2Id))

        // Delete parent task
        val deleteResult = taskRepository.deleteTask(parentTaskId)
        assertTrue(deleteResult.isSuccess)

        // Verify foreign key cascade removed both subtasks from SQLite
        assertNull("Parent task must be deleted", taskRepository.getTaskById(parentTaskId))
        assertNull("Subtask A must be cascade deleted", subtaskRepository.getSubtaskById(subtask1Id))
        assertNull("Subtask B must be cascade deleted", subtaskRepository.getSubtaskById(subtask2Id))
        assertEquals(0, subtaskRepository.getSubtasksForTask(parentTaskId).size)
    }
}
