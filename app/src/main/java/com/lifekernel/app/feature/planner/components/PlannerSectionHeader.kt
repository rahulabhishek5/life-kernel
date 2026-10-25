package com.lifekernel.app.feature.planner.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Static section header for task groups (e.g., OVERDUE, TODAY) per design-system.md §8.1.
 */
@Composable
fun PlannerSectionHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    val color = if (isWarning) PosTheme.colors.stateWarning else PosTheme.colors.textMuted
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = PosTheme.spacing.space4,
                end = PosTheme.spacing.space4,
                top = PosTheme.spacing.space4,
                bottom = PosTheme.spacing.space2
            )
            .testTag("section_header_$title"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title ($count)".uppercase(),
            style = PosTheme.typography.label.copy(
                fontFamily = PosFonts.mono,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}

/**
 * Expandable section header for the DONE section per design-system.md §8.1.
 * Features 48dp minimum touch target and chevron rotation indicator.
 */
@Composable
fun PlannerCollapsibleSectionHeader(
    title: String,
    count: Int,
    isCollapsed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val chevron = if (isCollapsed) "▸" else "▾"
    val stateDesc = if (isCollapsed) "Collapsed" else "Expanded"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = PosTheme.spacing.minTouchTarget)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            )
            .semantics {
                role = Role.Button
                stateDescription = stateDesc
                contentDescription = "$title section, $count tasks, $stateDesc"
            }
            .padding(horizontal = PosTheme.spacing.space4)
            .testTag("collapsible_section_header_$title"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = chevron,
            style = PosTheme.typography.title.copy(fontFamily = PosFonts.mono),
            color = PosTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.width(PosTheme.spacing.space2))

        Text(
            text = "$title ($count)".uppercase(),
            style = PosTheme.typography.label.copy(
                fontFamily = PosFonts.mono,
                fontWeight = FontWeight.Bold
            ),
            color = PosTheme.colors.textMuted
        )
    }
}
