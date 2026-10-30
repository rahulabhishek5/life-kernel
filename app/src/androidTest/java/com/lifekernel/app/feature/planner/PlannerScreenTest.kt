package com.lifekernel.app.feature.planner

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.LocalReducedMotion
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.repository.TagRepository
import com.lifekernel.app.domain.repository.TaskRepository
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.CreateTaskUseCase
import com.lifekernel.app.domain.usecase.ObserveAllTagsUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import com.lifekernel.app.domain.usecase.SearchAndFilterTasksUseCase
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
    private lateinit var fakeTagRepository: PlannerFakeTagRepository
    private lateinit var viewModel: PlannerViewModel

    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeRepository = PlannerFakeTaskRepository()
        fakeTagRepository = PlannerFakeTagRepository()
        viewModel = PlannerViewModel(
            observePlannerTasksUseCase = ObservePlannerTasksUseCase(fakeRepository, clock),
            completeTaskUseCase = CompleteTaskUseCase(fakeRepository),
            reopenTaskUseCase = ReopenTaskUseCase(fakeRepository),
            createTaskUseCase = CreateTaskUseCase(fakeRepository, clock),
            searchAndFilterTasksUseCase = SearchAndFilterTasksUseCase(fakeRepository),
            observeAllTagsUseCase = ObserveAllTagsUseCase(fakeTagRepository),
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
        composeTestRule.onNodeWithTag("task_priority_1", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextEquals("!!")

        // Today section renders
        composeTestRule.onNodeWithTag("section_header_TODAY")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_row_2")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_priority_2", useUnmergedTree = true)
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

    @Test
    fun plannerScreen_rendersTaskTagsOnRow() {
        val taggedTask = Task(
            id = 50L,
            title = "Task with tags",
            dueDate = testDate,
            tags = listOf(Tag(id = 1L, name = "urgent", createdAt = testInstant)),
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(taggedTask)

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("task_tag_50_urgent", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("#urgent")
    }

    @Test
    fun plannerScreen_taskRow_withSubtasks_displaysSubtaskBadge() {
        val subtask1 = Task(id = 61L, title = "Step 1", status = TaskStatus.DONE, parentId = 60L, createdAt = testInstant, updatedAt = testInstant)
        val subtask2 = Task(id = 62L, title = "Step 2", status = TaskStatus.OPEN, parentId = 60L, createdAt = testInstant, updatedAt = testInstant)
        val subtask3 = Task(id = 63L, title = "Step 3", status = TaskStatus.OPEN, parentId = 60L, createdAt = testInstant, updatedAt = testInstant)
        val taskWithSubtasks = Task(
            id = 60L,
            title = "Task with checklist",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant,
            subtasks = listOf(subtask1, subtask2, subtask3)
        )
        fakeRepository.addTask(taskWithSubtasks)

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("task_subtask_badge_60", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("1/3")
    }

    @Test
    fun searchBar_expandsFromTopBarIcon_andRendersControls() {
        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // Tap search icon in top bar
        composeTestRule.onNodeWithTag("planner_search_button")
            .assertIsDisplayed()
            .performClick()

        // Inline search text field and filter bar are displayed
        composeTestRule.onNodeWithTag("planner_search_input")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("planner_filter_bar")
            .assertIsDisplayed()
    }

    @Test
    fun crossDateSearch_displaysFutureTasksMatchingQuery() {
        val futureTask = Task(
            id = 100L,
            title = "Annual Strategy Review",
            dueDate = testDate.plusDays(30),
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(futureTask)

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // Initially excluded from Today's planner view
        composeTestRule.onNodeWithTag("task_row_100").assertDoesNotExist()

        // Open search and type query
        composeTestRule.onNodeWithTag("planner_search_button").performClick()
        composeTestRule.onNodeWithTag("planner_search_input").performTextInput("Strategy")

        // Future task now appears in global search view with explicit due date badge
        composeTestRule.onNodeWithTag("task_row_100").assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_date_100").assertTextEquals("DUE 24 NOV")
    }

    @Test
    fun filterChips_filterTasksByPriority() {
        val highTask = Task(
            id = 101L,
            title = "Urgent Server Migration",
            priority = TaskPriority.HIGH,
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val lowTask = Task(
            id = 102L,
            title = "Clean desk",
            priority = TaskPriority.LOW,
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(highTask)
        fakeRepository.addTask(lowTask)

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("planner_search_button").performClick()

        // Filter by High Priority
        composeTestRule.onNodeWithTag("filter_priority_high").performClick()

        // Only high priority task is shown
        composeTestRule.onNodeWithTag("task_row_101").assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_row_102").assertDoesNotExist()

        // Deselect High Priority
        composeTestRule.onNodeWithTag("filter_priority_high").performClick()
        composeTestRule.onNodeWithTag("task_row_101").assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_row_102").assertIsDisplayed()
    }

    @Test
    fun clearFilters_resetsToFullPlannerView() {
        val task = Task(
            id = 103L,
            title = "Normal task",
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(task)

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithTag("planner_search_button").performClick()
        composeTestRule.onNodeWithTag("planner_search_input").performTextInput("NoMatchingTaskXYZ")

        // Empty state is displayed
        composeTestRule.onNodeWithTag("planner_search_empty_state").assertIsDisplayed()

        // Click "CLEAR FILTERS"
        composeTestRule.onNodeWithText("CLEAR FILTERS").performClick()

        // Resets back to full planner view showing normal task
        composeTestRule.onNodeWithTag("task_row_103").assertIsDisplayed()
    }

    @Test
    fun touchTargets_minimum48dpEnforcedOnSearchAndFilterControls() {
        fakeTagRepository.setTags(listOf(Tag(id = 1L, name = "testtag", createdAt = testInstant)))

        composeTestRule.setContent {
            PosTheme {
                PlannerScreen(
                    onOpenDrawer = {},
                    viewModel = viewModel
                )
            }
        }

        // Top bar search button
        composeTestRule.onNodeWithTag("planner_search_button")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
            .performClick()

        // Navigation back button & Clear button in search mode
        composeTestRule.onNodeWithTag("top_bar_navigation_button")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        composeTestRule.onNodeWithTag("search_clear_button")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        // Filter chips
        composeTestRule.onNodeWithTag("filter_priority_high")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        composeTestRule.onNodeWithTag("filter_priority_medium")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        composeTestRule.onNodeWithTag("filter_priority_low")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        composeTestRule.onNodeWithTag("filter_tag_testtag")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
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

    override fun observeAllTasks(): Flow<List<Task>> {
        return tasksFlow.map { map ->
            map.values.filter { !it.isSubtask }.sortedWith(
                compareBy<Task> { it.dueDate ?: LocalDate.MAX }
                    .thenByDescending { it.priority.code }
                    .thenBy { it.id }
            )
        }
    }

    override suspend fun getAllTasks(): List<Task> {
        return tasks.values.filter { !it.isSubtask }.sortedWith(
            compareBy<Task> { it.dueDate ?: LocalDate.MAX }
                .thenByDescending { it.priority.code }
                .thenBy { it.id }
        )
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
        val existing = tasks[task.id] ?: return AppResult.failure(AppError.NotFound("Task ${task.id} not found"))
        tasks[task.id] = task.copy(status = existing.status)
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

class PlannerFakeTagRepository : TagRepository {
    private val tagsFlow = MutableStateFlow<List<Tag>>(emptyList())

    fun setTags(tags: List<Tag>) {
        tagsFlow.value = tags
    }

    override fun observeAllTags(): Flow<List<Tag>> = tagsFlow
    override suspend fun getTagById(id: Long): Tag? = tagsFlow.value.find { it.id == id }
    override suspend fun getTagByName(name: String): Tag? = tagsFlow.value.find { it.name.equals(name, ignoreCase = true) }
    override suspend fun createTag(name: String): AppResult<Tag> {
        val tag = Tag(id = tagsFlow.value.size + 1L, name = name, createdAt = Instant.EPOCH)
        tagsFlow.value = tagsFlow.value + tag
        return AppResult.success(tag)
    }
    override suspend fun deleteTag(id: Long): AppResult<Unit> {
        tagsFlow.value = tagsFlow.value.filter { it.id != id }
        return AppResult.success(Unit)
    }
    override suspend fun deleteOrphanTags(): AppResult<Int> = AppResult.success(0)
    override fun observeTagsForTask(taskId: Long): Flow<List<Tag>> = MutableStateFlow(emptyList())
    override suspend fun getTagsForTask(taskId: Long): List<Tag> = emptyList()
    override suspend fun syncTaskTags(taskId: Long, tagNames: List<String>): AppResult<List<Tag>> = AppResult.success(emptyList())
}

