package com.lifekernel.app.feature.planner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifekernel.app.core.designsystem.components.PosEmptyState
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.core.time.DefaultClock
import com.lifekernel.app.di.LocalAppContainer
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.feature.planner.components.PlannerCollapsibleSectionHeader
import com.lifekernel.app.feature.planner.components.PlannerFilterBar
import com.lifekernel.app.feature.planner.components.PlannerHeader
import com.lifekernel.app.feature.planner.components.PlannerQuickAddButton
import com.lifekernel.app.feature.planner.components.PlannerSectionHeader
import com.lifekernel.app.feature.planner.components.PlannerTaskRow
import com.lifekernel.app.feature.planner.components.PlannerTopBar
import com.lifekernel.app.feature.planner.components.QuickAddSheet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Daily Planner (Home / Today) screen per PRD §8.2 and design-system.md §8.1.
 * Displays formatted date header, progress card, partitioned task lists:
 * Overdue, Today (open tasks), and a collapsed-by-default Done section,
 * accompanied by search/filter bar, global results view, and the Quick-Add capture sheet.
 */
@Composable
fun PlannerScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    onTaskClick: ((Task) -> Unit)? = null,
    viewModel: PlannerViewModel? = null
) {
    val container = if (viewModel == null) LocalAppContainer.current else null
    val resolvedViewModel = viewModel ?: remember(container) {
        requireNotNull(container).createPlannerViewModel()
    }
    val uiState by resolvedViewModel.uiState.collectAsStateWithLifecycle()
    val clock = container?.clock ?: remember { DefaultClock() }
    val reducedMotion = PosTheme.reducedMotion
    val currentQuickAddVisible by rememberUpdatedState(uiState.isQuickAddVisible)

    // Back button prioritizes dismissing search mode and clearing active filters
    BackHandler(enabled = uiState.isSearchActive || uiState.isFilterActive) {
        resolvedViewModel.onCloseSearch()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !currentQuickAddVisible) {
                resolvedViewModel.checkDayRollover()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PosTheme.colors.background)
            .testTag("planner_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PlannerTopBar(
                isSearchActive = uiState.isSearchActive,
                searchQuery = uiState.searchQuery,
                onOpenDrawer = onOpenDrawer,
                onOpenSearch = resolvedViewModel::onToggleSearch,
                onCloseSearch = resolvedViewModel::onCloseSearch,
                onSearchQueryChange = resolvedViewModel::onUpdateSearchQuery
            )

            // Horizontal Filter Bar: visible when search is active or any filter is applied
            if (uiState.isSearchActive || uiState.isFilterActive) {
                PlannerFilterBar(
                    selectedPriority = uiState.selectedPriorityFilter,
                    selectedTag = uiState.selectedTagFilter,
                    availableTags = uiState.availableFilterTags,
                    isFilterActive = uiState.isFilterActive,
                    onTogglePriority = resolvedViewModel::onTogglePriorityFilter,
                    onToggleTag = resolvedViewModel::onToggleTagFilter,
                    onClearAll = resolvedViewModel::onClearAllFilters
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("planner_task_list"),
                verticalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
            ) {
                if (uiState.isFilterActive) {
                    // Global Search / Filter Results across all dates
                    if (uiState.filteredTasks.isEmpty()) {
                        item(key = "planner_search_empty_state", contentType = "empty_state") {
                            PosEmptyState(
                                label = "NO MATCHING TASKS",
                                guidance = "Try adjusting your search query or filters.",
                                actionText = "CLEAR FILTERS",
                                onActionClick = resolvedViewModel::onClearAllFilters,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = PosTheme.spacing.space6)
                                    .testTag("planner_search_empty_state")
                            )
                        }
                    } else {
                        val today = clock.today()
                        items(
                            items = uiState.filteredTasks,
                            key = { "filtered_${it.id}" },
                            contentType = { "task_row" }
                        ) { task ->
                            val explicitBadge = computeExplicitDateBadge(task.dueDate, today)
                            val isOverdue = task.dueDate?.isBefore(today) == true
                            PlannerTaskRow(
                                task = task,
                                onToggle = { resolvedViewModel.onToggleTask(it, reducedMotion) },
                                onTaskClick = onTaskClick,
                                isOverdue = isOverdue,
                                explicitDateBadge = explicitBadge,
                                modifier = Modifier.padding(horizontal = PosTheme.spacing.space4)
                            )
                        }
                    }
                } else {
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
                                actionText = "+ ADD TASK",
                                onActionClick = resolvedViewModel::onOpenQuickAdd,
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
                                onToggle = { resolvedViewModel.onToggleTask(it, reducedMotion) },
                                onTaskClick = onTaskClick,
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
                                onToggle = { resolvedViewModel.onToggleTask(it, reducedMotion) },
                                onTaskClick = onTaskClick,
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
                                onToggle = resolvedViewModel::onToggleDoneSection
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
                                    onToggle = { resolvedViewModel.onToggleTask(it, reducedMotion) },
                                    onTaskClick = onTaskClick,
                                    isOverdue = false,
                                    modifier = Modifier.padding(horizontal = PosTheme.spacing.space4)
                                )
                            }
                        }
                    }
                }

                // Bottom breathing room (so items aren't obscured by FAB)
                item(key = "bottom_spacer") {
                    Spacer(
                        modifier = Modifier
                            .height(PosTheme.spacing.space12 + PosTheme.spacing.space8)
                            .navigationBarsPadding()
                    )
                }
            }
        }

        // Quick-Add Floating Action Button — hidden when screen is searching/filtering or empty
        if (!uiState.isFilterActive && !uiState.isPlannedEmpty) {
            PlannerQuickAddButton(
                onClick = resolvedViewModel::onOpenQuickAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
            )
        }

        // Modal Quick-Add Sheet
        QuickAddSheet(
            visible = uiState.isQuickAddVisible,
            title = uiState.quickAddTitle,
            titleError = uiState.quickAddTitleError,
            selectedDatePreset = uiState.quickAddDatePreset,
            customDate = uiState.quickAddCustomDate,
            selectedPriority = uiState.quickAddPriority,
            onTitleChange = resolvedViewModel::onQuickAddTitleChanged,
            onDatePresetSelect = resolvedViewModel::onQuickAddDatePresetSelected,
            onCustomDatePicked = resolvedViewModel::onQuickAddCustomDatePicked,
            onPrioritySelect = resolvedViewModel::onQuickAddPrioritySelected,
            onSave = { resolvedViewModel.onSaveQuickAdd() },
            onDismissRequest = resolvedViewModel::onDismissQuickAdd
        )
    }
}

private fun computeExplicitDateBadge(dueDate: LocalDate?, today: LocalDate): String {
    if (dueDate == null) return "NO DUE DATE"
    return when {
        dueDate.isBefore(today) -> "OVERDUE"
        dueDate.isEqual(today) -> "TODAY"
        else -> "DUE ${dueDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.US)).uppercase(Locale.US)}"
    }
}
