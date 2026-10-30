package com.lifekernel.app.domain.model

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult

/**
 * Domain validation rules for tags per PRD §8.4, AC-TAG, and DATABASE_SCHEMA.md §2.4.
 *
 * Rules:
 * - Length: 1 to 30 characters.
 * - Whitespace: Leading and trailing whitespace stripped.
 * - Normalization: Strictly lowercase normalized.
 * - Allowed characters: Lowercase alphanumeric, hyphens, and underscores (`^[a-z0-9_-]+$`).
 * - Rejects: Empty strings, spaces, and invalid symbols.
 * - Prefix: Leading '#' prefix is stripped if provided.
 */
object TagValidator {

    const val MIN_TAG_LENGTH = 1
    const val MAX_TAG_LENGTH = 30
    private val TAG_REGEX = Regex("^[a-z0-9_-]+$")

    /**
     * Validates and normalizes a tag name.
     * Returns the lowercase normalized tag name on success, or [AppError.Validation] on failure.
     */
    fun validateTagName(name: String): AppResult<String> {
        val trimmed = name.trim().removePrefix("#").lowercase()
        if (trimmed.isEmpty()) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Tag name cannot be empty",
                    field = "name"
                )
            )
        }
        if (trimmed.length > MAX_TAG_LENGTH) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Tag name cannot exceed $MAX_TAG_LENGTH characters",
                    field = "name"
                )
            )
        }
        if (!TAG_REGEX.matches(trimmed)) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Tag name may only contain lowercase alphanumeric characters, hyphens, and underscores",
                    field = "name"
                )
            )
        }
        return AppResult.success(trimmed)
    }
}
