package com.lifekernel.app.core.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Neo-brutalist card container per design-system.md §6.
 *
 * Supports passive display, selection with accent border and offset block,
 * and tactile press-translation when [onClick] is provided.
 */
@Composable
fun PosCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val reducedMotion = PosTheme.reducedMotion
    val animDuration = if (reducedMotion) 0 else PosTheme.motion.motionFast

    val offsetDepth = PosTheme.borders.offsetShadow
    val hasOffsetBlock = isSelected

    val currentOffset by animateDpAsState(
        targetValue = if (isPressed && onClick != null) offsetDepth else PosTheme.spacing.space1 * 0,
        animationSpec = tween(durationMillis = animDuration),
        label = "PosCardOffset"
    )

    val shape = PosTheme.shapes.radiusMedium
    val borderColor = if (isSelected) PosTheme.colors.accent else PosTheme.colors.outlineSubtle
    val borderWidth = if (isSelected) PosTheme.borders.borderStrong else PosTheme.borders.borderStandard

    Box(
        modifier = modifier.padding(
            end = if (hasOffsetBlock) offsetDepth else PosTheme.spacing.space1 * 0,
            bottom = if (hasOffsetBlock) offsetDepth else PosTheme.spacing.space1 * 0
        )
    ) {
        // Offset shadow block for selected card
        if (hasOffsetBlock) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = offsetDepth, y = offsetDepth)
                    .clip(shape)
                    .background(PosTheme.colors.accent)
            )
        }

        // Foreground card
        Box(
            modifier = Modifier
                .offset(x = currentOffset, y = currentOffset)
                .clip(shape)
                .background(PosTheme.colors.surface)
                .border(borderWidth, borderColor, shape)
                .then(
                    if (onClick != null) {
                        Modifier
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onClick
                            )
                            .semantics {
                                role = Role.Button
                                if (isSelected) {
                                    this.selected = true
                                }
                            }
                    } else Modifier
                )
                .padding(PosTheme.spacing.space4),
            content = content
        )
    }
}
