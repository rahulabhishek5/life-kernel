package com.lifekernel.app.feature.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskFilterCriteria
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.CreateTaskUseCase
import com.lifekernel.app.domain.usecase.ObserveAllTagsUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.PlannerTasksSnapshot
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import com.lifekernel.app.domain.usecase.SearchAndFilterTasksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class QuickAddInternalState(
    val isVisible: Boolean = false,
    val title: String = "",
    val titleError: String? = null,
    val datePreset: QuickAddDatePreset = QuickAddDatePreset.TODAY,
    val customDate: LocalDate? = null,
    val priority: TaskPriority = TaskPriority.NONE
)

private data class DailyPlannerState(
    val date: LocalDate,
    val snapshot: PlannerTasksSnapshot,
    val isDoneCollapsed: Boolean,
    val completingIds: Set<Long>,
    val quickAddState: QuickAddInternalState
)

private data class SearchControlsState(
    val isSearchActive: Boolean,
    val searchQuery: String,
    val selectedPriority: TaskPriority?,
    val selectedTag: String?
)

private data class SearchFilterState(
    val isSearchActive: Boolean,
    val searchQuery: String,
    val selectedPriority: TaskPriority?,
    val selectedTag: String?,
    val availableTags: List<String>,
    val filteredTasks: List<Task>
)

