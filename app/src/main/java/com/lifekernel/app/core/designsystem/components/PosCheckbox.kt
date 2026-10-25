package com.lifekernel.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Neo-brutalist checkbox with 24dp visual box inside a 48dp minimum touch target.
 */
@Composable
fun PosCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val reducedMotion = PosTheme.reducedMotion
    val animDuration = if (reducedMotion) 0 else PosTheme.motion.motionFast

    val animatedColor by animateColorAsState(
        targetValue = if (checked) PosTheme.colors.accent else Color.Transparent,
        animationSpec = tween(durationMillis = animDuration),
        label = "PosCheckboxBackground"
    )

    val checkProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = animDuration),
        label = "PosCheckboxCheck"
    )

    val shape = PosTheme.shapes.radiusSmall
    val borderColor = if (checked) PosTheme.colors.accent else PosTheme.colors.outlineStrong
    val checkmarkColor = PosTheme.colors.onAccent

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
                onClick = { onCheckedChange(!checked) }
            )
            .semantics {
                role = Role.Checkbox
                stateDescription = if (checked) "Checked" else "Not checked"
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val boxSize = PosTheme.spacing.space6
        Box(
            modifier = Modifier
                .size(boxSize)
                .clip(shape)
                .background(if (enabled) animatedColor else PosTheme.colors.surfaceVariant)
                .border(
                    width = PosTheme.borders.borderStandard,
                    color = if (enabled) borderColor else PosTheme.colors.outlineSubtle,
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checkProgress > 0f) {
                val strokeWidth = PosTheme.borders.borderStrong
                Canvas(modifier = Modifier.size(boxSize)) {
                    val w = size.width
                    val h = size.height

                    val fullPath = Path().apply {
                        moveTo(w * 0.28f, h * 0.52f)
                        lineTo(w * 0.44f, h * 0.68f)
                        lineTo(w * 0.72f, h * 0.36f)
                    }

                    val pathMeasure = PathMeasure().apply {
                        setPath(fullPath, false)
                    }

                    val partialPath = Path()
                    pathMeasure.getSegment(0f, pathMeasure.length * checkProgress, partialPath, true)

                    drawPath(
                        path = partialPath,
                        color = checkmarkColor,
                        style = Stroke(
                            width = strokeWidth.toPx(),
                            cap = StrokeCap.Square,
                            join = StrokeJoin.Miter
                        )
                    )
                }
            }
        }
    }
}
