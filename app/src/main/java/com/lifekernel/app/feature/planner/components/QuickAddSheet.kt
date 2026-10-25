package com.lifekernel.app.feature.planner.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.lifekernel.app.core.designsystem.components.PosButton
import com.lifekernel.app.core.designsystem.components.PosChip
import com.lifekernel.app.core.designsystem.components.PosSheet
import com.lifekernel.app.core.designsystem.components.PosTextField
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.feature.planner.QuickAddDatePreset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Quick-add task modal sheet adhering to design-system.md §8.4.
 *
 * Provides rapid task capture with:
 * - Autofocused title text field supporting software keyboard Done action (SM-03 two-interaction capture).
 * - Date preset chips: Today (default), Tomorrow, and Pick date.
 * - Priority chips: None, Low (·), Med (!), and High (!!).
 * - Inline validation on empty title preventing save without closing the sheet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickAddSheet(
    visible: Boolean,
    title: String,
    titleError: String?,
    selectedDatePreset: QuickAddDatePreset,
    customDate: LocalDate?,
    selectedPriority: TaskPriority,
    onTitleChange: (String) -> Unit,
    onDatePresetSelect: (QuickAddDatePreset) -> Unit,
    onCustomDatePicked: (LocalDate) -> Unit,
    onPrioritySelect: (TaskPriority) -> Unit,
    onSave: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onPickDateOverride: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    val reducedMotion = PosTheme.reducedMotion
    LaunchedEffect(visible) {
        if (visible) {
            val enterDuration = if (reducedMotion) 0L else 220L
            kotlinx.coroutines.delay(enterDuration + 50L)
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
                // Non-critical if sheet was dismissed during enter transition
            }
        }
    }

    val customDateFormatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale.US) }
    val customDateText = if (selectedDatePreset == QuickAddDatePreset.PICK_DATE && customDate != null) {
        customDate.format(customDateFormatter).uppercase(Locale.US)
    } else {
        "Pick date"
    }

    val onPickDateClicked = onPickDateOverride ?: {
        val initial = customDate ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onCustomDatePicked(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    PosSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "NEW TASK",
                style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
                color = PosTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = PosTheme.spacing.space3)
            )

            // Title field with autofocus and keyboard Done action
            PosTextField(
                value = title,
                onValueChange = onTitleChange,
                label = "TITLE",
                placeholder = "What needs to be done?",
                errorMessage = titleError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSave() }
                ),
                inputModifier = Modifier
                    .focusRequester(focusRequester)
                    .testTag("quick_add_title_input"),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_add_title_field")
            )

            Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

            // Date selection chips
            Text(
                text = "DUE DATE",
                style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
                color = PosTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = PosTheme.spacing.space2)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2),
                verticalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2),
                modifier = Modifier.fillMaxWidth()
            ) {
                PosChip(
                    text = "Today",
                    selected = selectedDatePreset == QuickAddDatePreset.TODAY,
                    onClick = { onDatePresetSelect(QuickAddDatePreset.TODAY) },
                    modifier = Modifier.testTag("quick_add_date_today")
                )
                PosChip(
                    text = "Tomorrow",
                    selected = selectedDatePreset == QuickAddDatePreset.TOMORROW,
                    onClick = { onDatePresetSelect(QuickAddDatePreset.TOMORROW) },
                    modifier = Modifier.testTag("quick_add_date_tomorrow")
                )
                PosChip(
                    text = customDateText,
                    selected = selectedDatePreset == QuickAddDatePreset.PICK_DATE,
                    onClick = onPickDateClicked,
                    modifier = Modifier.testTag("quick_add_date_pick")
                )
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

            // Priority selection chips
            Text(
                text = "PRIORITY",
                style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
                color = PosTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = PosTheme.spacing.space2)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2),
                verticalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2),
                modifier = Modifier.fillMaxWidth()
            ) {
                PosChip(
                    text = "None",
                    selected = selectedPriority == TaskPriority.NONE,
                    onClick = { onPrioritySelect(TaskPriority.NONE) },
                    modifier = Modifier.testTag("quick_add_priority_none")
                )
                PosChip(
                    text = "Low ·",
                    selected = selectedPriority == TaskPriority.LOW,
                    onClick = { onPrioritySelect(TaskPriority.LOW) },
                    modifier = Modifier.testTag("quick_add_priority_low")
                )
                PosChip(
                    text = "Med !",
                    selected = selectedPriority == TaskPriority.MEDIUM,
                    onClick = { onPrioritySelect(TaskPriority.MEDIUM) },
                    modifier = Modifier.testTag("quick_add_priority_medium")
                )
                PosChip(
                    text = "High !!",
                    selected = selectedPriority == TaskPriority.HIGH,
                    onClick = { onPrioritySelect(TaskPriority.HIGH) },
                    modifier = Modifier.testTag("quick_add_priority_high")
                )
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

            // Save Action Button
            PosButton(
                text = "SAVE",
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_add_save_button")
            )
        }
    }
}
