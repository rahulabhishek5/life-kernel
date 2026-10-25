package com.lifekernel.app.feature.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lifekernel.app.core.designsystem.components.PosEmptyState
import com.lifekernel.app.core.designsystem.components.PosTopBar
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.di.LocalAppContainer
import com.lifekernel.app.feature.planner.components.PlannerCollapsibleSectionHeader
import com.lifekernel.app.feature.planner.components.PlannerHeader
import com.lifekernel.app.feature.planner.components.PlannerSectionHeader
import com.lifekernel.app.feature.planner.components.PlannerTaskRow

/**
 * Daily Planner (Home / Today) screen per PRD §8.2 and design-system.md §8.1.
 * Displays formatted date header, progress card, and partitioned task lists:
 * Overdue, Today (open tasks), and a collapsed-by-default Done section.
 */
@Composable
fun PlannerScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlannerViewModel = LocalAppContainer.current.createPlannerViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkDayRollover()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PosTheme.colors.background)
            .testTag("planner_screen")
    ) {
        PosTopBar(
            title = "Today",
            onNavigationClick = onOpenDrawer
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .testTag("planner_task_list"),
            verticalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
        ) {
            // 1. Date Header & Progress Card
            item(key = "planner_header", contentType = "header") {
                PlannerHeader(
                    dateHeader = uiState.dateHeader,
                    completedCount = uiState.completedCount,
                    totalCount = uiState.totalCount,
                    progress = uiState.progress,
                    modifier = Modifier.padding(top = PosTheme.spacing.space2)
                )
            }

            // 2. Empty State (if both Overdue and Today are empty)
            if (uiState.isPlannedEmpty && !uiState.isLoading) {
                item(key = "planner_empty_state", contentType = "empty_state") {
                    PosEmptyState(
                        label = "NOTHING PLANNED",
                        guidance = "Add your first task for today.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = PosTheme.spacing.space6)
                            .testTag("planner_empty_state")
                    )
                }
            }

            // 3. Overdue Tasks Section (visible only if overdue tasks exist)
            if (uiState.overdueTasks.isNotEmpty()) {
                item(key = "header_overdue", contentType = "section_header") {
                    PlannerSectionHeader(
                        title = "OVERDUE",
                        count = uiState.overdueTasks.size,
                        isWarning = true
                    )
                }

                items(
                    items = uiState.overdueTasks,
                    key = { "overdue_${it.id}" },
                    contentType = { "task_row" }
                ) { task ->
                    PlannerTaskRow(
                        task = task,
                        onToggle = viewModel::onToggleTask,
                        isOverdue = true,
                        modifier = Modifier.padding(horizontal = PosTheme.spacing.space4)
                    )
                }
            }

            // 4. Today Tasks Section (open tasks due today or without due date)
            if (uiState.todayTasks.isNotEmpty()) {
                item(key = "header_today", contentType = "section_header") {
                    PlannerSectionHeader(
                        title = "TODAY",
                        count = uiState.todayTasks.size
                    )
                }

                items(
                    items = uiState.todayTasks,
                    key = { "today_${it.id}" },
                    contentType = { "task_row" }
                ) { task ->
                    PlannerTaskRow(
                        task = task,
                        onToggle = viewModel::onToggleTask,
                        isOverdue = false,
                        modifier = Modifier.padding(horizontal = PosTheme.spacing.space4)
                    )
                }
            }

            // 5. Done Tasks Section (collapsed by default with count)
            if (uiState.doneTasks.isNotEmpty()) {
                item(key = "header_done", contentType = "section_header") {
                    PlannerCollapsibleSectionHeader(
                        title = "DONE",
                        count = uiState.doneTasks.size,
                        isCollapsed = uiState.isDoneCollapsed,
                        onToggle = viewModel::onToggleDoneSection
                    )
                }

                if (!uiState.isDoneCollapsed) {
                    items(
                        items = uiState.doneTasks,
                        key = { "done_${it.id}" },
                        contentType = { "task_row" }
                    ) { task ->
                        PlannerTaskRow(
                            task = task,
                            onToggle = viewModel::onToggleTask,
                            isOverdue = false,
                            modifier = Modifier.padding(horizontal = PosTheme.spacing.space4)
                        )
                    }
                }
            }

            // Bottom breathing room
            item(key = "bottom_spacer") {
                Spacer(
                    modifier = Modifier
                        .height(PosTheme.spacing.space8)
                        .navigationBarsPadding()
                )
            }
        }
    }
}
