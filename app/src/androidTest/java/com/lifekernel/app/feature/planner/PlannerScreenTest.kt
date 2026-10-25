package com.lifekernel.app.feature.planner

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.LocalReducedMotion
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.repository.TaskRepository
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class PlannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var clock: FixedClock
    private lateinit var fakeRepository: PlannerFakeTaskRepository
    private lateinit var viewModel: PlannerViewModel

    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeRepository = PlannerFakeTaskRepository()
        viewModel = PlannerViewModel(
            observePlannerTasksUseCase = ObservePlannerTasksUseCase(fakeRepository, clock),
            completeTaskUseCase = CompleteTaskUseCase(fakeRepository),
            reopenTaskUseCase = ReopenTaskUseCase(fakeRepository),
            clock = clock
        )
    }

    @Test
    fun emptyState_rendersWhenNoTasksPlanned() {
        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify date header renders
        composeTestRule.onNodeWithTag("planner_date_header")
            .assertIsDisplayed()
            .assertTextEquals("SUN · 25 OCT 2026")

        // Progress text shows 0 / 0 DONE
        composeTestRule.onNodeWithTag("planner_progress_text")
            .assertIsDisplayed()
            .assertTextEquals("0 / 0 DONE")

        // Empty state is displayed
        composeTestRule.onNodeWithTag("planner_empty_state")
            .assertIsDisplayed()
            .assertTextContains("NOTHING PLANNED")
    }

    @Test
    fun taskList_rendersHeaderProgressAndSections() {
        val overdueTask = Task(
            id = 1L,
            title = "Pay electricity bill",
            dueDate = testDate.minusDays(1),
            priority = TaskPriority.HIGH,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val todayTask = Task(
            id = 2L,
            title = "Review pull request",
            dueDate = testDate,
            priority = TaskPriority.MEDIUM,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val doneTask = Task(
            id = 3L,
            title = "Morning stretch",
            dueDate = testDate,
            status = TaskStatus.DONE,
            createdAt = testInstant,
            updatedAt = testInstant
        )

        fakeRepository.addTask(overdueTask)
        fakeRepository.addTask(todayTask)
        fakeRepository.addTask(doneTask)
        fakeRepository.setCompletedCount(testDate, 1)

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify progress text (1 completed out of 2 today tasks = 1 / 2 DONE)
        composeTestRule.onNodeWithTag("planner_progress_text")
            .assertIsDisplayed()
            .assertTextEquals("1 / 2 DONE")

        // Overdue section renders
        composeTestRule.onNodeWithTag("section_header_OVERDUE")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_row_1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_priority_1")
            .assertIsDisplayed()
            .assertTextEquals("!!")

        // Today section renders
        composeTestRule.onNodeWithTag("section_header_TODAY")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_row_2")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_priority_2")
            .assertIsDisplayed()
            .assertTextEquals("!")

        // Done section header renders with count 1
        composeTestRule.onNodeWithTag("collapsible_section_header_DONE")
            .assertIsDisplayed()

        // Done task row starts collapsed (does not exist in composition)
        composeTestRule.onNodeWithTag("task_row_3")
            .assertDoesNotExist()
    }

    @Test
    fun doneSection_expandsAndCollapsesWhenTapped() {
        val doneTask = Task(
            id = 5L,
            title = "Walk the dog",
            dueDate = testDate,
            status = TaskStatus.DONE,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(doneTask)
        fakeRepository.setCompletedCount(testDate, 1)

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // Tap Done section header to expand
        composeTestRule.onNodeWithTag("collapsible_section_header_DONE")
            .performClick()
        composeTestRule.waitForIdle()

        // Done task row is now visible
        composeTestRule.onNodeWithTag("task_row_5")
            .assertIsDisplayed()

        // Tap again to collapse
        composeTestRule.onNodeWithTag("collapsible_section_header_DONE")
            .performClick()
        composeTestRule.waitForIdle()

        // Done task row is hidden again
        composeTestRule.onNodeWithTag("task_row_5")
            .assertDoesNotExist()
    }

    @Test
    fun taskCheckbox_meets48dpTouchTargetAndTogglesCompletion() {
        val openTask = Task(
            id = 10L,
            title = "Check touch target size",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(openTask)

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // Checkbox meets 48dp min touch target
        composeTestRule.onNodeWithTag("task_checkbox_10")
            .assertHeightIsAtLeast(48.dp)

        // Tapping checkbox toggles completion
        composeTestRule.onNodeWithTag("task_checkbox_10")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify task in repository is now DONE
        val updated = runBlocking { fakeRepository.getTaskById(10L) }
        assertEquals(TaskStatus.DONE, updated?.status)
    }

    @Test
    fun reducedMotion_completionAppliesInstantly() {
        val openTask = Task(
            id = 20L,
            title = "Reduced motion task",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(openTask)

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

        composeTestRule.onNodeWithTag("task_checkbox_20")
            .performClick()
        composeTestRule.waitForIdle()

        val taskInRepo = runBlocking { fakeRepository.getTaskById(20L) }
        assertEquals(TaskStatus.DONE, taskInRepo?.status)
    }
}

private class PlannerFakeTaskRepository : TaskRepository {
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
