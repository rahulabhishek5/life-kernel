package com.lifekernel.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Edge-to-edge aware top app bar component per design-system.md §6 & §8.
 * Features an AMOLED black background, leading hamburger button with 48dp minimum touch target,
 * title using PosTypography.title, and optional trailing actions slot.
 */
@Composable
fun PosTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationClick: (() -> Unit)? = null,
    navigationIconContentDescription: String = "Open navigation drawer",
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PosTheme.colors.background)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = PosTheme.spacing.space4),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onNavigationClick != null) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val iconColor = PosTheme.colors.textPrimary

                    Box(
                        modifier = Modifier
                            .defaultMinSize(
                                minWidth = PosTheme.spacing.minTouchTarget,
                                minHeight = PosTheme.spacing.minTouchTarget
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onNavigationClick
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = navigationIconContentDescription
                            }
                            .testTag("top_bar_navigation_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier.size(width = 18.dp, height = 14.dp)
                        ) {
                            val barHeight = 2.dp.toPx()
                            // Top bar
                            drawRect(
                                color = iconColor,
                                topLeft = Offset(0f, 0f),
                                size = Size(size.width, barHeight)
                            )
                            // Middle bar
                            drawRect(
                                color = iconColor,
                                topLeft = Offset(0f, (size.height - barHeight) / 2),
                                size = Size(size.width, barHeight)
                            )
                            // Bottom bar
                            drawRect(
                                color = iconColor,
                                topLeft = Offset(0f, size.height - barHeight),
                                size = Size(size.width, barHeight)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(PosTheme.spacing.space2))
                }

                Text(
                    text = title,
                    style = PosTheme.typography.title,
                    color = PosTheme.colors.textPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("top_bar_title")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }

        // Subtle bottom divider separating top bar from scrolled content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PosTheme.colors.divider)
                .testTag("top_bar_divider")
        )
    }
}
