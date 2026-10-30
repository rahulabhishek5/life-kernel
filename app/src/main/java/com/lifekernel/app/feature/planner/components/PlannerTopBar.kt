package com.lifekernel.app.feature.planner.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.lifekernel.app.core.designsystem.components.PosTextField
import com.lifekernel.app.core.designsystem.components.PosTopBar
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Top bar for the Daily Planner supporting standard title mode and reactive search mode
 * per PRD §8.2 (FR-PLAN-004) and design-system.md §6.
 */
@Composable
fun PlannerTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isSearchActive) {
        PosTopBar(
            title = "Today",
            modifier = modifier,
            onNavigationClick = onOpenDrawer,
            actions = {
                val iconColor = PosTheme.colors.textPrimary
                Box(
                    modifier = Modifier
                        .defaultMinSize(
                            minWidth = PosTheme.spacing.minTouchTarget,
                            minHeight = PosTheme.spacing.minTouchTarget
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenSearch
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = "Search tasks"
                        }
                        .testTag("planner_search_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        val strokeWidth = 2.dp.toPx()
                        val radius = 5.5.dp.toPx()
                        val center = Offset(size.width * 0.42f, size.height * 0.42f)
                        drawCircle(
                            color = iconColor,
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )
                        val handleStart = Offset(
                            center.x + radius * 0.707f,
                            center.y + radius * 0.707f
                        )
                        drawLine(
                            color = iconColor,
                            start = handleStart,
                            end = Offset(size.width * 0.95f, size.height * 0.95f),
                            strokeWidth = strokeWidth
                        )
                    }
                }
            }
        )
    } else {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .zIndex(1f),
            color = PosTheme.colors.background,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
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
                        // Leading navigation back button
                        val iconColor = PosTheme.colors.textPrimary
                        Box(
                            modifier = Modifier
                                .defaultMinSize(
                                    minWidth = PosTheme.spacing.minTouchTarget,
                                    minHeight = PosTheme.spacing.minTouchTarget
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onCloseSearch
                                )
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Close search"
                                }
                                .testTag("top_bar_navigation_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(width = 18.dp, height = 14.dp)) {
                                val strokeWidth = 2.dp.toPx()
                                val midY = size.height / 2f
                                drawLine(
                                    color = iconColor,
                                    start = Offset(0f, midY),
                                    end = Offset(size.width, midY),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = iconColor,
                                    start = Offset(0f, midY),
                                    end = Offset(size.width * 0.45f, 0f),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = iconColor,
                                    start = Offset(0f, midY),
                                    end = Offset(size.width * 0.45f, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(PosTheme.spacing.space2))

                        // Inline Search PosTextField
                        PosTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = "Search tasks...",
                            modifier = Modifier.weight(1f),
                            inputModifier = Modifier
                                .focusRequester(focusRequester)
                                .testTag("planner_search_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            leadingIcon = {
                                val searchIconColor = PosTheme.colors.textMuted
                                Canvas(modifier = Modifier.size(16.dp)) {
                                    val strokeWidth = 2.dp.toPx()
                                    val radius = 5.dp.toPx()
                                    val center = Offset(size.width * 0.42f, size.height * 0.42f)
                                    drawCircle(
                                        color = searchIconColor,
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    val handleStart = Offset(
                                        center.x + radius * 0.707f,
                                        center.y + radius * 0.707f
                                    )
                                    drawLine(
                                        color = searchIconColor,
                                        start = handleStart,
                                        end = Offset(size.width * 0.95f, size.height * 0.95f),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            },
                            trailingIcon = {
                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(
                                            minWidth = PosTheme.spacing.minTouchTarget,
                                            minHeight = PosTheme.spacing.minTouchTarget
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                if (searchQuery.isNotEmpty()) {
                                                    onSearchQueryChange("")
                                                } else {
                                                    onCloseSearch()
                                                }
                                            }
                                        )
                                        .semantics {
                                            role = Role.Button
                                            contentDescription = "Clear search query"
                                        }
                                        .testTag("search_clear_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✕",
                                        style = PosTheme.typography.label.copy(
                                            fontFamily = PosFonts.mono,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PosTheme.colors.textMuted
                                    )
                                }
                            }
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
    }
}
