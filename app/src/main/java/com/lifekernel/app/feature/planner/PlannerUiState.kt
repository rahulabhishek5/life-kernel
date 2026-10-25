package com.lifekernel.app.feature.planner

import com.lifekernel.app.domain.model.Task

/**
 * UI State for the Daily Planner tasks view per PRD §8.2 and design-system.md §8.1.
 */
data class PlannerUiState(
    val dateHeader: String = "",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progress: Float = 0f,
    val overdueTasks: List<Task> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val doneTasks: List<Task> = emptyList(),
    val isDoneCollapsed: Boolean = true,
    val isLoading: Boolean = false
) {
    val isPlannedEmpty: Boolean
        get() = overdueTasks.isEmpty() && todayTasks.isEmpty()
}
