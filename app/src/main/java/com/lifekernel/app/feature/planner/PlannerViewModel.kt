package com.lifekernel.app.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ViewModel managing the Planner tasks view state per ARCHITECTURE.md §6 and PRD §8.2.
 * Collects reactive task streams, coordinates task completion/reopening, and re-evaluates
 * the calendar date on resume/rollover without background polling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModel(
    private val observePlannerTasksUseCase: ObservePlannerTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val reopenTaskUseCase: ReopenTaskUseCase,
    private val clock: Clock,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE · d MMM uuuu", Locale.US)

    private val currentDateFlow = MutableStateFlow(clock.today())
    private val isDoneCollapsedFlow = MutableStateFlow(true)

    val uiState: StateFlow<PlannerUiState> = combine(
        currentDateFlow,
        currentDateFlow.flatMapLatest { date -> observePlannerTasksUseCase(date) },
        isDoneCollapsedFlow
    ) { date, snapshot, isDoneCollapsed ->
        val totalCount = snapshot.totalTodayCount
        val completedCount = snapshot.completedTodayCount
        val progress = if (totalCount > 0) {
            (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        PlannerUiState(
            dateHeader = formatDate(date),
            completedCount = completedCount,
            totalCount = totalCount,
            progress = progress,
            overdueTasks = snapshot.overdueTasks,
            todayTasks = snapshot.todayTasks,
            doneTasks = snapshot.completedTodayTasks,
            isDoneCollapsed = isDoneCollapsed,
            isLoading = false
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlannerUiState(
            dateHeader = formatDate(clock.today()),
            isLoading = true
        )
    )

    fun onToggleTask(task: Task) {
        scope.launch {
            if (task.isCompleted) {
                reopenTaskUseCase(task.id)
            } else {
                completeTaskUseCase(task.id)
            }
        }
    }

    fun onToggleDoneSection() {
        isDoneCollapsedFlow.value = !isDoneCollapsedFlow.value
    }

    /**
     * Checks if the calendar date has advanced (e.g. crossing midnight or resuming the app).
     * Re-evaluates date-dependent query flows without continuous polling loops.
     */
    fun checkDayRollover() {
        val today = clock.today()
        if (currentDateFlow.value != today) {
            currentDateFlow.value = today
        }
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(dateFormatter).uppercase(Locale.US)
    }
}
