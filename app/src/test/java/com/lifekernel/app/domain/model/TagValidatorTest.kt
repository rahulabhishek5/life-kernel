package com.lifekernel.app.domain.model

import com.lifekernel.app.core.result.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TagValidator] per PRD §8.4, AC-TAG, and DATABASE_SCHEMA.md §2.4.
 */
class TagValidatorTest {

    @Test
    fun validateTagName_validLimits_succeeds() {
        val minTag = "a"
        val minResult = TagValidator.validateTagName(minTag)
        assertTrue(minResult.isSuccess)
        assertEquals("a", minResult.getOrNull())

        val maxTag = "a".repeat(TagValidator.MAX_TAG_LENGTH)
        val maxResult = TagValidator.validateTagName(maxTag)
        assertTrue(maxResult.isSuccess)
        assertEquals(maxTag, maxResult.getOrNull())
    }

    @Test
    fun validateTagName_emptyOrWhitespace_fails() {
        val emptyResult = TagValidator.validateTagName("")
        assertTrue(emptyResult.isFailure)
        assertEquals("name", (emptyResult.errorOrNull() as AppError.Validation).field)

        val whitespaceResult = TagValidator.validateTagName("   \t\n  ")
        assertTrue(whitespaceResult.isFailure)
    }

    @Test
    fun validateTagName_exceedingLengthLimit_fails() {
        val tooLongTag = "a".repeat(TagValidator.MAX_TAG_LENGTH + 1)
        val result = TagValidator.validateTagName(tooLongTag)
        assertTrue(result.isFailure)
        val error = result.errorOrNull() as AppError.Validation
        assertEquals("name", error.field)
        assertTrue(error.message.contains("cannot exceed 30"))
    }

    @Test
    fun validateTagName_lowercaseNormalization_succeeds() {
        val mixedResult = TagValidator.validateTagName("Urgent")
        assertTrue(mixedResult.isSuccess)
        assertEquals("urgent", mixedResult.getOrNull())

        val allCapsResult = TagValidator.validateTagName("WORK")
        assertTrue(allCapsResult.isSuccess)
        assertEquals("work", allCapsResult.getOrNull())
    }

    @Test
    fun validateTagName_trimsLeadingAndTrailingWhitespace() {
        val result = TagValidator.validateTagName("   study   ")
        assertTrue(result.isSuccess)
        assertEquals("study", result.getOrNull())
    }

    @Test
    fun validateTagName_stripsLeadingHashPrefix() {
        val result = TagValidator.validateTagName("#focus")
        assertTrue(result.isSuccess)
        assertEquals("focus", result.getOrNull())

        val hashOnlyResult = TagValidator.validateTagName("#")
        assertTrue(hashOnlyResult.isFailure)
    }

    @Test
    fun validateTagName_rejectsSpaces() {
        val spaceResult = TagValidator.validateTagName("study math")
        assertTrue(spaceResult.isFailure)
        val error = spaceResult.errorOrNull() as AppError.Validation
        assertEquals("name", error.field)

        val middleSpaceResult = TagValidator.validateTagName("work project")
        assertTrue(middleSpaceResult.isFailure)
    }

    @Test
    fun validateTagName_allowedSymbols_hyphenAndUnderscore() {
        val validResult = TagValidator.validateTagName("life-kernel_v1")
        assertTrue(validResult.isSuccess)
        assertEquals("life-kernel_v1", validResult.getOrNull())
    }

    @Test
    fun validateTagName_rejectsInvalidSymbols() {
        val atSymbol = TagValidator.validateTagName("tag@work")
        assertTrue(atSymbol.isFailure)

        val exclamation = TagValidator.validateTagName("urgent!")
        assertTrue(exclamation.isFailure)

        val dot = TagValidator.validateTagName("tag.name")
        assertTrue(dot.isFailure)

        val middleHash = TagValidator.validateTagName("tag#name")
        assertTrue(middleHash.isFailure)
    }
}
