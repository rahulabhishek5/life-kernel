package com.lifekernel.app.feature.planner

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.domain.repository.TaskRepository
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PlannerViewModelTest {

    private lateinit var clock: FixedClock
    private lateinit var fakeRepository: PlannerFakeTaskRepository
    private lateinit var observePlannerTasksUseCase: ObservePlannerTasksUseCase
    private lateinit var completeTaskUseCase: CompleteTaskUseCase
    private lateinit var reopenTaskUseCase: ReopenTaskUseCase
    private lateinit var viewModel: PlannerViewModel

    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeRepository = PlannerFakeTaskRepository()
        observePlannerTasksUseCase = ObservePlannerTasksUseCase(fakeRepository, clock)
        completeTaskUseCase = CompleteTaskUseCase(fakeRepository)
        reopenTaskUseCase = ReopenTaskUseCase(fakeRepository)

        viewModel = PlannerViewModel(
            observePlannerTasksUseCase = observePlannerTasksUseCase,
            completeTaskUseCase = completeTaskUseCase,
            reopenTaskUseCase = reopenTaskUseCase,
            clock = clock,
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )
    }

    @Test
    fun initialState_whenEmpty_hasZeroCountsAndPlannedEmpty() = runBlocking {
        val state = viewModel.uiState.first { !it.isLoading }

        assertTrue(state.isPlannedEmpty)
        assertEquals(0, state.completedCount)
        assertEquals(0, state.totalCount)
        assertEquals(0f, state.progress, 0.001f)
        assertTrue(state.isDoneCollapsed)
        assertTrue(state.overdueTasks.isEmpty())
        assertTrue(state.todayTasks.isEmpty())
        assertTrue(state.doneTasks.isEmpty())
        assertEquals("SUN · 25 OCT 2026", state.dateHeader)
    }

    @Test
    fun taskPartitioning_partitionsOverdueTodayAndDoneCorrectly() = runBlocking {
        val overdueTask = Task(
            id = 1L,
            title = "Overdue task",
            dueDate = testDate.minusDays(2),
            status = TaskStatus.OPEN,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val todayTask = Task(
            id = 2L,
            title = "Today task",
            dueDate = testDate,
            status = TaskStatus.OPEN,
            priority = TaskPriority.HIGH,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val noDueDateTask = Task(
            id = 3L,
            title = "No due date task",
            dueDate = null,
            status = TaskStatus.OPEN,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val doneTask = Task(
            id = 4L,
            title = "Completed task",
            dueDate = testDate,
            status = TaskStatus.DONE,
            createdAt = testInstant,
            updatedAt = testInstant
        )

        fakeRepository.addTask(overdueTask)
        fakeRepository.addTask(todayTask)
        fakeRepository.addTask(noDueDateTask)
        fakeRepository.addTask(doneTask)
        fakeRepository.setCompletedCount(testDate, 1)

        val state = viewModel.uiState.first { it.todayTasks.size == 2 }

        assertFalse(state.isPlannedEmpty)
        assertEquals(1, state.overdueTasks.size)
        assertEquals(1L, state.overdueTasks.first().id)

        // Today section includes tasks due today + tasks without due date
        assertEquals(2, state.todayTasks.size)
        assertTrue(state.todayTasks.any { it.id == 2L })
        assertTrue(state.todayTasks.any { it.id == 3L })

        assertEquals(1, state.doneTasks.size)
        assertEquals(4L, state.doneTasks.first().id)

        assertEquals(1, state.completedCount)
        assertEquals(3, state.totalCount) // 2 open today + 1 completed
        assertEquals(1f / 3f, state.progress, 0.001f)
    }

    @Test
    fun toggleTask_callsCompleteWhenOpen_andReopenWhenDone() = runBlocking {
        val openTask = Task(
            id = 10L,
            title = "Task to complete",
            dueDate = testDate,
            status = TaskStatus.OPEN,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(openTask)

        // Toggle open task -> complete
        viewModel.onToggleTask(openTask)
        val updatedToDone = fakeRepository.getTaskById(10L)
        assertEquals(TaskStatus.DONE, updatedToDone?.status)

        // Toggle done task -> reopen
        viewModel.onToggleTask(updatedToDone!!)
        val updatedToOpen = fakeRepository.getTaskById(10L)
        assertEquals(TaskStatus.OPEN, updatedToOpen?.status)
    }

    @Test
    fun toggleDoneSection_switchesCollapseState() = runBlocking {
        val initial = viewModel.uiState.first { !it.isLoading }
        assertTrue(initial.isDoneCollapsed)

        viewModel.onToggleDoneSection()
        val expanded = viewModel.uiState.first { !it.isDoneCollapsed }
        assertFalse(expanded.isDoneCollapsed)

        viewModel.onToggleDoneSection()
        val collapsedAgain = viewModel.uiState.first { it.isDoneCollapsed }
        assertTrue(collapsedAgain.isDoneCollapsed)
    }

    @Test
    fun checkDayRollover_switchesToNewDateWhenMidnightCrossed() = runBlocking {
        val initial = viewModel.uiState.first { !it.isLoading }
        assertEquals("SUN · 25 OCT 2026", initial.dateHeader)

        // Advance clock across midnight to Oct 26
        clock.advanceBy(Duration.ofHours(24))

        viewModel.checkDayRollover()
        val rolledOver = viewModel.uiState.first { it.dateHeader == "MON · 26 OCT 2026" }
        assertEquals("MON · 26 OCT 2026", rolledOver.dateHeader)
    }
}

class PlannerFakeTaskRepository : TaskRepository {
    val tasks = mutableMapOf<Long, Task>()
    val completedCounts = mutableMapOf<LocalDate, Int>()
    private var nextId = 1L

    private val tasksFlow = MutableStateFlow<Map<Long, Task>>(emptyMap())

    fun notifyChanged() {
        tasksFlow.value = tasks.toMap()
    }

    fun addTask(task: Task) {
        tasks[task.id] = task
        notifyChanged()
    }

    fun setCompletedCount(date: LocalDate, count: Int) {
        completedCounts[date] = count
        notifyChanged()
    }

    override fun observeOpenTasksDueOnOrBefore(date: LocalDate): Flow<List<Task>> {
        return tasksFlow.map { map ->
            map.values.filter {
                it.status == TaskStatus.OPEN &&
                    !it.isSubtask &&
                    it.dueDate != null &&
                    it.dueDate!! <= date
            }.sortedBy { it.dueDate }
        }
    }

    override fun observeOpenTasksForDate(date: LocalDate): Flow<List<Task>> {
        return tasksFlow.map { map ->
            map.values.filter {
                it.status == TaskStatus.OPEN &&
                    !it.isSubtask &&
                    it.dueDate == date
            }.sortedByDescending { it.priority.code }
        }
    }

    override fun observeOpenTasksWithoutDueDate(): Flow<List<Task>> {
        return tasksFlow.map { map ->
            map.values.filter {
                it.status == TaskStatus.OPEN &&
                    !it.isSubtask &&
                    it.dueDate == null
            }.sortedByDescending { it.priority.code }
        }
    }

    override fun observeCompletedTasksForDate(date: LocalDate): Flow<List<Task>> {
        return tasksFlow.map { map ->
            map.values.filter {
                it.status == TaskStatus.DONE &&
                    it.dueDate == date
            }
        }
    }

    override fun observeCompletedCountForDate(date: LocalDate): Flow<Int> {
        return tasksFlow.map { completedCounts[date] ?: 0 }
    }

    override fun observeTaskById(id: Long): Flow<Task?> {
        return tasksFlow.map { it[id] }
    }

    override fun observeSubtasks(parentId: Long): Flow<List<Task>> {
        return tasksFlow.map { map ->
            map.values.filter { it.parentId == parentId }.sortedBy { it.sortOrder }
        }
    }

    override suspend fun getTaskById(id: Long): Task? = tasks[id]

    override suspend fun createTask(task: Task, tagIds: List<Long>): AppResult<Long> {
        val id = if (task.id == 0L) nextId++ else task.id
        val storedTask = task.copy(id = id)
        tasks[id] = storedTask
        notifyChanged()
        return AppResult.success(id)
    }

    override suspend fun updateTask(task: Task, tagIds: List<Long>?): AppResult<Unit> {
        tasks[task.id] = task
        notifyChanged()
        return AppResult.success(Unit)
    }

    override suspend fun deleteTask(taskId: Long): AppResult<Unit> {
        tasks.remove(taskId)
        notifyChanged()
        return AppResult.success(Unit)
    }

    override suspend fun completeTask(taskId: Long, occurrenceDate: LocalDate?): AppResult<Unit> {
        val task = tasks[taskId] ?: return AppResult.failure(AppError.NotFound("Task $taskId not found"))
        tasks[taskId] = task.copy(status = TaskStatus.DONE)
        val date = occurrenceDate ?: task.dueDate ?: LocalDate.now()
        completedCounts[date] = (completedCounts[date] ?: 0) + 1
        notifyChanged()
        return AppResult.success(Unit)
    }

    override suspend fun reopenTask(taskId: Long, occurrenceDate: LocalDate?): AppResult<Unit> {
        val task = tasks[taskId] ?: return AppResult.failure(AppError.NotFound("Task $taskId not found"))
        tasks[taskId] = task.copy(status = TaskStatus.OPEN)
        val date = occurrenceDate ?: task.dueDate ?: LocalDate.now()
        val current = completedCounts[date] ?: 0
        if (current > 0) completedCounts[date] = current - 1
        notifyChanged()
        return AppResult.success(Unit)
    }
}
