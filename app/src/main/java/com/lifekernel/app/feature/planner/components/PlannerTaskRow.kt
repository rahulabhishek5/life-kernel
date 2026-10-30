package com.lifekernel.app.feature.planner.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.lifekernel.app.core.designsystem.components.PosCheckbox
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Task row item for the planner view per design-system.md §8.1.
 * Features leading 48dp checkbox, title with completion strike-through,
 * priority indicators, tag labels, and overdue date formatting.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerTaskRow(
    task: Task,
    onToggle: (Task) -> Unit,
    modifier: Modifier = Modifier,
    onTaskClick: ((Task) -> Unit)? = null,
    isOverdue: Boolean = false,
    explicitDateBadge: String? = null
) {
    val reducedMotion = PosTheme.reducedMotion
    val duration = if (reducedMotion) 0 else PosTheme.motion.motionEmphasized

    val titleColor by animateColorAsState(
        targetValue = if (task.isCompleted) PosTheme.colors.textMuted else PosTheme.colors.textPrimary,
        animationSpec = tween(durationMillis = duration),
        label = "TaskRowTitleColor"
    )

    val shape = PosTheme.shapes.radiusMedium
    val borderColor = if (isOverdue && !task.isCompleted) {
        PosTheme.colors.stateWarning.copy(alpha = 0.5f)
    } else {
        PosTheme.colors.outlineSubtle
    }

    val stateDesc = if (task.isCompleted) "Completed" else "Not completed"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = PosTheme.spacing.minTouchTarget)
            .background(PosTheme.colors.surface, shape)
            .border(PosTheme.borders.borderStandard, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (onTaskClick != null) {
                        onTaskClick(task)
                    } else {
                        onToggle(task)
                    }
                }
            )
            .semantics {
                if (onTaskClick != null) {
                    role = Role.Button
                } else {
                    role = Role.Checkbox
                    stateDescription = stateDesc
                }
            }
            .testTag("task_row_${task.id}")
            .padding(horizontal = PosTheme.spacing.space3, vertical = PosTheme.spacing.space2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PosCheckbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task) },
                contentDescription = "${task.title}, $stateDesc",
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(PosTheme.spacing.space2))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = PosTheme.typography.body,
                    color = titleColor,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.tags.isNotEmpty() || task.subtasks.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier
                            .padding(top = PosTheme.spacing.space1)
                            .testTag("task_tags_row_${task.id}"),
                        horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space1),
                        verticalArrangement = Arrangement.spacedBy(PosTheme.spacing.space1)
                    ) {
                        if (task.subtasks.isNotEmpty()) {
                            val completedCount = task.subtasks.count { it.isCompleted }
                            val totalCount = task.subtasks.size
                            Box(
                                modifier = Modifier
                                    .clip(PosTheme.shapes.radiusSmall)
                                    .background(PosTheme.colors.surfaceVariant)
                                    .border(
                                        width = PosTheme.borders.borderHairline,
                                        color = PosTheme.colors.outlineSubtle,
                                        shape = PosTheme.shapes.radiusSmall
                                    )
                                    .padding(
                                        horizontal = PosTheme.spacing.space2,
                                        vertical = PosTheme.spacing.space1
                                    )
                                    .testTag("task_subtask_badge_${task.id}")
                            ) {
                                Text(
                                    text = "$completedCount/$totalCount",
                                    style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
                                    color = if (completedCount == totalCount && totalCount > 0) {
                                        PosTheme.colors.textMuted
                                    } else {
                                        PosTheme.colors.textPrimary
                                    }
                                )
                            }
                        }

                        task.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(PosTheme.shapes.radiusSmall)
                                    .background(PosTheme.colors.surfaceVariant)
                                    .border(
                                        width = PosTheme.borders.borderHairline,
                                        color = PosTheme.colors.outlineSubtle,
                                        shape = PosTheme.shapes.radiusSmall
                                    )
                                    .padding(
                                        horizontal = PosTheme.spacing.space2,
                                        vertical = PosTheme.spacing.space1
                                    )
                                    .testTag("task_tag_${task.id}_${tag.name}")
                            ) {
                                Text(
                                    text = "#${tag.name}",
                                    style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
                                    color = PosTheme.colors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            // Priority badge / symbol (!! for High, ! for Medium, · for Low)
            if (!task.isCompleted && task.priority != TaskPriority.NONE) {
                Spacer(modifier = Modifier.width(PosTheme.spacing.space2))
                val priorityText = when (task.priority) {
                    TaskPriority.HIGH -> "!!"
                    TaskPriority.MEDIUM -> "!"
                    TaskPriority.LOW -> "·"
                    TaskPriority.NONE -> ""
                }
                val priorityColor = when (task.priority) {
                    TaskPriority.HIGH -> PosTheme.colors.stateError
                    TaskPriority.MEDIUM -> PosTheme.colors.stateWarning
                    else -> PosTheme.colors.textMuted
                }
                Text(
                    text = priorityText,
                    style = PosTheme.typography.title.copy(
                        fontFamily = PosFonts.mono,
                        fontWeight = FontWeight.Bold
                    ),
                    color = priorityColor,
                    modifier = Modifier.testTag("task_priority_${task.id}")
                )
            }

            // Due date / Explicit date badge (highlighted in stateWarning if overdue)
            val badgeText = explicitDateBadge ?: if (task.dueDate != null) {
                task.dueDate!!.format(DateTimeFormatter.ofPattern("d MMM", Locale.US)).uppercase(Locale.US)
            } else null

            if (badgeText != null) {
                Spacer(modifier = Modifier.width(PosTheme.spacing.space2))
                val isWarning = (isOverdue || explicitDateBadge == "OVERDUE") && !task.isCompleted
                val dateColor = if (isWarning) {
                    PosTheme.colors.stateWarning
                } else {
                    PosTheme.colors.textMuted
                }
                Text(
                    text = badgeText,
                    style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
                    color = dateColor,
                    modifier = Modifier.testTag("task_date_${task.id}")
                )
            }
        }
    }
}
