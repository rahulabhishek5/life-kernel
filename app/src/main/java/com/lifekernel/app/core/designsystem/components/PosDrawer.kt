package com.lifekernel.app.core.designsystem.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme

enum class PosDrawerValue {
    Closed,
    Open
}

/**
 * State holder for the modal navigation drawer.
 */
@Stable
class PosDrawerState(
    initialValue: PosDrawerValue = PosDrawerValue.Closed
) {
    var currentValue by mutableStateOf(initialValue)
        private set

    val isOpen: Boolean get() = currentValue == PosDrawerValue.Open
    val isClosed: Boolean get() = currentValue == PosDrawerValue.Closed

    fun open() {
        currentValue = PosDrawerValue.Open
    }

    fun close() {
        currentValue = PosDrawerValue.Closed
    }

    companion object {
        val Saver: Saver<PosDrawerState, String> = Saver(
            save = { it.currentValue.name },
            restore = { PosDrawerState(PosDrawerValue.valueOf(it)) }
        )
    }
}

@Composable
fun rememberPosDrawerState(initialValue: PosDrawerValue = PosDrawerValue.Closed): PosDrawerState {
    return rememberSaveable(saver = PosDrawerState.Saver) {
        PosDrawerState(initialValue)
    }
}

/**
 * Modal navigation drawer component per design-system.md §6, §7 & §8.3.
 * Slides from the left with a width of ~80% screen width (capped at 320dp).
 * Animated over 220ms (motionEmphasized), or 0ms under reduced motion.
 */
@Composable
fun PosDrawer(
    drawerState: PosDrawerState,
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reducedMotion = PosTheme.reducedMotion
    val duration = if (reducedMotion) 0 else PosTheme.motion.motionEmphasized

    BackHandler(enabled = drawerState.isOpen) {
        drawerState.close()
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        // Scrim (60% alpha over black)
        if (drawerState.isOpen) {
            val scrimAlpha by animateFloatAsState(
                targetValue = if (drawerState.isOpen) 1f else 0f,
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                label = "drawer_scrim_fade"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PosTheme.colors.scrim.copy(alpha = PosTheme.colors.scrim.alpha * scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { drawerState.close() }
                    )
                    .semantics {
                        role = Role.Button
                        contentDescription = "Close navigation drawer"
                    }
                    .testTag("drawer_scrim")
            )
        }

        // Animated Drawer Sheet
        AnimatedVisibility(
            visible = drawerState.isOpen,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(durationMillis = duration)),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(durationMillis = duration)),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(PosTheme.colors.surface)
                    .testTag("drawer_sheet")
            ) {
                val maxDrawerWidth = 320.dp
                val calculatedWidth = maxWidth * 0.8f
                val finalWidth = if (calculatedWidth > maxDrawerWidth) maxDrawerWidth else calculatedWidth

                Box(
                    modifier = Modifier
                        .width(finalWidth)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PosTheme.colors.surface)
                    ) {
                        drawerContent()
                    }
                }
            }
        }
    }
}

/**
 * Top header of the navigation drawer with app title and version per design-system.md §8.3.
 */
@Composable
fun PosDrawerHeader(
    modifier: Modifier = Modifier,
    appName: String = "Life Kernel",
    version: String = "v0.1"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = PosTheme.spacing.space4,
                end = PosTheme.spacing.space4,
                top = PosTheme.spacing.space4,
                bottom = PosTheme.spacing.space2
            )
    ) {
        Text(
            text = appName,
            style = PosTheme.typography.title,
            color = PosTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(PosTheme.spacing.space1))
        Text(
            text = version,
            style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
            color = PosTheme.colors.textMuted
        )
    }
}

/**
 * Individual navigation drawer item per design-system.md §8.3.
 * Selected item renders with accentSubtle background, a 3dp accent edge bar, and textPrimary.
 * Unselected items render with transparent background and textPrimary.
 */
@Composable
fun PosDrawerItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = if (isSelected) PosTheme.colors.accentSubtle else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = PosTheme.spacing.minTouchTarget)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                selected = isSelected
                contentDescription = title
            }
            .testTag("drawer_item_$title"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 3dp left edge indicator bar for selected item
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(PosTheme.borders.offsetShadow) // 3dp
                    .height(PosTheme.spacing.minTouchTarget)
                    .background(PosTheme.colors.accent)
            )
        } else {
            Spacer(modifier = Modifier.width(PosTheme.borders.offsetShadow))
        }

        Spacer(modifier = Modifier.width(PosTheme.spacing.space4 - PosTheme.borders.offsetShadow))

        Text(
            text = title,
            style = PosTheme.typography.body,
            color = PosTheme.colors.textPrimary
        )
    }
}

/**
 * Divider for separating drawer groups per design-system.md §8.3.
 */
@Composable
fun PosDrawerDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = PosTheme.spacing.space2),
        thickness = PosTheme.borders.borderHairline,
        color = PosTheme.colors.divider
    )
}
