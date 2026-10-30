package com.lifekernel.app.domain.model

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult

/**
 * Domain validation rules per DATABASE_SCHEMA.md §2.4 & §6.2.
 */
object TaskValidator {

    const val MIN_TITLE_LENGTH = 1
    const val MAX_TITLE_LENGTH = 200
    const val MAX_DESCRIPTION_LENGTH = 10_000
    const val MIN_TAG_LENGTH = TagValidator.MIN_TAG_LENGTH
    const val MAX_TAG_LENGTH = TagValidator.MAX_TAG_LENGTH
    const val MIN_RECUR_INTERVAL = 1
    const val MAX_RECUR_INTERVAL = 365

    fun validateTitle(title: String): AppResult<String> {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Task title cannot be empty",
                    field = "title"
                )
            )
        }
        if (trimmed.length > MAX_TITLE_LENGTH) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Task title cannot exceed $MAX_TITLE_LENGTH characters",
                    field = "title"
                )
            )
        }
        return AppResult.success(trimmed)
    }

    fun validateDescription(description: String?): AppResult<String?> {
        if (description == null || description.isBlank()) {
            return AppResult.success(null)
        }
        if (description.length > MAX_DESCRIPTION_LENGTH) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Task description cannot exceed $MAX_DESCRIPTION_LENGTH characters",
                    field = "description"
                )
            )
        }
        return AppResult.success(description)
    }

    fun validateTagName(name: String): AppResult<String> {
        return TagValidator.validateTagName(name)
    }

    fun validateTask(task: Task): AppResult<Unit> {
        val titleResult = validateTitle(task.title)
        if (titleResult.isFailure) {
            return AppResult.failure(titleResult.errorOrNull()!!)
        }

        val descResult = validateDescription(task.description)
        if (descResult.isFailure) {
            return AppResult.failure(descResult.errorOrNull()!!)
        }

        // Start date vs Due date rule
        if (task.startDate != null && task.dueDate != null && task.startDate > task.dueDate) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Start date cannot be after due date",
                    field = "startDate"
                )
            )
        }

        // Subtask depth rule: Depth is 1 (subtasks cannot have subtasks)
        if (task.isSubtask && task.subtasks.isNotEmpty()) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Subtasks cannot have their own subtasks (maximum depth is 1)",
                    field = "subtasks"
                )
            )
        }

        // Subtasks cannot be recurring
        if (task.isSubtask && task.recurrence.isRecurring) {
            return AppResult.failure(
                AppError.Validation(
                    message = "Subtasks cannot be recurring",
                    field = "recurrence"
                )
            )
        }

        // Recurring tasks rules
        if (task.recurrence.isRecurring) {
            if (task.subtasks.isNotEmpty()) {
                return AppResult.failure(
                    AppError.Validation(
                        message = "Recurring tasks cannot have subtasks",
                        field = "subtasks"
                    )
                )
            }
            if (task.dueDate == null) {
                return AppResult.failure(
                    AppError.Validation(
                        message = "Recurring tasks require a due date",
                        field = "dueDate"
                    )
                )
            }
            val interval = task.recurrence.interval
            if (interval < MIN_RECUR_INTERVAL || interval > MAX_RECUR_INTERVAL) {
                return AppResult.failure(
                    AppError.Validation(
                        message = "Recurrence interval must be between $MIN_RECUR_INTERVAL and $MAX_RECUR_INTERVAL",
                        field = "recurrence.interval"
                    )
                )
            }
        }

        return AppResult.success(Unit)
    }
}
