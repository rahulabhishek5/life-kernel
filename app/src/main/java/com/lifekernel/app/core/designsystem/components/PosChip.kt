package com.lifekernel.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
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
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Filter and tag chip component with 4dp radius and accent highlight when selected.
 */
@Composable
fun PosChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = PosTheme.shapes.radiusSmall

    val backgroundColor = if (selected) PosTheme.colors.accentSubtle else Color.Transparent
    val borderColor = if (selected) PosTheme.colors.accent else PosTheme.colors.outlineStrong
    val textColor = if (selected) PosTheme.colors.accent else PosTheme.colors.textPrimary

    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = PosTheme.spacing.minTouchTarget,
                minHeight = PosTheme.spacing.minTouchTarget
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .semantics {
                role = Role.Button
                this.selected = selected
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor)
                .border(PosTheme.borders.borderStandard, borderColor, shape)
                .padding(
                    horizontal = PosTheme.spacing.space3,
                    vertical = PosTheme.spacing.space2
                )
        ) {
            Text(
                text = text,
                style = PosTheme.typography.label,
                color = if (enabled) textColor else PosTheme.colors.textDisabled
            )
        }
    }
}
