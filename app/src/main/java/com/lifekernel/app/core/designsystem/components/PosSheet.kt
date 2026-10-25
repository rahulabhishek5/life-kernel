package com.lifekernel.app.core.designsystem.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Modal bottom sheet wrapper component per design-system.md §6 & §8.4.
 *
 * Adheres to warm neo-brutalist component language:
 * - 8dp top radius (radiusLarge)
 * - 1.5dp outlineStrong border
 * - surfaceVariant (#171717) background
 * - 60% alpha black scrim
 * - Centered tactile drag handle (32dp x 4dp, 2dp radius)
 * - Keyboard and navigation bar insets aware (imePadding, navigationBarsPadding)
 * - 220ms slide animation (motionEmphasized), or 0ms under LocalReducedMotion
 */
@Composable
fun PosSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val reducedMotion = PosTheme.reducedMotion
    val duration = if (reducedMotion) 0 else PosTheme.motion.motionEmphasized

    BackHandler(enabled = visible) {
        onDismissRequest()
    }

    val shape = PosTheme.shapes.radiusLarge.copy(
        bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
        bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pos_sheet_container")
            .semantics { paneTitle = "Modal Bottom Sheet" }
    ) {
        // Scrim (60% alpha over AMOLED black)
        if (visible) {
            val scrimAlpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                label = "sheet_scrim_fade"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PosTheme.colors.scrim.copy(alpha = PosTheme.colors.scrim.alpha * scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = "Dismiss sheet"
                    }
                    .testTag("pos_sheet_scrim")
            )
        }

        // Sheet sliding up from bottom
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = duration)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = duration)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .clip(shape)
                    .background(PosTheme.colors.surfaceVariant, shape)
                    .border(PosTheme.borders.borderStandard, PosTheme.colors.outlineStrong, shape)
                    .testTag("pos_sheet_surface")
                    .padding(vertical = PosTheme.spacing.space3)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PosTheme.colors.outlineStrong)
                        .testTag("pos_sheet_drag_handle")
                )

                Spacer(modifier = Modifier.height(PosTheme.spacing.space3))

                // Inner scrollable column with imePadding
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = PosTheme.spacing.space4),
                    content = content
                )
            }
        }
    }
}
