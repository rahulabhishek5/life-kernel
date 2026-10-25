package com.lifekernel.app.core.designsystem.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Segmented toggle control for 2-3 options (e.g. Tasks vs. Timetable).
 */
@Composable
fun PosSegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = PosTheme.shapes.radiusMedium

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PosTheme.colors.surfaceVariant)
            .border(PosTheme.borders.borderStandard, PosTheme.colors.outlineStrong, shape)
            .padding(PosTheme.spacing.space1)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val segmentShape = PosTheme.shapes.radiusSmall
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(
                        minWidth = PosTheme.spacing.minTouchTarget,
                        minHeight = PosTheme.spacing.minTouchTarget
                    )
                    .clip(segmentShape)
                    .background(if (isSelected) PosTheme.colors.accent else Color.Transparent)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onOptionSelected(index) }
                    )
                    .semantics {
                        role = Role.Tab
                        selected = isSelected
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = PosTheme.typography.body.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isSelected) PosTheme.colors.onAccent else PosTheme.colors.textMuted
                )
            }
        }
    }
}
