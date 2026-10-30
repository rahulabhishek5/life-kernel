package com.lifekernel.app.feature.tasks

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.lifekernel.app.core.designsystem.components.PosButton
import com.lifekernel.app.core.designsystem.components.PosButtonVariant
import com.lifekernel.app.core.designsystem.components.PosChip
import com.lifekernel.app.core.designsystem.components.PosConfirmationDialog
import com.lifekernel.app.core.designsystem.components.PosProgressBar
import com.lifekernel.app.core.designsystem.components.PosTextField
import com.lifekernel.app.core.designsystem.components.PosTopBar
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.di.LocalAppContainer
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.feature.tasks.components.PosTagPicker
import com.lifekernel.app.feature.tasks.components.TaskDetailSubtaskRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Task detail, edit, and deletion view per PRD §8.3 (FR-TASK-001..003) and design-system.md §8.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel? = null,
    onPickDateOverride: ((isStartDate: Boolean) -> Unit)? = null
) {
    val container = if (viewModel == null) LocalAppContainer.current else null
    val resolvedViewModel = viewModel ?: remember(container, taskId) {
        requireNotNull(container).createTaskDetailViewModel(taskId)
    }

    val uiState by resolvedViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clock = resolvedViewModel.clock
    val today = clock.today()
    val tomorrow = today.plusDays(1)

    // Handle system back: Dismiss delete dialog first if open, else navigate back
    BackHandler {
        if (uiState.showDeleteDialog) {
            resolvedViewModel.onDismissDeleteDialog()
        } else {
            onBack()
        }
    }

    // Auto-navigate back after successful save or delete
    LaunchedEffect(uiState.isDeleted, uiState.isSaved) {
        if (uiState.isDeleted || uiState.isSaved) {
            onBack()
        }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US) }

    val onPickDateClicked: (Boolean) -> Unit = { isStartDate ->
        if (onPickDateOverride != null) {
            onPickDateOverride(isStartDate)
        } else {
            val initial = if (isStartDate) uiState.startDate ?: today else uiState.dueDate ?: today
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selected = LocalDate.of(year, month + 1, dayOfMonth)
                    if (isStartDate) {
                        resolvedViewModel.onStartDateChange(selected)
                    } else {
                        resolvedViewModel.onDueDateChange(selected)
                    }
                },
                initial.year,
                initial.monthValue - 1,
                initial.dayOfMonth
            ).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PosTheme.colors.background)
            .testTag("task_detail_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PosTopBar(
                title = "Task Details",
                isBackNavigation = true,
                onNavigationClick = {
                    if (uiState.showDeleteDialog) {
                        resolvedViewModel.onDismissDeleteDialog()
                    } else {
                        onBack()
                    }
                }
            )

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = PosTheme.spacing.space4, vertical = PosTheme.spacing.space3)
            ) {
                // 1. Status Readout
                Text(
                    text = "STATUS",
                    style = PosTheme.typography.label,
                    color = PosTheme.colors.textMuted,
                    modifier = Modifier.padding(bottom = PosTheme.spacing.space1)
                )

                val statusText = if (uiState.isCompleted) "COMPLETED" else "OPEN"
                val statusColor = if (uiState.isCompleted) PosTheme.colors.stateSuccess else PosTheme.colors.accent
                Box(
                    modifier = Modifier
                        .background(PosTheme.colors.surfaceVariant, PosTheme.shapes.radiusSmall)
                        .border(PosTheme.borders.borderStandard, statusColor, PosTheme.shapes.radiusSmall)
                        .padding(horizontal = PosTheme.spacing.space3, vertical = PosTheme.spacing.space1)
                        .testTag("task_detail_status_readout")
                ) {
                    Text(
                        text = statusText,
                        style = PosTheme.typography.label.copy(
                            fontFamily = PosFonts.mono,
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

                // 2. Title Field (with inline validation)
                PosTextField(
                    value = uiState.title,
                    onValueChange = resolvedViewModel::onTitleChange,
                    label = "TITLE",
                    placeholder = "Task title",
                    errorMessage = uiState.titleError,
                    inputModifier = Modifier.testTag("task_detail_title_input")
                )

                Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

                // 3. Description Field (multi-line)
                PosTextField(
                    value = uiState.description,
                    onValueChange = resolvedViewModel::onDescriptionChange,
                    label = "DESCRIPTION",
                    placeholder = "Add notes or description...",
                    singleLine = false,
                    errorMessage = uiState.descriptionError,
                    inputModifier = Modifier
                        .testTag("task_detail_description_input")
                        .heightIn(min = 96.dp)
                )

                Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

                // 4. Priority Selector
                Text(
                    text = "PRIORITY",
                    style = PosTheme.typography.label,
                    color = PosTheme.colors.textMuted,
                    modifier = Modifier.padding(bottom = PosTheme.spacing.space2)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
                ) {
                    TaskPriority.entries.forEach { priority ->
                        val isSelected = uiState.priority == priority
                        val label = when (priority) {
                            TaskPriority.NONE -> "None"
                            TaskPriority.LOW -> "Low ·"
                            TaskPriority.MEDIUM -> "Medium !"
                            TaskPriority.HIGH -> "High !!"
                        }
                        PosChip(
                            text = label,
                            selected = isSelected,
                            onClick = { resolvedViewModel.onPriorityChange(priority) },
                            modifier = Modifier.testTag("priority_chip_${priority.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

                // 5. Start Date Picker & Presets
                Text(
                    text = "START DATE",
                    style = PosTheme.typography.label,
                    color = PosTheme.colors.textMuted,
                    modifier = Modifier.padding(bottom = PosTheme.spacing.space2)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
                ) {
                    PosChip(
                        text = "None",
                        selected = uiState.startDate == null,
                        onClick = { resolvedViewModel.onStartDateChange(null) },
                        modifier = Modifier.testTag("start_date_chip_none")
                    )

                    PosChip(
                        text = "Today",
                        selected = uiState.startDate == today,
                        onClick = { resolvedViewModel.onStartDateChange(today) },
                        modifier = Modifier.testTag("start_date_chip_today")
                    )

                    val isCustomStart = uiState.startDate != null && uiState.startDate != today
                    val startPickLabel = if (isCustomStart) {
                        uiState.startDate!!.format(dateFormatter)
                    } else {
                        "Pick date"
                    }
                    PosChip(
                        text = startPickLabel,
                        selected = isCustomStart,
                        onClick = { onPickDateClicked(true) },
                        modifier = Modifier.testTag("start_date_chip_pick")
                    )
                }

                Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

                // 6. Due Date Picker & Presets
                Text(
                    text = "DUE DATE",
                    style = PosTheme.typography.label,
                    color = PosTheme.colors.textMuted,
                    modifier = Modifier.padding(bottom = PosTheme.spacing.space2)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
                ) {
                    PosChip(
                        text = "None",
                        selected = uiState.dueDate == null,
                        onClick = { resolvedViewModel.onDueDateChange(null) },
                        modifier = Modifier.testTag("due_date_chip_none")
                    )

                    PosChip(
                        text = "Today",
                        selected = uiState.dueDate == today,
                        onClick = { resolvedViewModel.onDueDateChange(today) },
                        modifier = Modifier.testTag("due_date_chip_today")
                    )

                    PosChip(
                        text = "Tomorrow",
                        selected = uiState.dueDate == tomorrow,
                        onClick = { resolvedViewModel.onDueDateChange(tomorrow) },
                        modifier = Modifier.testTag("due_date_chip_tomorrow")
                    )

                    val isCustomDue = uiState.dueDate != null && uiState.dueDate != today && uiState.dueDate != tomorrow
                    val duePickLabel = if (isCustomDue) {
                        uiState.dueDate!!.format(dateFormatter)
                    } else {
                        "Pick date"
                    }
                    PosChip(
                        text = duePickLabel,
                        selected = isCustomDue,
                        onClick = { onPickDateClicked(false) },
                        modifier = Modifier.testTag("due_date_chip_pick")
                    )
                }

                // Date Error Feedback
                if (uiState.dateError != null) {
                    Spacer(modifier = Modifier.height(PosTheme.spacing.space1))
                    Text(
                        text = uiState.dateError!!,
                        style = PosTheme.typography.label,
                        color = PosTheme.colors.stateError,
                        modifier = Modifier.testTag("task_detail_date_error")
                    )
                }

                // 7. Tags Picker
                Spacer(modifier = Modifier.height(PosTheme.spacing.space4))
                PosTagPicker(
                    assignedTags = uiState.tags,
                    availableTags = uiState.allAvailableTags,
                    query = uiState.tagInput,
                    onQueryChange = resolvedViewModel::onTagInputChanged,
                    errorMessage = uiState.tagInputError,
                    onAddTag = resolvedViewModel::onAddTag,
                    onRemoveTag = resolvedViewModel::onRemoveTag,
                    modifier = Modifier.testTag("task_detail_tag_picker")
                )

                // 8. Subtasks Checklist Section
                Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

                // Progress Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val subtasksHeaderText = if (uiState.totalSubtasksCount > 0) {
                        "SUBTASKS: ${uiState.completedSubtasksCount} / ${uiState.totalSubtasksCount}"
                    } else {
                        "SUBTASKS"
                    }
                    Text(
                        text = subtasksHeaderText,
                        style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
                        color = PosTheme.colors.textMuted,
                        modifier = Modifier.testTag("task_detail_subtasks_header")
                    )
                }

                if (uiState.totalSubtasksCount > 0) {
                    Spacer(modifier = Modifier.height(PosTheme.spacing.space2))
                    PosProgressBar(
                        progress = uiState.subtaskProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_detail_subtasks_progress_bar")
                    )
                }

                // Subtask Items List
                if (uiState.subtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_detail_subtasks_list"),
                        verticalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
                    ) {
                        uiState.subtasks.forEach { subtask ->
                            TaskDetailSubtaskRow(
                                subtask = subtask,
                                onToggle = resolvedViewModel::onToggleSubtask,
                                onDelete = resolvedViewModel::onDeleteSubtask
                            )
                        }
                    }
                }

                // Subtask Quick-Input Field
                Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
                ) {
                    PosTextField(
                        value = uiState.subtaskInput,
                        onValueChange = resolvedViewModel::onSubtaskInputChange,
                        label = "ADD SUBTASK",
                        placeholder = "Type subtask...",
                        errorMessage = uiState.subtaskInputError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { resolvedViewModel.onAddSubtask() }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("task_detail_subtask_input")
                    )

                    PosButton(
                        text = "ADD",
                        onClick = resolvedViewModel::onAddSubtask,
                        variant = PosButtonVariant.Secondary,
                        modifier = Modifier
                            .padding(top = PosTheme.spacing.space6)
                            .testTag("task_detail_subtask_add_button")
                    )
                }

                Spacer(modifier = Modifier.height(PosTheme.spacing.space8))

                // 8. Action Buttons: Save & Delete
                PosButton(
                    text = if (uiState.isSaving) "SAVING..." else "SAVE CHANGES",
                    onClick = resolvedViewModel::onSave,
                    variant = PosButtonVariant.Primary,
                    enabled = uiState.canSave,
                    fillMaxWidth = true,
                    modifier = Modifier.testTag("task_detail_save_button")
                )

                Spacer(modifier = Modifier.height(PosTheme.spacing.space3))

                PosButton(
                    text = "DELETE TASK",
                    onClick = resolvedViewModel::onShowDeleteDialog,
                    variant = PosButtonVariant.Destructive,
                    fillMaxWidth = true,
                    modifier = Modifier.testTag("task_detail_delete_button")
                )

                Spacer(
                    modifier = Modifier
                        .height(PosTheme.spacing.space8)
                        .navigationBarsPadding()
                )
            }
        }

        // 9. Modal Confirmation Delete Dialog
        if (uiState.showDeleteDialog) {
            val taskName = uiState.title.ifBlank { "this task" }
            val deleteMessage = if (uiState.subtasksCount > 0) {
                val subtaskNote = if (uiState.subtasksCount == 1) "1 subtask" else "${uiState.subtasksCount} subtasks"
                "Are you sure you want to delete '$taskName'? $subtaskNote will also be deleted."
            } else {
                "Are you sure you want to delete '$taskName'?"
            }

            PosConfirmationDialog(
                title = "DELETE TASK",
                message = deleteMessage,
                confirmText = "DELETE",
                dismissText = "CANCEL",
                isDestructive = true,
                onConfirm = resolvedViewModel::onConfirmDelete,
                onDismiss = resolvedViewModel::onDismissDeleteDialog,
                modifier = Modifier.testTag("delete_task_confirmation_dialog")
            )
        }
    }
}
