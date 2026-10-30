package com.lifekernel.app.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.domain.model.Subtask
import com.lifekernel.app.domain.model.SubtaskValidator
import com.lifekernel.app.domain.model.TagValidator
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskValidator
import com.lifekernel.app.domain.usecase.AddSubtaskUseCase
import com.lifekernel.app.domain.usecase.DeleteSubtaskUseCase
import com.lifekernel.app.domain.usecase.DeleteTaskUseCase
import com.lifekernel.app.domain.usecase.GetTaskByIdUseCase
import com.lifekernel.app.domain.usecase.ObserveAllTagsUseCase
import com.lifekernel.app.domain.usecase.ObserveSubtasksForTaskUseCase
import com.lifekernel.app.domain.usecase.SyncTaskTagsUseCase
import com.lifekernel.app.domain.usecase.ToggleSubtaskUseCase
import com.lifekernel.app.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel managing task detail, inline editing, subtasks checklist, and deletion flow per ARCHITECTURE.md §3 & §6.
 */
class TaskDetailViewModel(
    val taskId: Long,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val observeAllTagsUseCase: ObserveAllTagsUseCase? = null,
    private val syncTaskTagsUseCase: SyncTaskTagsUseCase? = null,
    private val observeSubtasksForTaskUseCase: ObserveSubtasksForTaskUseCase? = null,
    private val addSubtaskUseCase: AddSubtaskUseCase? = null,
    private val toggleSubtaskUseCase: ToggleSubtaskUseCase? = null,
    private val deleteSubtaskUseCase: DeleteSubtaskUseCase? = null,
    val clock: Clock,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        if (observeAllTagsUseCase != null) {
            scope.launch {
                observeAllTagsUseCase().collect { allTags ->
                    _uiState.update { it.copy(allAvailableTags = allTags) }
                }
            }
        }

        if (observeSubtasksForTaskUseCase != null) {
            scope.launch {
                observeSubtasksForTaskUseCase(taskId).collect { subtasks ->
                    _uiState.update { it.copy(subtasks = subtasks) }
                }
            }
        }

        scope.launch {
            val taskResult = getTaskByIdUseCase.get(taskId)
            val task = taskResult.getOrNull()
            if (task != null) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        task = task,
                        title = task.title,
                        description = task.description ?: "",
                        priority = task.priority,
                        startDate = task.startDate,
                        dueDate = task.dueDate,
                        isCompleted = task.isCompleted,
                        tags = task.tags.map { it.name }
                    )
                }
            } else {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        userMessage = "Task not found"
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        val error = when {
            newTitle.trim().isEmpty() -> "Title cannot be empty"
            newTitle.trim().length > TaskValidator.MAX_TITLE_LENGTH -> "Title cannot exceed ${TaskValidator.MAX_TITLE_LENGTH} characters"
            else -> null
        }
        _uiState.update { it.copy(title = newTitle, titleError = error) }
    }

    fun onDescriptionChange(newDescription: String) {
        val error = if (newDescription.length > TaskValidator.MAX_DESCRIPTION_LENGTH) {
            "Description cannot exceed ${TaskValidator.MAX_DESCRIPTION_LENGTH} characters"
        } else {
            null
        }
        _uiState.update { it.copy(description = newDescription, descriptionError = error) }
    }

    fun onPriorityChange(newPriority: TaskPriority) {
        _uiState.update { it.copy(priority = newPriority) }
    }

    fun onStartDateChange(newStartDate: LocalDate?) {
        val dateError = if (newStartDate != null && _uiState.value.dueDate != null && newStartDate > _uiState.value.dueDate!!) {
            "Start date cannot be after due date"
        } else {
            null
        }
        _uiState.update { it.copy(startDate = newStartDate, dateError = dateError) }
    }

    fun onDueDateChange(newDueDate: LocalDate?) {
        val dateError = if (_uiState.value.startDate != null && newDueDate != null && _uiState.value.startDate!! > newDueDate) {
            "Start date cannot be after due date"
        } else {
            null
        }
        _uiState.update { it.copy(dueDate = newDueDate, dateError = dateError) }
    }

    fun onShowDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete() {
        scope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = deleteTaskUseCase(taskId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        isSaving = false,
                        isDeleted = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showDeleteDialog = false,
                        userMessage = result.errorOrNull()?.message ?: "Failed to delete task"
                    )
                }
            }
        }
    }

    fun onTagInputChanged(newInput: String) {
        val error = if (newInput.trim().isNotEmpty()) {
            val result = TagValidator.validateTagName(newInput)
            if (result.isFailure) {
                result.errorOrNull()?.message
            } else {
                null
            }
        } else {
            null
        }
        _uiState.update { it.copy(tagInput = newInput, tagInputError = error) }
    }

    fun onAddTag(tagName: String) {
        val validation = TagValidator.validateTagName(tagName)
        if (validation.isFailure) {
            _uiState.update { it.copy(tagInputError = validation.errorOrNull()?.message) }
            return
        }
        val normalized = validation.getOrNull()!!
        _uiState.update { current ->
            val currentTags = current.tags
            val updatedTags = if (currentTags.any { it.equals(normalized, ignoreCase = true) }) {
                currentTags
            } else {
                currentTags + normalized
            }
            current.copy(
                tags = updatedTags,
                tagInput = "",
                tagInputError = null
            )
        }
    }

    fun onRemoveTag(tagName: String) {
        _uiState.update { current ->
            current.copy(
                tags = current.tags.filterNot { it.equals(tagName, ignoreCase = true) }
            )
        }
    }

    fun onSubtaskInputChange(newInput: String) {
        val error = if (newInput.length > SubtaskValidator.MAX_TITLE_LENGTH) {
            "Subtask title cannot exceed ${SubtaskValidator.MAX_TITLE_LENGTH} characters"
        } else {
            null
        }
        _uiState.update { it.copy(subtaskInput = newInput, subtaskInputError = error) }
    }

    fun onAddSubtask() {
        val input = _uiState.value.subtaskInput
        val validation = SubtaskValidator.validateTitle(input)
        if (validation.isFailure) {
            _uiState.update { it.copy(subtaskInputError = validation.errorOrNull()?.message) }
            return
        }
        val validTitle = validation.getOrNull()!!

        if (addSubtaskUseCase != null) {
            scope.launch {
                val result = addSubtaskUseCase(taskId, validTitle)
                if (result.isSuccess) {
                    _uiState.update { it.copy(subtaskInput = "", subtaskInputError = null) }
                } else {
                    _uiState.update { it.copy(subtaskInputError = result.errorOrNull()?.message) }
                }
            }
        } else {
            _uiState.update { current ->
                val nextPos = current.subtasks.size
                val nextId = (current.subtasks.maxOfOrNull { it.id } ?: 0L) + 1L
                val newSubtask = Subtask(
                    id = nextId,
                    taskId = taskId,
                    title = validTitle,
                    isCompleted = false,
                    position = nextPos,
                    createdAt = clock.nowEpochMillis(),
                    updatedAt = clock.nowEpochMillis()
                )
                current.copy(
                    subtasks = current.subtasks + newSubtask,
                    subtaskInput = "",
                    subtaskInputError = null
                )
            }
        }
    }

    fun onToggleSubtask(subtask: Subtask) {
        if (toggleSubtaskUseCase != null) {
            scope.launch {
                toggleSubtaskUseCase(subtask.id)
            }
        } else {
            _uiState.update { current ->
                current.copy(
                    subtasks = current.subtasks.map {
                        if (it.id == subtask.id) it.copy(isCompleted = !it.isCompleted) else it
                    }
                )
            }
        }
    }

    fun onDeleteSubtask(subtask: Subtask) {
        if (deleteSubtaskUseCase != null) {
            scope.launch {
                deleteSubtaskUseCase(subtask.id)
            }
        } else {
            _uiState.update { current ->
                current.copy(
                    subtasks = current.subtasks.filterNot { it.id == subtask.id }
                )
            }
        }
    }

    fun onSave() {
        val state = _uiState.value
        val titleTrimmed = state.title.trim()
        if (titleTrimmed.isEmpty()) {
            _uiState.update { it.copy(titleError = "Title cannot be empty") }
            return
        }
        if (state.startDate != null && state.dueDate != null && state.startDate > state.dueDate) {
            _uiState.update { it.copy(dateError = "Start date cannot be after due date") }
            return
        }

        val currentTask = state.task ?: return
        val updatedTask = currentTask.copy(
            title = titleTrimmed,
            description = state.description.trim().ifEmpty { null },
            priority = state.priority,
            startDate = state.startDate,
            dueDate = state.dueDate
        )

        scope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = updateTaskUseCase(updatedTask)
            if (result.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = result.errorOrNull()?.message ?: "Failed to save task"
                    )
                }
                return@launch
            }

            if (syncTaskTagsUseCase != null) {
                val syncResult = syncTaskTagsUseCase(taskId, state.tags)
                if (syncResult.isFailure) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            userMessage = syncResult.errorOrNull()?.message ?: "Failed to save tags"
                        )
                    }
                    return@launch
                }
            }

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
