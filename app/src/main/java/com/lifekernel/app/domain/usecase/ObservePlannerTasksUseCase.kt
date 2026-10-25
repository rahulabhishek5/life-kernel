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
 * Combines overdue tasks (due before date), today's tasks (due on date + tasks without due date),
 * and tasks completed today.
 */
class ObservePlannerTasksUseCase(
    private val taskRepository: TaskRepository,
    private val clock: Clock
) {

    operator fun invoke(date: LocalDate = clock.today()): Flow<PlannerTasksSnapshot> {
        val overdueFlow = taskRepository.observeOpenTasksDueOnOrBefore(date.minusDays(1))
        val todayDueFlow = taskRepository.observeOpenTasksForDate(date)
        val noDueDateFlow = taskRepository.observeOpenTasksWithoutDueDate()
        val completedTasksFlow = taskRepository.observeCompletedTasksForDate(date)
        val completedCountFlow = taskRepository.observeCompletedCountForDate(date)

        return combine(
            overdueFlow,
            todayDueFlow,
            noDueDateFlow,
            completedTasksFlow,
            completedCountFlow
        ) { overdue, todayDue, noDueDate, completedTasks, completedCount ->
            val todayCombined = (todayDue + noDueDate)
                .distinctBy { it.id }
                .sortedByDescending { it.priority.code }

            PlannerTasksSnapshot(
                overdueTasks = overdue,
                todayTasks = todayCombined,
                completedTodayTasks = completedTasks,
                completedTodayCount = completedCount,
                totalTodayCount = todayCombined.size + completedCount
            )
        }
    }
}
