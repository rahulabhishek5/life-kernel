package com.lifekernel.app.domain.model

import com.lifekernel.app.core.result.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class TaskValidatorTest {

    @Test
    fun validateTitle_validTitle_returnsTrimmedTitle() {
        val result = TaskValidator.validateTitle("  Buy groceries  ")
        assertTrue(result.isSuccess)
        assertEquals("Buy groceries", result.getOrNull())
    }

    @Test
    fun validateTitle_emptyOrWhitespace_returnsValidationError() {
        val emptyResult = TaskValidator.validateTitle("")
        assertTrue(emptyResult.isFailure)
        val error = emptyResult.errorOrNull() as AppError.Validation
        assertEquals("title", error.field)

        val whitespaceResult = TaskValidator.validateTitle("   \n\t  ")
        assertTrue(whitespaceResult.isFailure)
    }

    @Test
    fun validateTitle_lengthLimits() {
        val maxTitle = "A".repeat(TaskValidator.MAX_TITLE_LENGTH)
        val validResult = TaskValidator.validateTitle(maxTitle)
        assertTrue(validResult.isSuccess)

        val tooLongTitle = "A".repeat(TaskValidator.MAX_TITLE_LENGTH + 1)
        val invalidResult = TaskValidator.validateTitle(tooLongTitle)
        assertTrue(invalidResult.isFailure)
        val error = invalidResult.errorOrNull() as AppError.Validation
        assertEquals("title", error.field)
    }

    @Test
    fun validateDescription_nullOrBlank_returnsNull() {
        val nullResult = TaskValidator.validateDescription(null)
        assertTrue(nullResult.isSuccess)
        assertNull(nullResult.getOrNull())

        val blankResult = TaskValidator.validateDescription("   ")
        assertTrue(blankResult.isSuccess)
        assertNull(blankResult.getOrNull())
    }

    @Test
    fun validateDescription_lengthLimits() {
        val validDesc = "D".repeat(TaskValidator.MAX_DESCRIPTION_LENGTH)
        val validResult = TaskValidator.validateDescription(validDesc)
        assertTrue(validResult.isSuccess)
        assertEquals(validDesc, validResult.getOrNull())

        val invalidDesc = "D".repeat(TaskValidator.MAX_DESCRIPTION_LENGTH + 1)
        val invalidResult = TaskValidator.validateDescription(invalidDesc)
        assertTrue(invalidResult.isFailure)
        val error = invalidResult.errorOrNull() as AppError.Validation
        assertEquals("description", error.field)
    }

    @Test
    fun validateTagName_validLimitsAndTrimming() {
        val validResult = TaskValidator.validateTagName("  work  ")
        assertTrue(validResult.isSuccess)
        assertEquals("work", validResult.getOrNull())

        val emptyResult = TaskValidator.validateTagName("  ")
        assertTrue(emptyResult.isFailure)

        val maxTag = "T".repeat(TaskValidator.MAX_TAG_LENGTH)
        assertTrue(TaskValidator.validateTagName(maxTag).isSuccess)

        val tooLongTag = "T".repeat(TaskValidator.MAX_TAG_LENGTH + 1)
        assertTrue(TaskValidator.validateTagName(tooLongTag).isFailure)
    }

    @Test
    fun validateTask_validOneOffTask_succeeds() {
        val task = Task(
            title = "File quarterly taxes",
            description = "Gather receipts and submit by Friday",
            dueDate = LocalDate.of(2026, 11, 1),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
        val result = TaskValidator.validateTask(task)
        assertTrue(result.isSuccess)
    }

    @Test
    fun validateTask_subtaskHierarchyRules() {
        val subtask = Task(
            title = "Subtask 1",
            parentId = 10L,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
        assertTrue("Valid subtask with depth 1", TaskValidator.validateTask(subtask).isSuccess)

        // Depth > 1 forbidden: subtask cannot have its own subtasks
        val nestedSubtask = subtask.copy(
            subtasks = listOf(
                Task(
                    title = "Sub-subtask",
                    parentId = 20L,
                    createdAt = Instant.EPOCH,
                    updatedAt = Instant.EPOCH
                )
            )
        )
        val nestedResult = TaskValidator.validateTask(nestedSubtask)
        assertTrue(nestedResult.isFailure)
        assertEquals("subtasks", (nestedResult.errorOrNull() as AppError.Validation).field)

        // Subtasks cannot recur
        val recurringSubtask = subtask.copy(
            recurrence = TaskRecurrence(type = RecurrenceType.DAILY)
        )
        val recurResult = TaskValidator.validateTask(recurringSubtask)
        assertTrue(recurResult.isFailure)
        assertEquals("recurrence", (recurResult.errorOrNull() as AppError.Validation).field)
    }

    @Test
    fun validateTask_recurringRules() {
        val recurringTaskWithoutDueDate = Task(
            title = "Daily standup",
            recurrence = TaskRecurrence(type = RecurrenceType.DAILY, interval = 1),
            dueDate = null,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
        val noDueDateResult = TaskValidator.validateTask(recurringTaskWithoutDueDate)
        assertTrue("Recurring task requires due date", noDueDateResult.isFailure)
        assertEquals("dueDate", (noDueDateResult.errorOrNull() as AppError.Validation).field)

        val recurringWithSubtasks = recurringTaskWithoutDueDate.copy(
            dueDate = LocalDate.of(2026, 10, 25),
            subtasks = listOf(
                Task(
                    title = "Child",
                    parentId = 1L,
                    createdAt = Instant.EPOCH,
                    updatedAt = Instant.EPOCH
                )
            )
        )
        val recurSubtaskResult = TaskValidator.validateTask(recurringWithSubtasks)
        assertTrue("Recurring task cannot have subtasks", recurSubtaskResult.isFailure)

        val invalidIntervalTask = recurringTaskWithoutDueDate.copy(
            dueDate = LocalDate.of(2026, 10, 25),
            recurrence = TaskRecurrence(type = RecurrenceType.DAILY, interval = 0)
        )
        assertTrue(TaskValidator.validateTask(invalidIntervalTask).isFailure)

        val validRecurring = recurringTaskWithoutDueDate.copy(
            dueDate = LocalDate.of(2026, 10, 25)
        )
        assertTrue(TaskValidator.validateTask(validRecurring).isSuccess)
    }
}
