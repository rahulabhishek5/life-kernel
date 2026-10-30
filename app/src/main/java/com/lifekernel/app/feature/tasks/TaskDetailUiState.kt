package com.lifekernel.app.feature.tasks

import androidx.compose.runtime.Immutable
import com.lifekernel.app.domain.model.Subtask
import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import java.time.LocalDate

/**
 * Immutable UI state for task detail and edit screen per ARCHITECTURE.md §6 & §13.
 */
@Immutable
data class TaskDetailUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val descriptionError: String? = null,
    val priority: TaskPriority = TaskPriority.NONE,
    val startDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val dateError: String? = null,
    val isCompleted: Boolean = false,
    val subtasks: List<Subtask> = emptyList(),
    val subtaskInput: String = "",
    val subtaskInputError: String? = null,
    val tags: List<String> = emptyList(),
    val allAvailableTags: List<Tag> = emptyList(),
    val tagInput: String = "",
    val tagInputError: String? = null,
    val showDeleteDialog: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleted: Boolean = false,
    val isSaved: Boolean = false,
    val userMessage: String? = null
) {
    val subtasksCount: Int
        get() = subtasks.size

    val totalSubtasksCount: Int
        get() = subtasks.size

    val completedSubtasksCount: Int
        get() = subtasks.count { it.isCompleted }

    val subtaskProgress: Float
        get() = if (subtasks.isNotEmpty()) completedSubtasksCount.toFloat() / subtasks.size.toFloat() else 0f

    val canSave: Boolean
        get() = !isLoading &&
            !isSaving &&
            title.trim().isNotEmpty() &&
            titleError == null &&
            descriptionError == null &&
            dateError == null &&
            tagInputError == null
}
