package com.lifekernel.app.feature.planner.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import com.lifekernel.app.core.designsystem.components.PosChip
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.domain.model.TaskPriority

/**
 * Horizontal scrollable filter chip bar for planner search and filtering
 * per PRD §8.2 (FR-PLAN-004..006) and design-system.md §6.
 *
 * Enforces minimum 48dp touch targets on all interactive chips and actions.
 */
@Composable
fun PlannerFilterBar(
    selectedPriority: TaskPriority?,
    selectedTag: String?,
    availableTags: List<String>,
    isFilterActive: Boolean,
    onTogglePriority: (TaskPriority) -> Unit,
    onToggleTag: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(
                horizontal = PosTheme.spacing.space4,
                vertical = PosTheme.spacing.space1
            )
            .testTag("planner_filter_bar"),
        horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clear all button (visible when any query or filter constraint is active)
        if (isFilterActive) {
            Box(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = PosTheme.spacing.minTouchTarget,
                        minHeight = PosTheme.spacing.minTouchTarget
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClearAll
                    )
                    .semantics {
                        role = Role.Button
                    }
                    .testTag("filter_clear_all")
                    .padding(horizontal = PosTheme.spacing.space2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CLEAR ALL",
                    style = PosTheme.typography.label.copy(
                        fontFamily = PosFonts.mono,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PosTheme.colors.accent
                )
            }
        }

        // Priority chips
        PosChip(
            text = "High !!",
            selected = selectedPriority == TaskPriority.HIGH,
            onClick = { onTogglePriority(TaskPriority.HIGH) },
            modifier = Modifier.testTag("filter_priority_high")
        )

        PosChip(
            text = "Med !",
            selected = selectedPriority == TaskPriority.MEDIUM,
            onClick = { onTogglePriority(TaskPriority.MEDIUM) },
            modifier = Modifier.testTag("filter_priority_medium")
        )

        PosChip(
            text = "Low ·",
            selected = selectedPriority == TaskPriority.LOW,
            onClick = { onTogglePriority(TaskPriority.LOW) },
            modifier = Modifier.testTag("filter_priority_low")
        )

        // Dynamic Tag chips
        availableTags.forEach { tagName ->
            val cleanTag = tagName.trim().removePrefix("#")
            val isSelected = selectedTag?.equals(cleanTag, ignoreCase = true) == true
            PosChip(
                text = "#$cleanTag",
                selected = isSelected,
                onClick = { onToggleTag(cleanTag) },
                modifier = Modifier.testTag("filter_tag_$cleanTag")
            )
        }
    }
}
