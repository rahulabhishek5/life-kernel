package com.lifekernel.app.domain.model

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult

/**
 * Domain validation rules for subtasks per PRD §8.3, FR-TASK-002, and DATABASE_SCHEMA.md §2.4.
 *
 * Rules:
 * - Title: 1 to 200 characters, trimmed of leading/trailing whitespace.
 * - Rejects: Empty/blank titles.
 * - Position: Non-negative integer (position >= 0).
 * - TaskId: Must be a positive task identifier (taskId > 0).
 */
object SubtaskValidator {

    const val MIN_TITLE_LENGTH = 1
    const val MAX_TITLE_LENGTH = 200

    /**
     * Validates and trims a subtask title.
     * Returns the trimmed title on success, or [AppError.Validation] on failure.
     */
    fun validateTitle(title: String): AppResult<String> {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Subtask title cannot be empty",
                    field = "title"
                )
            )
        }
        if (trimmed.length > MAX_TITLE_LENGTH) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Subtask title cannot exceed $MAX_TITLE_LENGTH characters",
                    field = "title"
                )
            )
        }
        return AppResult.success(trimmed)
    }

    /**
     * Validates that position is non-negative.
     */
    fun validatePosition(position: Int): AppResult<Int> {
        if (position < 0) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Subtask position cannot be negative",
                    field = "position"
                )
            )
        }
        return AppResult.success(position)
    }

    /**
     * Validates a complete [Subtask] model.
     */
    fun validateSubtask(subtask: Subtask): AppResult<Unit> {
        if (subtask.taskId <= 0L) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Subtask must be associated with a valid task ID",
                    field = "taskId"
                )
            )
        }
        val titleResult = validateTitle(subtask.title)
        if (titleResult.isFailure) {
            return AppResult.failure(titleResult.errorOrNull()!!)
        }
        val positionResult = validatePosition(subtask.position)
        if (positionResult.isFailure) {
            return AppResult.failure(positionResult.errorOrNull()!!)
        }
        return AppResult.success(Unit)
    }
}
