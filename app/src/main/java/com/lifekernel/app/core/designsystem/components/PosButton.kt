package com.lifekernel.app.core.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.lifekernel.app.core.designsystem.theme.PosTheme

enum class PosButtonVariant {
    Primary,
    Secondary,
    Text,
    Destructive
}

/**
 * Warm neo-brutalist button component per design-system.md §6.
 *
 * Primary variant features a 3dp hard accent offset block that translates on press.
 * Minimum touch target of 48dp is strictly maintained.
 */
@Composable
fun PosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PosButtonVariant = PosButtonVariant.Primary,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val reducedMotion = PosTheme.reducedMotion
    val animDuration = if (reducedMotion) 0 else PosTheme.motion.motionFast

    val offsetDepth = PosTheme.borders.offsetShadow
    val currentOffset by animateDpAsState(
        targetValue = if (isPressed && enabled && variant == PosButtonVariant.Primary) offsetDepth else PosTheme.spacing.space1 * 0,
        animationSpec = tween(durationMillis = animDuration),
        label = "PosButtonOffset"
    )

    val shape = PosTheme.shapes.radiusMedium

    data class ButtonStyle(
        val backgroundColor: Color,
        val textColor: Color,
        val borderColor: Color,
        val borderWidth: androidx.compose.ui.unit.Dp
    )

    val style = when (variant) {
        PosButtonVariant.Primary -> {
            if (enabled) {
                ButtonStyle(
                    backgroundColor = PosTheme.colors.accent,
                    textColor = PosTheme.colors.onAccent,
                    borderColor = PosTheme.colors.background,
                    borderWidth = PosTheme.borders.borderStrong
                )
            } else {
                ButtonStyle(
                    backgroundColor = PosTheme.colors.surfaceVariant,
                    textColor = PosTheme.colors.textDisabled,
                    borderColor = PosTheme.colors.outlineSubtle,
                    borderWidth = PosTheme.borders.borderStandard
                )
            }
        }
        PosButtonVariant.Secondary -> {
            if (enabled) {
                ButtonStyle(
                    backgroundColor = Color.Transparent,
                    textColor = PosTheme.colors.textPrimary,
                    borderColor = PosTheme.colors.outlineStrong,
                    borderWidth = PosTheme.borders.borderStandard
                )
            } else {
                ButtonStyle(
                    backgroundColor = Color.Transparent,
                    textColor = PosTheme.colors.textDisabled,
                    borderColor = PosTheme.colors.outlineSubtle,
                    borderWidth = PosTheme.borders.borderStandard
                )
            }
        }
        PosButtonVariant.Destructive -> {
            if (enabled) {
                ButtonStyle(
                    backgroundColor = Color.Transparent,
                    textColor = PosTheme.colors.stateError,
                    borderColor = PosTheme.colors.stateError,
                    borderWidth = PosTheme.borders.borderStandard
                )
            } else {
                ButtonStyle(
                    backgroundColor = Color.Transparent,
                    textColor = PosTheme.colors.textDisabled,
                    borderColor = PosTheme.colors.outlineSubtle,
                    borderWidth = PosTheme.borders.borderStandard
                )
            }
        }
        PosButtonVariant.Text -> {
            if (enabled) {
                ButtonStyle(
                    backgroundColor = Color.Transparent,
                    textColor = PosTheme.colors.accent,
                    borderColor = Color.Transparent,
                    borderWidth = androidx.compose.ui.unit.Dp.Hairline * 0
                )
            } else {
                ButtonStyle(
                    backgroundColor = Color.Transparent,
                    textColor = PosTheme.colors.textDisabled,
                    borderColor = Color.Transparent,
                    borderWidth = androidx.compose.ui.unit.Dp.Hairline * 0
                )
            }
        }
    }

    Box(
        modifier = modifier
            .defaultMinSize(
                minWidth = PosTheme.spacing.minTouchTarget,
                minHeight = PosTheme.spacing.minTouchTarget
            )
            .padding(
                end = if (variant == PosButtonVariant.Primary && enabled) offsetDepth else PosTheme.spacing.space1 * 0,
                bottom = if (variant == PosButtonVariant.Primary && enabled) offsetDepth else PosTheme.spacing.space1 * 0
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .semantics { role = Role.Button }
    ) {
        // Offset block behind primary button (neo-brutalist tactile depth)
        if (variant == PosButtonVariant.Primary && enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = offsetDepth, y = offsetDepth)
                    .clip(shape)
                    .background(PosTheme.colors.accent)
                    .border(PosTheme.borders.borderStandard, PosTheme.colors.outlineStrong, shape)
            )
        }

        // Foreground button
        Box(
            modifier = Modifier
                .offset(x = currentOffset, y = currentOffset)
                .clip(shape)
                .background(style.backgroundColor)
                .then(
                    if (style.borderWidth > PosTheme.borders.borderHairline * 0) {
                        Modifier.border(style.borderWidth, style.borderColor, shape)
                    } else Modifier
                )
                .defaultMinSize(
                    minWidth = PosTheme.spacing.minTouchTarget,
                    minHeight = PosTheme.spacing.minTouchTarget
                )
                .padding(horizontal = PosTheme.spacing.space4, vertical = PosTheme.spacing.space3),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = PosTheme.typography.body,
                color = style.textColor
            )
        }
    }
}
