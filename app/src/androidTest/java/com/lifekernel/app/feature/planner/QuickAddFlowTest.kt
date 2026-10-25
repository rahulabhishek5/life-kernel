package com.lifekernel.app.feature.planner

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.LocalReducedMotion
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.domain.repository.TaskRepository
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.CreateTaskUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Instrumented UI tests for Task T-011: Quick-Add sheet, SM-03 capture flow,
 * touch targets, validation, and completion transitions.
 */
@RunWith(AndroidJUnit4::class)
class QuickAddFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var clock: FixedClock
    private lateinit var fakeRepository: QuickAddFakeTaskRepository
    private lateinit var viewModel: PlannerViewModel

    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeRepository = QuickAddFakeTaskRepository()
        viewModel = PlannerViewModel(
            observePlannerTasksUseCase = ObservePlannerTasksUseCase(fakeRepository, clock),
            completeTaskUseCase = CompleteTaskUseCase(fakeRepository),
            reopenTaskUseCase = ReopenTaskUseCase(fakeRepository),
            createTaskUseCase = CreateTaskUseCase(fakeRepository, clock),
            clock = clock
        )
    }

    @Test
    fun twoInteractionCapture_createsAndRendersTaskImmediately() {
        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // Interaction 1: Tap Quick-Add FAB
        composeTestRule.onNodeWithTag("planner_quick_add_fab")
            .assertIsDisplayed()
            .performClick()

        // Verify sheet opened
        composeTestRule.onNodeWithTag("pos_sheet_surface")
            .assertIsDisplayed()

        // Interaction 2: Type title and press keyboard Done action
        composeTestRule.onNodeWithTag("quick_add_title_input")
            .performTextInput("Ship Milestone M3")
        composeTestRule.onNodeWithTag("quick_add_title_input")
            .performImeAction()

        composeTestRule.waitForIdle()

        // Verify task is persisted in repository with default date (Today)
        val created = fakeRepository.tasks.values.firstOrNull { it.title == "Ship Milestone M3" }
        assertNotNull(created)
        assertEquals(testDate, created?.dueDate)
        assertEquals(TaskPriority.NONE, created?.priority)

        // Verify rendered immediately in the Today list
        composeTestRule.onNodeWithText("Ship Milestone M3")
            .assertIsDisplayed()
    }

    @Test
    fun emptyTitle_showsInlineError_andKeepsSheetOpen() {
        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("planner_quick_add_fab")
            .performClick()

        // Tap SAVE without typing
        composeTestRule.onNodeWithTag("quick_add_save_button")
            .performClick()
        composeTestRule.waitForIdle()

        // Inline error is shown
        composeTestRule.onNodeWithText("Title cannot be empty")
            .assertIsDisplayed()

        // Sheet is still open and nothing was persisted
        composeTestRule.onNodeWithTag("pos_sheet_surface")
            .assertIsDisplayed()
        assertTrue(fakeRepository.tasks.isEmpty())
    }

    @Test
    fun datePresets_tomorrow_createsTaskWithTomorrowDate() {
        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("planner_quick_add_fab")
            .performClick()

        composeTestRule.onNodeWithTag("quick_add_title_input")
            .performTextInput("Tomorrow task")

        // Select Tomorrow preset chip
        composeTestRule.onNodeWithTag("quick_add_date_tomorrow")
            .performClick()

        composeTestRule.onNodeWithTag("quick_add_save_button")
            .performClick()
        composeTestRule.waitForIdle()

        val created = fakeRepository.tasks.values.firstOrNull { it.title == "Tomorrow task" }
        assertNotNull(created)
        assertEquals(testDate.plusDays(1), created?.dueDate)
    }

    @Test
    fun prioritySelection_high_createsTaskWithHighPriority() {
        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("planner_quick_add_fab")
            .performClick()

        composeTestRule.onNodeWithTag("quick_add_title_input")
            .performTextInput("Urgent fix")

        // Select High priority chip
        composeTestRule.onNodeWithTag("quick_add_priority_high")
            .performClick()

        composeTestRule.onNodeWithTag("quick_add_save_button")
            .performClick()
        composeTestRule.waitForIdle()

        val created = fakeRepository.tasks.values.firstOrNull { it.title == "Urgent fix" }
        assertNotNull(created)
        assertEquals(TaskPriority.HIGH, created?.priority)

        // Verify High priority symbol rendered
        composeTestRule.onNodeWithText("!!")
            .assertIsDisplayed()
    }

    @Test
    fun minimumTouchTargets_satisfy48dp() {
        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // FAB touch target
        composeTestRule.onNodeWithTag("planner_quick_add_fab")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
            .performClick()

        // Date chips touch targets
        composeTestRule.onNodeWithTag("quick_add_date_today")
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("quick_add_date_tomorrow")
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("quick_add_date_pick")
            .assertHeightIsAtLeast(48.dp)

        // Priority chips touch targets
        composeTestRule.onNodeWithTag("quick_add_priority_none")
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("quick_add_priority_low")
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("quick_add_priority_medium")
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("quick_add_priority_high")
            .assertHeightIsAtLeast(48.dp)

        // Save button touch target
        composeTestRule.onNodeWithTag("quick_add_save_button")
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun processRecreation_taskPersistsAcrossRecreation() {
        // Create task
        runBlocking {
            viewModel.onOpenQuickAdd()
            viewModel.onQuickAddTitleChanged("Persistent task")
            viewModel.onSaveQuickAdd()
        }

        // Simulate activity recreation by creating a fresh ViewModel connected to the same repository
        val recreatedViewModel = PlannerViewModel(
            observePlannerTasksUseCase = ObservePlannerTasksUseCase(fakeRepository, clock),
            completeTaskUseCase = CompleteTaskUseCase(fakeRepository),
            reopenTaskUseCase = ReopenTaskUseCase(fakeRepository),
            createTaskUseCase = CreateTaskUseCase(fakeRepository, clock),
            clock = clock
        )

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = recreatedViewModel
                )
            }
        }

        // Verify task is still present after recreation
        composeTestRule.onNodeWithText("Persistent task")
            .assertIsDisplayed()
    }

    @Test
    fun reducedMotion_sheetRendersInstantly() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                PosTheme {
                    PlannerScreen(
                        onOpenDrawer = {},
                        viewModel = viewModel
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("planner_quick_add_fab")
            .performClick()

        // Sheet is displayed immediately without waiting
        composeTestRule.onNodeWithTag("pos_sheet_surface")
            .assertIsDisplayed()
    }
}

private class QuickAddFakeTaskRepository : TaskRepository {
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
