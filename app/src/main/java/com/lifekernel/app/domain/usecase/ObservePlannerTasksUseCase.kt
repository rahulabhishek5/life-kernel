package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/**
 * Snapshot of tasks relevant to the daily planner view per PRD §4.1 and FR-PLAN-001/003/007.
 */
data class PlannerTasksSnapshot(
    val overdueTasks: List<Task> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val completedTodayTasks: List<Task> = emptyList(),
    val completedTodayCount: Int = 0,
    val totalTodayCount: Int = 0
)

/**
 * Use case to observe the reactive stream of planner tasks for a given date.
 */
class ObservePlannerTasksUseCase(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {

    operator fun invoke(date: LocalDate = clock.today()): Flow<PlannerTasksSnapshot> {
        val overdueFlow = taskRepository.observeOpenTasksDueOnOrBefore(date.minusDays(1))
        val todayFlow = taskRepository.observeOpenTasksForDate(date)
        val completedTasksFlow = taskRepository.observeCompletedTasksForDate(date)
        val completedCountFlow = taskRepository.observeCompletedCountForDate(date)

        return combine(
            overdueFlow,
            todayFlow,
            completedTasksFlow,
            completedCountFlow
        ) { overdue, today, completedTasks, completedCount ->
            PlannerTasksSnapshot(
                overdueTasks = overdue,
                todayTasks = today,
                completedTodayTasks = completedTasks,
                completedTodayCount = completedCount,
                totalTodayCount = today.size + completedCount
            )
        }
    }
}
