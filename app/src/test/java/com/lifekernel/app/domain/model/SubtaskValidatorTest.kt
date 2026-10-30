package com.lifekernel.app.domain.model

import com.lifekernel.app.core.result.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SubtaskValidator] per PRD §8.3, FR-TASK-002, and DATABASE_SCHEMA.md §2.4.
 */
class SubtaskValidatorTest {

    @Test
    fun validateTitle_validLimits_succeeds() {
        val minTitle = "a"
        val minResult = SubtaskValidator.validateTitle(minTitle)
        assertTrue(minResult.isSuccess)
        assertEquals("a", minResult.getOrNull())

        val maxTitle = "a".repeat(SubtaskValidator.MAX_TITLE_LENGTH)
        val maxResult = SubtaskValidator.validateTitle(maxTitle)
        assertTrue(maxResult.isSuccess)
        assertEquals(maxTitle, maxResult.getOrNull())
    }

    @Test
    fun validateTitle_emptyOrBlank_fails() {
        val emptyResult = SubtaskValidator.validateTitle("")
        assertTrue(emptyResult.isFailure)
        val emptyError = emptyResult.errorOrNull() as AppError.Validation
        assertEquals("title", emptyError.field)

        val blankResult = SubtaskValidator.validateTitle("   \t\n  ")
        assertTrue(blankResult.isFailure)
        val blankError = blankResult.errorOrNull() as AppError.Validation
        assertEquals("title", blankError.field)
    }

    @Test
    fun validateTitle_exceedingLimit_fails() {
        val tooLong = "a".repeat(SubtaskValidator.MAX_TITLE_LENGTH + 1)
        val result = SubtaskValidator.validateTitle(tooLong)
        assertTrue(result.isFailure)
        val error = result.errorOrNull() as AppError.Validation
        assertEquals("title", error.field)
        assertTrue(error.message.contains("cannot exceed 200"))
    }

    @Test
    fun validateTitle_trimsWhitespace() {
        val result = SubtaskValidator.validateTitle("   Buy milk and eggs   ")
        assertTrue(result.isSuccess)
        assertEquals("Buy milk and eggs", result.getOrNull())
    }

    @Test
    fun validatePosition_nonNegative_succeeds() {
        val zeroResult = SubtaskValidator.validatePosition(0)
        assertTrue(zeroResult.isSuccess)
        assertEquals(0, zeroResult.getOrNull())

        val positiveResult = SubtaskValidator.validatePosition(42)
        assertTrue(positiveResult.isSuccess)
        assertEquals(42, positiveResult.getOrNull())
    }

    @Test
    fun validatePosition_negative_fails() {
        val negativeResult = SubtaskValidator.validatePosition(-1)
        assertTrue(negativeResult.isFailure)
        val error = negativeResult.errorOrNull() as AppError.Validation
        assertEquals("position", error.field)
    }

    @Test
    fun validateSubtask_valid_succeeds() {
        val subtask = Subtask(
            id = 1,
            taskId = 10,
            title = "Clean the kitchen",
            isCompleted = false,
            position = 0
        )
        val result = SubtaskValidator.validateSubtask(subtask)
        assertTrue(result.isSuccess)
    }

    @Test
    fun validateSubtask_invalidTaskId_fails() {
        val subtask = Subtask(
            id = 1,
            taskId = 0,
            title = "Clean the kitchen",
            isCompleted = false,
            position = 0
        )
        val result = SubtaskValidator.validateSubtask(subtask)
        assertTrue(result.isFailure)
        val error = result.errorOrNull() as AppError.Validation
        assertEquals("taskId", error.field)
    }
}
