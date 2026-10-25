package com.lifekernel.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Linear progress bar animating over motionStandard with surfaceVariant track and accent fill.
 */
@Composable
fun PosProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = Dp.Unspecified
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val reducedMotion = PosTheme.reducedMotion
    val animDuration = if (reducedMotion) 0 else PosTheme.motion.motionStandard

    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = animDuration),
        label = "PosProgressBarFill"
    )

    val shape = PosTheme.shapes.radiusSmall
    val actualHeight = if (height == Dp.Unspecified) {
        PosTheme.spacing.space1 + PosTheme.borders.borderStrong
    } else {
        height
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(actualHeight)
            .clip(shape)
            .background(PosTheme.colors.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(shape)
                .background(PosTheme.colors.accent)
        )
    }
}
