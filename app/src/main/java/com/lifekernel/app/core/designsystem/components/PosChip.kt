package com.lifekernel.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.lifekernel.app.core.designsystem.theme.PosFonts
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
    enabled: Boolean = true,
    inactiveBackgroundColor: Color = PosTheme.colors.surfaceVariant,
    selectedBackgroundColor: Color = PosTheme.colors.accentSubtle
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = PosTheme.shapes.radiusSmall
    val reducedMotion = PosTheme.reducedMotion
    val duration = if (reducedMotion) 0 else 160

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) selectedBackgroundColor else inactiveBackgroundColor,
        animationSpec = tween(durationMillis = duration),
        label = "PosChipBgColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) PosTheme.colors.accent else PosTheme.colors.outlineStrong,
        animationSpec = tween(durationMillis = duration),
        label = "PosChipBorderColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) PosTheme.colors.accent else PosTheme.colors.textPrimary,
        animationSpec = tween(durationMillis = duration),
        label = "PosChipTextColor"
    )

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

/**
 * Removable tag chip with mono font and dedicated 48dp touch-target remove action
 * per design-system.md §6 and PRD §8.4.
 */
@Composable
fun PosRemovableChip(
    tagText: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = PosTheme.shapes.radiusSmall
    val formattedText = if (tagText.startsWith("#")) tagText else "#$tagText"

    Row(
        modifier = modifier
            .clip(shape)
            .background(PosTheme.colors.surfaceVariant)
            .border(PosTheme.borders.borderStandard, PosTheme.colors.outlineStrong, shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formattedText,
            style = PosTheme.typography.label.copy(
                fontFamily = PosFonts.mono
            ),
            color = PosTheme.colors.textPrimary,
            modifier = Modifier.padding(
                start = PosTheme.spacing.space3,
                top = PosTheme.spacing.space2,
                bottom = PosTheme.spacing.space2,
                end = PosTheme.spacing.space1
            )
        )

        Box(
            modifier = Modifier
                .defaultMinSize(
                    minWidth = PosTheme.spacing.minTouchTarget,
                    minHeight = PosTheme.spacing.minTouchTarget
                )
                .clickable(
                    enabled = enabled,
                    onClick = onRemove,
                    role = Role.Button
                )
                .semantics {
                    contentDescription = "Remove tag $tagText"
                }
                .testTag("remove_tag_$tagText"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                style = PosTheme.typography.label.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = PosTheme.colors.textMuted
            )
        }
    }
}
