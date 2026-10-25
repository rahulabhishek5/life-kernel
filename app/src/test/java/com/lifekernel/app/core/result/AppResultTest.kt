package com.lifekernel.app.core.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {

    @Test
    fun success_containsDataAndCorrectFlags() {
        val result = AppResult.success("test-payload")

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertEquals("test-payload", result.getOrNull())
        assertNull(result.errorOrNull())
    }

    @Test
    fun failure_containsErrorAndCorrectFlags() {
        val error = AppError.Validation(message = "Title is required", field = "title")
        val result: AppResult<String> = AppResult.failure(error)

        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
        assertNull(result.getOrNull())
        assertEquals(error, result.errorOrNull())
        assertEquals("Title is required", result.errorOrNull()?.message)
    }

    @Test
    fun map_transformsSuccessValue() {
        val result = AppResult.success(10).map { it * 2 }

        assertEquals(20, result.getOrNull())
    }

    @Test
    fun map_propagatesFailureUntouched() {
        val error = AppError.NotFound("Record not found", identifier = "42")
        val failure: AppResult<Int> = AppResult.failure(error)
        val mapped = failure.map { it * 2 }

        assertTrue(mapped.isFailure)
        assertEquals(error, mapped.errorOrNull())
    }

    @Test
    fun flatMap_chainsSuccessResult() {
        val result = AppResult.success(5).flatMap { value ->
            AppResult.success("Value: $value")
        }

        assertEquals("Value: 5", result.getOrNull())
    }

    @Test
    fun flatMap_shortCircuitsOnFailure() {
        val error = AppError.Storage("Disk full")
        val failure: AppResult<Int> = AppResult.failure(error)
        val chained = failure.flatMap { AppResult.success("Success $it") }

        assertTrue(chained.isFailure)
        assertEquals(error, chained.errorOrNull())
    }

    @Test
    fun onSuccessAndOnFailure_triggerAppropriately() {
        var successFired = false
        var failureFired = false

        AppResult.success(1).onSuccess { successFired = true }
        AppResult.failure(AppError.Generic("Error")).onFailure { failureFired = true }

        assertTrue(successFired)
        assertTrue(failureFired)
    }
}