/**
 * ViewModel managing the Planner tasks view state per ARCHITECTURE.md §6 and PRD §8.2.
 * Collects reactive task streams, coordinates task completion/reopening, handles quick-add capture,
 * maintains search and multi-criteria filters with a 250ms debounce pipeline,
 * and re-evaluates the calendar date on resume/rollover without background polling.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PlannerViewModel(
    private val observePlannerTasksUseCase: ObservePlannerTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val reopenTaskUseCase: ReopenTaskUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val searchAndFilterTasksUseCase: SearchAndFilterTasksUseCase,
    private val observeAllTagsUseCase: ObserveAllTagsUseCase,
    private val clock: Clock,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE · d MMM uuuu", Locale.US)

    private val currentDateFlow = MutableStateFlow(clock.today())
    private val isDoneCollapsedFlow = MutableStateFlow(true)
    private val completingTaskIdsFlow = MutableStateFlow<Set<Long>>(emptySet())
    private val quickAddStateFlow = MutableStateFlow(QuickAddInternalState())

    // Search and filter state flows
    private val isSearchActiveFlow = MutableStateFlow(false)
    private val searchQueryFlow = MutableStateFlow("")
    private val selectedPriorityFlow = MutableStateFlow<TaskPriority?>(null)
    private val selectedTagFlow = MutableStateFlow<String?>(null)

    private val debouncedQueryFlow = searchQueryFlow.debounce { query ->
        if (query.isEmpty()) 0L else 250L
    }

    private val criteriaFlow: Flow<TaskFilterCriteria> = combine(
        debouncedQueryFlow,
        selectedPriorityFlow,
        selectedTagFlow
    ) { query, priority, tag ->
        TaskFilterCriteria(
            searchQuery = query,
            selectedPriority = priority,
            selectedTagName = tag
        )
    }

    private val filteredTasksFlow: Flow<List<Task>> = criteriaFlow.flatMapLatest { criteria ->
        if (criteria.isActive) {
            searchAndFilterTasksUseCase(criteria)
        } else {
            flowOf(emptyList())
        }
    }

    private val availableTagsFlow: Flow<List<String>> = observeAllTagsUseCase().map { tags ->
        tags.map { it.name }
    }

    private val searchControlsFlow: Flow<SearchControlsState> = combine(
        isSearchActiveFlow,
        searchQueryFlow,
        selectedPriorityFlow,
        selectedTagFlow
    ) { isSearchActive, query, priority, tag ->
        SearchControlsState(
            isSearchActive = isSearchActive,
            searchQuery = query,
            selectedPriority = priority,
            selectedTag = tag
        )
    }

    private val searchFilterStateFlow: Flow<SearchFilterState> = combine(
        searchControlsFlow,
        availableTagsFlow,
        filteredTasksFlow
    ) { controls, tags, filteredTasks ->
        SearchFilterState(
            isSearchActive = controls.isSearchActive,
            searchQuery = controls.searchQuery,
            selectedPriority = controls.selectedPriority,
            selectedTag = controls.selectedTag,
            availableTags = tags,
            filteredTasks = filteredTasks
        )
    }

    private val dailyPlannerStateFlow: Flow<DailyPlannerState> = combine(
        currentDateFlow,
        currentDateFlow.flatMapLatest { date -> observePlannerTasksUseCase(date) },
        isDoneCollapsedFlow,
        completingTaskIdsFlow,
        quickAddStateFlow
    ) { date, snapshot, isDoneCollapsed, completingIds, quickAddState ->
        DailyPlannerState(
            date = date,
            snapshot = snapshot,
            isDoneCollapsed = isDoneCollapsed,
            completingIds = completingIds,
            quickAddState = quickAddState
        )
    }

    val uiState: StateFlow<PlannerUiState> = combine(
        dailyPlannerStateFlow,
        searchFilterStateFlow
    ) { daily, search ->
        val snapshot = daily.snapshot
        val totalCount = snapshot.totalTodayCount
        val completedCount = snapshot.completedTodayCount
        val progress = if (totalCount > 0) {
            (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        // Tasks in transition (220ms delay) stay in todayTasks with strike-through before moving to Done
        val completingTasks = snapshot.completedTodayTasks.filter { it.id in daily.completingIds }
        val displayTodayTasks = (snapshot.todayTasks + completingTasks).distinctBy { it.id }
        val displayDoneTasks = snapshot.completedTodayTasks.filter { it.id !in daily.completingIds }

        PlannerUiState(
            dateHeader = formatDate(daily.date),
            completedCount = completedCount,
            totalCount = totalCount,
            progress = progress,
            overdueTasks = snapshot.overdueTasks,
            todayTasks = displayTodayTasks,
            doneTasks = displayDoneTasks,
            isDoneCollapsed = daily.isDoneCollapsed,
            isLoading = false,
            isQuickAddVisible = daily.quickAddState.isVisible,
            quickAddTitle = daily.quickAddState.title,
            quickAddTitleError = daily.quickAddState.titleError,
            quickAddDatePreset = daily.quickAddState.datePreset,
            quickAddCustomDate = daily.quickAddState.customDate,
            quickAddPriority = daily.quickAddState.priority,
            isSearchActive = search.isSearchActive,
            searchQuery = search.searchQuery,
            selectedPriorityFilter = search.selectedPriority,
            selectedTagFilter = search.selectedTag,
            availableFilterTags = search.availableTags,
            filteredTasks = search.filteredTasks
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlannerUiState(
            dateHeader = formatDate(clock.today()),
            isLoading = true
        )
    )

    fun onToggleSearch() {
        val next = !isSearchActiveFlow.value
        isSearchActiveFlow.value = next
        if (!next) {
            onClearAllFilters()
        }
    }

    fun onCloseSearch() {
        isSearchActiveFlow.value = false
        onClearAllFilters()
    }

    fun onUpdateSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun onTogglePriorityFilter(priority: TaskPriority) {
        selectedPriorityFlow.update { current ->
            if (current == priority) null else priority
        }
    }

    fun onToggleTagFilter(tagName: String) {
        val target = tagName.trim().removePrefix("#")
        selectedTagFlow.update { current ->
            if (current?.equals(target, ignoreCase = true) == true) null else target
        }
    }

    fun onClearAllFilters() {
        searchQueryFlow.value = ""
        selectedPriorityFlow.value = null
        selectedTagFlow.value = null
    }

    fun onToggleTask(task: Task, isReducedMotion: Boolean = false) {
        scope.launch {
            if (task.isCompleted) {
                reopenTaskUseCase(task.id)
            } else {
                val delayMs = if (isReducedMotion) 0L else 220L
                if (delayMs > 0) {
                    completingTaskIdsFlow.update { it + task.id }
                }
                completeTaskUseCase(task.id)
                if (delayMs > 0) {
                    delay(delayMs)
                    completingTaskIdsFlow.update { it - task.id }
                }
            }
        }
    }

    fun onToggleDoneSection() {
        isDoneCollapsedFlow.value = !isDoneCollapsedFlow.value
    }

    fun onOpenQuickAdd() {
        quickAddStateFlow.value = QuickAddInternalState(isVisible = true)
    }

    fun onDismissQuickAdd() {
        quickAddStateFlow.update { it.copy(isVisible = false) }
    }

    fun onQuickAddTitleChanged(title: String) {
        quickAddStateFlow.update {
            it.copy(
                title = title,
                titleError = if (it.titleError != null && title.isNotBlank()) null else it.titleError
            )
        }
    }

    fun onQuickAddDatePresetSelected(preset: QuickAddDatePreset) {
        quickAddStateFlow.update {
            it.copy(
                datePreset = preset,
                customDate = if (preset != QuickAddDatePreset.PICK_DATE) null else it.customDate
            )
        }
    }

    fun onQuickAddCustomDatePicked(date: LocalDate) {
        quickAddStateFlow.update {
            it.copy(
                datePreset = QuickAddDatePreset.PICK_DATE,
                customDate = date
            )
        }
    }

    fun onQuickAddPrioritySelected(priority: TaskPriority) {
        quickAddStateFlow.update { it.copy(priority = priority) }
    }

    fun onSaveQuickAdd(): Boolean {
        val current = quickAddStateFlow.value
        val trimmed = current.title.trim()
        if (trimmed.isEmpty()) {
            quickAddStateFlow.update { it.copy(titleError = "Title cannot be empty") }
            return false
        }
        if (trimmed.length > 200) {
            quickAddStateFlow.update { it.copy(titleError = "Title cannot exceed 200 characters") }
            return false
        }

        val dueDate = when (current.datePreset) {
            QuickAddDatePreset.TODAY -> clock.today()
            QuickAddDatePreset.TOMORROW -> clock.today().plusDays(1)
            QuickAddDatePreset.PICK_DATE -> current.customDate
        }

        val now = clock.nowInstant()
        val newTask = Task(
            title = trimmed,
            dueDate = dueDate,
            priority = current.priority,
            createdAt = now,
            updatedAt = now
        )

        scope.launch {
            createTaskUseCase(newTask)
        }

        // Dismiss and reset form
        quickAddStateFlow.value = QuickAddInternalState(isVisible = false)
        return true
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
