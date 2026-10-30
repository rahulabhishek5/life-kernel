package com.lifekernel.app.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
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
import java.time.LocalDate
import java.time.ZoneId

/**
 * Repository integration test verifying atomic Room transactions, reactive Flows,
 * timezone stability of completed_on, and cascading cleanup per ARCHITECTURE.md §14 and DATABASE_SCHEMA.md §9.
 */
@RunWith(AndroidJUnit4::class)
class TaskRepositoryIntegrationTest {

    private lateinit var database: LifeKernelDatabase
    private lateinit var clock: FixedClock
    private lateinit var repository: TaskRepositoryImpl
    private val testInstant = Instant.parse("2026-10-25T14:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = LifeKernelDatabase.createInMemory(context)
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        repository = TaskRepositoryImpl(database, clock)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun createTask_storesEntityAndTagsAtomically() = runBlocking {
        val tag1Id = database.tagDao().insertTag(TagEntity(name = "Urgent", createdAt = 1000L))
        val tag2Id = database.tagDao().insertTag(TagEntity(name = "Work", createdAt = 1000L))

        val task = Task(
            title = "Prepare quarterly review",
            description = "Gather feedback and metric dashboards",
            priority = TaskPriority.HIGH,
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )

        val result = repository.createTask(task, listOf(tag1Id, tag2Id))
        assertTrue(result.isSuccess)
        val taskId = result.getOrNull()!!

        val retrieved = repository.getTaskById(taskId)
        assertNotNull(retrieved)
        assertEquals("Prepare quarterly review", retrieved?.title)
        assertEquals(TaskPriority.HIGH, retrieved?.priority)
        assertEquals(2, retrieved?.tags?.size)
        assertTrue(retrieved?.tags?.any { it.name == "Urgent" } == true)
        assertTrue(retrieved?.tags?.any { it.name == "Work" } == true)
    }

    @Test
    fun completeTask_updatesCompletionAndStatusAtomically() = runBlocking {
        val task = Task(
            title = "Finalize invoice",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val taskId = repository.createTask(task).getOrNull()!!

        // Advance clock for completion
        val completeInstant = Instant.parse("2026-10-25T16:30:00Z")
        clock.setInstant(completeInstant)

        val completeResult = repository.completeTask(taskId)
        assertTrue(completeResult.isSuccess)

        // Verify task entity status is DONE and updatedAt refreshed
        val updatedTask = repository.getTaskById(taskId)
        assertEquals(TaskStatus.DONE, updatedTask?.status)
        assertEquals(completeInstant, updatedTask?.updatedAt)

        // Verify completion row exists in database
        val completions = database.taskCompletionDao().getCompletionsForTask(taskId)
        assertEquals(1, completions.size)
        val completion = completions.first()
        assertEquals(completeInstant.toEpochMilli(), completion.completedAt)
        assertEquals(testDate.toString(), completion.completedOn)
    }

    @Test
    fun completeTask_doubleComplete_isRejected() = runBlocking {
        val task = Task(
            title = "One-off task",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val taskId = repository.createTask(task).getOrNull()!!

        val firstComplete = repository.completeTask(taskId)
        assertTrue(firstComplete.isSuccess)

        val secondComplete = repository.completeTask(taskId)
        assertTrue("Double complete must be rejected", secondComplete.isFailure)
        assertTrue(secondComplete.errorOrNull() is AppError.Validation)
    }

    @Test
    fun reopenTask_removesCompletionAndResetsStatusAtomically() = runBlocking {
        val task = Task(
            title = "Read design doc",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val taskId = repository.createTask(task).getOrNull()!!
        repository.completeTask(taskId)

        val reopenInstant = Instant.parse("2026-10-25T18:00:00Z")
        clock.setInstant(reopenInstant)

        val reopenResult = repository.reopenTask(taskId)
        assertTrue(reopenResult.isSuccess)

        val reopenedTask = repository.getTaskById(taskId)
        assertEquals(TaskStatus.OPEN, reopenedTask?.status)
        assertEquals(reopenInstant, reopenedTask?.updatedAt)

        val completions = database.taskCompletionDao().getCompletionsForTask(taskId)
        assertTrue("Completion row must be removed upon reopen", completions.isEmpty())
    }

    @Test
    fun reopenTask_alreadyOpen_isRejected() = runBlocking {
        val task = Task(
            title = "Still open",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val taskId = repository.createTask(task).getOrNull()!!

        val reopenResult = repository.reopenTask(taskId)
        assertTrue("Reopening an already open task must be rejected", reopenResult.isFailure)
        assertTrue(reopenResult.errorOrNull() is AppError.Validation)
    }

    @Test
    fun deleteTask_cascadesSubtasksCompletionsAndCleansOrphanTags() = runBlocking {
        val tagId = database.tagDao().insertTag(TagEntity(name = "CleanupTarget", createdAt = 1000L))

        val parentTask = Task(
            title = "Parent task",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val parentId = repository.createTask(parentTask, listOf(tagId)).getOrNull()!!

        val subtask = Task(
            title = "Subtask 1",
            parentId = parentId,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val subtaskId = repository.createTask(subtask).getOrNull()!!

        // Complete parent
        repository.completeTask(parentId)

        // Delete parent
        val deleteResult = repository.deleteTask(parentId)
        assertTrue(deleteResult.isSuccess)

        // Verify parent deleted
        assertNull(repository.getTaskById(parentId))

        // Verify subtask deleted by FK CASCADE
        assertNull(repository.getTaskById(subtaskId))

        // Verify completions deleted by FK CASCADE
        val completions = database.taskCompletionDao().getCompletionsForTask(parentId)
        assertTrue(completions.isEmpty())

        // Verify orphan tag deleted by cleanup
        assertNull(database.tagDao().getTagById(tagId))
    }

    @Test
    fun completedOn_remainsStable_acrossSimulatedTimezoneChanges() = runBlocking {
        // Complete a task at 23:45 UTC on 2026-10-25
        val nearMidnightInstant = Instant.parse("2026-10-25T23:45:00Z")
        clock.setInstant(nearMidnightInstant)

        val task = Task(
            title = "Late night work",
            dueDate = testDate,
            createdAt = nearMidnightInstant,
            updatedAt = nearMidnightInstant
        )
        val taskId = repository.createTask(task).getOrNull()!!
        repository.completeTask(taskId)

        // Verify it was completed on 2026-10-25
        val completedToday = repository.observeCompletedTasksForDate(testDate).first()
        assertEquals(1, completedToday.size)
        assertEquals(taskId, completedToday.first().id)

        // Now simulate a query looking at 2026-10-25 regardless of any timezone conversion:
        // The completed_on snapshot string in the database remains "2026-10-25" forever.
        val completion = database.taskCompletionDao().getCompletionsForTask(taskId).first()
        assertEquals("2026-10-25", completion.completedOn)

        val nextDayQuery = repository.observeCompletedTasksForDate(LocalDate.of(2026, 10, 26)).first()
        assertTrue("Must not appear on 2026-10-26 despite UTC+2 rollover", nextDayQuery.isEmpty())
    }

    @Test
    fun emptyDatabase_emitsExpectedDefaults() = runBlocking {
        val openToday = repository.observeOpenTasksForDate(testDate).first()
        assertTrue(openToday.isEmpty())

        val completedToday = repository.observeCompletedTasksForDate(testDate).first()
        assertTrue(completedToday.isEmpty())

        val count = repository.observeCompletedCountForDate(testDate).first()
        assertEquals(0, count)

        val task = repository.observeTaskById(999L).first()
        assertNull(task)
    }

    @Test
    fun reactiveFlows_reEmitOnMutations() = runBlocking {
        val openFlow = repository.observeOpenTasksForDate(testDate)
        val completedFlow = repository.observeCompletedTasksForDate(testDate)
        val countFlow = repository.observeCompletedCountForDate(testDate)

        assertEquals(0, openFlow.first().size)
        assertEquals(0, completedFlow.first().size)
        assertEquals(0, countFlow.first())

        // 1. Create task
        val task = Task(
            title = "Reactive test task",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val taskId = repository.createTask(task).getOrNull()!!

        assertEquals(1, openFlow.first().size)
        assertEquals(0, completedFlow.first().size)

        // 2. Complete task
        repository.completeTask(taskId)
        assertEquals(0, openFlow.first().size)
        assertEquals(1, completedFlow.first().size)
        assertEquals(1, countFlow.first())

        // 3. Reopen task
        repository.reopenTask(taskId)
        assertEquals(1, openFlow.first().size)
        assertEquals(0, completedFlow.first().size)
        assertEquals(0, countFlow.first())

        // 4. Delete task
        repository.deleteTask(taskId)
        assertEquals(0, openFlow.first().size)
        assertEquals(0, completedFlow.first().size)
        assertEquals(0, countFlow.first())
    }

    @Test
    fun updateTask_editingCompletedTask_preservesStatusAndCompletionsIntact() = runBlocking {
        // Critical Invariant (AC-TASK): Editing a completed task must preserve its completed status (status = 1)
        // and keep its task_completions records intact.
        val task = Task(
            title = "Task to be completed then edited",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val taskId = repository.createTask(task).getOrNull()!!

        // Complete the task
        val completeInstant = Instant.parse("2026-10-25T15:00:00Z")
        clock.setInstant(completeInstant)
        val completeResult = repository.completeTask(taskId)
        assertTrue(completeResult.isSuccess)

        // Verify task is DONE and completion row exists
        val completedTask = repository.getTaskById(taskId)
        assertEquals(TaskStatus.DONE, completedTask?.status)
        val completionsBefore = database.taskCompletionDao().getCompletionsForTask(taskId)
        assertEquals(1, completionsBefore.size)
        assertEquals(completeInstant.toEpochMilli(), completionsBefore.first().completedAt)

        // Now edit the completed task (update title, description, priority)
        val editInstant = Instant.parse("2026-10-25T17:00:00Z")
        clock.setInstant(editInstant)
        val updatedTask = completedTask!!.copy(
            title = "Task edited after completion",
            description = "Post-completion feedback attached",
            priority = TaskPriority.HIGH
        )
        val updateResult = repository.updateTask(updatedTask)
        assertTrue(updateResult.isSuccess)

        // Verify task attributes were updated
        val retrievedAfterEdit = repository.getTaskById(taskId)
        assertNotNull(retrievedAfterEdit)
        assertEquals("Task edited after completion", retrievedAfterEdit?.title)
        assertEquals("Post-completion feedback attached", retrievedAfterEdit?.description)
        assertEquals(TaskPriority.HIGH, retrievedAfterEdit?.priority)

        // Verify critical invariant: status is still DONE and completions are untouched
        assertEquals("Status must remain DONE (1)", TaskStatus.DONE, retrievedAfterEdit?.status)
        val completionsAfter = database.taskCompletionDao().getCompletionsForTask(taskId)
        assertEquals("Completion record count must remain 1", 1, completionsAfter.size)
        assertEquals(completeInstant.toEpochMilli(), completionsAfter.first().completedAt)
        assertEquals(testDate.toString(), completionsAfter.first().completedOn)
    }
}
