package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class UpdateTaskUseCaseTest {

    private lateinit var fakeRepository: FakeTaskRepository
    private lateinit var clock: FixedClock
    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeRepository = FakeTaskRepository()
    }

    @Test
    fun updateTask_validUpdates_refreshesUpdatedAtAndPersists() = runBlocking {
        val originalTask = Task(
            id = 1L,
            title = "Original Title",
            description = "Original Description",
            priority = TaskPriority.LOW,
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[1L] = originalTask

        val updateInstant = Instant.parse("2026-10-25T14:30:00Z")
        clock.setInstant(updateInstant)

        val useCase = UpdateTaskUseCase(fakeRepository, clock)
        val result = useCase(
            originalTask.copy(
                title = "Updated Title",
                description = "Updated Description",
                priority = TaskPriority.HIGH
            )
        )

        assertTrue(result.isSuccess)
        val stored = fakeRepository.getTaskById(1L)
        assertNotNull(stored)
        assertEquals("Updated Title", stored?.title)
        assertEquals("Updated Description", stored?.description)
        assertEquals(TaskPriority.HIGH, stored?.priority)
        assertEquals(updateInstant, stored?.updatedAt)
    }

    @Test
    fun updateTask_blankTitle_isRejectedWithValidationError() = runBlocking {
        val originalTask = Task(
            id = 1L,
            title = "Valid Title",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[1L] = originalTask

        val useCase = UpdateTaskUseCase(fakeRepository, clock)
        val result = useCase(originalTask.copy(title = "   "))

        assertTrue(result.isFailure)
        val error = result.errorOrNull()
        assertTrue(error is AppError.Validation)
        assertEquals("title", (error as AppError.Validation).field)

        // Verify task was not changed
        val stored = fakeRepository.getTaskById(1L)
        assertEquals("Valid Title", stored?.title)
    }

    @Test
    fun updateTask_editingCompletedTask_preservesCompletedStatusAndCompletions() = runBlocking {
        // Critical Invariant (AC-TASK): Editing a completed task must preserve its completed status (status = 1)
        val completedTask = Task(
            id = 42L,
            title = "Old completed title",
            status = TaskStatus.DONE,
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[42L] = completedTask
        fakeRepository.setCompletedCount(testDate, 1)

        val updateInstant = Instant.parse("2026-10-25T16:00:00Z")
        clock.setInstant(updateInstant)

        val useCase = UpdateTaskUseCase(fakeRepository, clock)
        val updateResult = useCase(
            completedTask.copy(
                title = "New updated completed title",
                description = "Added post-completion notes"
            )
        )

        assertTrue(updateResult.isSuccess)
        val updated = fakeRepository.getTaskById(42L)
        assertNotNull(updated)
        assertEquals("New updated completed title", updated?.title)
        assertEquals("Added post-completion notes", updated?.description)
        assertEquals("Status must remain DONE", TaskStatus.DONE, updated?.status)
        assertEquals(updateInstant, updated?.updatedAt)

        // Verify completions remain intact
        val count = fakeRepository.completedCounts[testDate]
        assertEquals(1, count)
    }

    @Test
    fun updateTask_startDateAfterDueDate_isRejectedWithValidationError() = runBlocking {
        val originalTask = Task(
            id = 1L,
            title = "Task with invalid dates",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[1L] = originalTask

        val useCase = UpdateTaskUseCase(fakeRepository, clock)
        val result = useCase(
            originalTask.copy(
                startDate = testDate.plusDays(5),
                dueDate = testDate
            )
        )

        assertTrue(result.isFailure)
        val error = result.errorOrNull()
        assertTrue(error is AppError.Validation)
        assertEquals("startDate", (error as AppError.Validation).field)
    }
}
