package com.lifekernel.app.feature.tasks.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.lifekernel.app.core.designsystem.components.PosCheckbox
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.domain.model.Subtask

/**
 * Neo-brutalist subtask checklist row per design-system.md §8.1, §10, §11 and PRD §8.3.
 *
 * Requirements:
 * - Leading PosCheckbox with strict 48dp minimum bounding box.
 * - Subtask title with 220ms strike-through and dimming animation on completion (0ms on reduced motion).
 * - Trailing delete action with strict 48dp minimum bounding box.
 * - Independent completion state from parent task.
 */
@Composable
fun TaskDetailSubtaskRow(
    subtask: Subtask,
    onToggle: (Subtask) -> Unit,
    onDelete: (Subtask) -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = PosTheme.reducedMotion
    val duration = if (reducedMotion) 0 else PosTheme.motion.motionEmphasized

    val textColor by animateColorAsState(
        targetValue = if (subtask.isCompleted) PosTheme.colors.textMuted else PosTheme.colors.textPrimary,
        animationSpec = tween(durationMillis = duration),
        label = "SubtaskTextColor"
    )

    val shape = PosTheme.shapes.radiusSmall
    val stateDescriptionText = if (subtask.isCompleted) "Completed" else "Not completed"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = PosTheme.spacing.minTouchTarget)
            .background(PosTheme.colors.surface, shape)
            .border(PosTheme.borders.borderStandard, PosTheme.colors.outlineSubtle, shape)
            .testTag("subtask_row_${subtask.id}")
            .padding(horizontal = PosTheme.spacing.space1, vertical = PosTheme.spacing.space1)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Leading PosCheckbox with strict 48dp touch target
            PosCheckbox(
                checked = subtask.isCompleted,
                onCheckedChange = { onToggle(subtask) },
                contentDescription = "${subtask.title}, $stateDescriptionText",
                modifier = Modifier.testTag("subtask_checkbox_${subtask.id}")
            )

            // 2. Subtask Title with strike-through and dimming animation
            Text(
                text = subtask.title,
                style = PosTheme.typography.body,
                color = textColor,
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = PosTheme.spacing.space2)
                    .testTag("subtask_title_${subtask.id}")
            )

            // 3. Trailing Delete Action with strict 48dp touch target
            Box(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = PosTheme.spacing.minTouchTarget,
                        minHeight = PosTheme.spacing.minTouchTarget
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = { onDelete(subtask) }
                    )
                    .semantics {
                        contentDescription = "Delete subtask ${subtask.title}"
                    }
                    .testTag("subtask_delete_${subtask.id}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    style = PosTheme.typography.label.copy(
                        fontFamily = PosFonts.mono,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PosTheme.colors.textMuted
                )
            }
        }
    }
}
