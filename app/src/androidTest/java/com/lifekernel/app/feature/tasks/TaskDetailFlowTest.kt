package com.lifekernel.app.feature.tasks

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.domain.repository.TaskRepository
import com.lifekernel.app.domain.usecase.DeleteTaskUseCase
import com.lifekernel.app.domain.usecase.GetTaskByIdUseCase
import com.lifekernel.app.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Instrumented UI tests for task detail, inline editing, and deletion confirmation flows
 * per AC-TASK, FR-TASK-001..003, and design-system.md §6.
 */
@RunWith(AndroidJUnit4::class)
class TaskDetailFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var clock: FixedClock
    private lateinit var fakeRepository: TaskDetailFakeTaskRepository
    private lateinit var getTaskByIdUseCase: GetTaskByIdUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase

    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeRepository = TaskDetailFakeTaskRepository()
        getTaskByIdUseCase = GetTaskByIdUseCase(fakeRepository)
        updateTaskUseCase = UpdateTaskUseCase(fakeRepository, clock)
        deleteTaskUseCase = DeleteTaskUseCase(fakeRepository)
    }

    private fun createViewModel(taskId: Long): TaskDetailViewModel {
        return TaskDetailViewModel(
            taskId = taskId,
            getTaskByIdUseCase = getTaskByIdUseCase,
            updateTaskUseCase = updateTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            clock = clock
        )
    }

    @Test
    fun taskDetail_displaysTaskAttributesAndStatusReadout() {
        val task = Task(
            id = 1L,
            title = "Buy groceries",
            description = "Milk, eggs, bread",
            priority = TaskPriority.HIGH,
            dueDate = testDate,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(task)

        val viewModel = createViewModel(1L)

        composeTestRule.setContent {
            PosTheme {
                TaskDetailScreen(
                    taskId = 1L,
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        // Screen is displayed
        composeTestRule.onNodeWithTag("task_detail_screen").assertIsDisplayed()

        // Status readout shows OPEN
        composeTestRule.onNodeWithTag("task_detail_status_readout")
            .assertIsDisplayed()
            .assertTextContains("OPEN")

        // Title and description match
        composeTestRule.onNodeWithTag("task_detail_title_input")
            .assertIsDisplayed()
            .assertTextEquals("Buy groceries")

        composeTestRule.onNodeWithTag("task_detail_description_input")
            .assertIsDisplayed()
            .assertTextEquals("Milk, eggs, bread")

        // High priority chip is selected
        composeTestRule.onNodeWithTag("priority_chip_HIGH")
            .assertIsDisplayed()
            .assertIsSelected()
    }

    @Test
    fun taskDetail_editTitle_persistsOnSave() {
        val task = Task(
            id = 2L,
            title = "Old task title",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(task)

        var backInvoked = false
        val viewModel = createViewModel(2L)

        composeTestRule.setContent {
            PosTheme {
                TaskDetailScreen(
                    taskId = 2L,
                    onBack = { backInvoked = true },
                    viewModel = viewModel
                )
            }
        }

        // Edit title
        composeTestRule.onNodeWithTag("task_detail_title_input")
            .performTextClearance()
        composeTestRule.onNodeWithTag("task_detail_title_input")
            .performTextInput("Buy organic groceries")
        composeTestRule.waitForIdle()

        // Tap Save button
        composeTestRule.onNodeWithTag("task_detail_save_button")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify back was invoked after successful save
        assertTrue("Back must be invoked after saving", backInvoked)

        // Verify title in repository was updated
        val updated = runBlocking { fakeRepository.getTaskById(2L) }
        assertNotNull(updated)
        assertEquals("Buy organic groceries", updated?.title)
    }

    @Test
    fun taskDetail_deleteConfirmationDialog_explicitlyNamesTask() {
        val task = Task(
            id = 3L,
            title = "Prepare tax return",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(task)

        var backInvoked = false
        val viewModel = createViewModel(3L)

        composeTestRule.setContent {
            PosTheme {
                TaskDetailScreen(
                    taskId = 3L,
                    onBack = { backInvoked = true },
                    viewModel = viewModel
                )
            }
        }

        // Tap Delete button
        composeTestRule.onNodeWithTag("task_detail_delete_button")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify confirmation dialog is displayed
        composeTestRule.onNodeWithTag("pos_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pos_dialog_title").assertTextEquals("DELETE TASK")

        // AC-TASK Rule: The message must explicitly name the task being deleted
        composeTestRule.onNodeWithTag("pos_dialog_body")
            .assertTextEquals("Are you sure you want to delete 'Prepare tax return'?")

        // Verify action buttons exist and maintain minimum 48dp touch target
        composeTestRule.onNodeWithTag("pos_dialog_cancel_button")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)

        composeTestRule.onNodeWithTag("pos_dialog_confirm_button")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)

        // Tap Cancel
        composeTestRule.onNodeWithTag("pos_dialog_cancel_button")
            .performClick()
        composeTestRule.waitForIdle()

        // Dialog dismissed, task still exists
        composeTestRule.onNodeWithTag("pos_dialog").assertDoesNotExist()
        val stillExists = runBlocking { fakeRepository.getTaskById(3L) }
        assertNotNull("Task must not be deleted on cancel", stillExists)
        assertEquals(false, backInvoked)
    }

    @Test
    fun taskDetail_deleteConfirmationDialog_withSubtasks_notesSubtasksWillBeDeleted() {
        val subtask1 = Task(id = 11L, title = "Sub 1", parentId = 4L, createdAt = testInstant, updatedAt = testInstant)
        val subtask2 = Task(id = 12L, title = "Sub 2", parentId = 4L, createdAt = testInstant, updatedAt = testInstant)
        val task = Task(
            id = 4L,
            title = "Launch website",
            createdAt = testInstant,
            updatedAt = testInstant,
            subtasks = listOf(subtask1, subtask2)
        )
        fakeRepository.addTask(task)

        var backInvoked = false
        val viewModel = createViewModel(4L)

        composeTestRule.setContent {
            PosTheme {
                TaskDetailScreen(
                    taskId = 4L,
                    onBack = { backInvoked = true },
                    viewModel = viewModel
                )
            }
        }

        // Tap Delete button
        composeTestRule.onNodeWithTag("task_detail_delete_button")
            .performClick()
        composeTestRule.waitForIdle()

        // AC-TASK Rule: If subtasks exist, dialog must note that subtasks will also be deleted
        composeTestRule.onNodeWithTag("pos_dialog_body")
            .assertTextEquals("Are you sure you want to delete 'Launch website'? 2 subtasks will also be deleted.")

        // Confirm deletion
        composeTestRule.onNodeWithTag("pos_dialog_confirm_button")
            .performClick()
        composeTestRule.waitForIdle()

        // Dialog dismissed, task removed from repository, back invoked
        composeTestRule.onNodeWithTag("pos_dialog").assertDoesNotExist()
        val deleted = runBlocking { fakeRepository.getTaskById(4L) }
        assertNull("Task must be removed from repository after confirmed delete", deleted)
        assertTrue("Back must be invoked after confirmed delete", backInvoked)
    }

    @Test
    fun taskDetail_tagPicker_createTagAndRemoveTag() {
        val task = Task(
            id = 5L,
            title = "Task with tags",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(task)

        val viewModel = createViewModel(5L)

        composeTestRule.setContent {
            PosTheme {
                TaskDetailScreen(
                    taskId = 5L,
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        // Initially no tags
        composeTestRule.onNodeWithTag("no_tags_text").assertIsDisplayed()

        // Type tag name
        composeTestRule.onNodeWithTag("tag_picker_input")
            .performTextInput("urgent")
        composeTestRule.waitForIdle()

        // Create chip appears
        composeTestRule.onNodeWithTag("create_tag_chip")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Remove tag action is displayed and has at least 48dp minimum touch target
        composeTestRule.onNodeWithTag("remove_tag_urgent")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)

        // Tap remove button
        composeTestRule.onNodeWithTag("remove_tag_urgent")
            .performClick()
        composeTestRule.waitForIdle()

        // Tag chip removed
        composeTestRule.onNodeWithTag("remove_tag_urgent").assertDoesNotExist()
        composeTestRule.onNodeWithTag("no_tags_text").assertIsDisplayed()
    }

    @Test
    fun taskDetail_subtasks_addToggleDeleteAnd48dpTouchTargets() {
        val task = Task(
            id = 6L,
            title = "Task with checklist",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(task)

        val viewModel = createViewModel(6L)

        composeTestRule.setContent {
            PosTheme {
                TaskDetailScreen(
                    taskId = 6L,
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        // Header initially shows "SUBTASKS"
        composeTestRule.onNodeWithTag("task_detail_subtasks_header")
            .assertIsDisplayed()
            .assertTextContains("SUBTASKS")

        // Input a subtask title and add via button
        composeTestRule.onNodeWithTag("task_detail_subtask_input")
            .performTextInput("Write unit tests")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("task_detail_subtask_add_button")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Subtask item is rendered
        val subtask = viewModel.uiState.value.subtasks.first()
        val subtaskId = subtask.id

        composeTestRule.onNodeWithTag("subtask_row_$subtaskId")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("subtask_title_$subtaskId")
            .assertTextEquals("Write unit tests")

        // Verify fractional progress header: "SUBTASKS: 0 / 1"
        composeTestRule.onNodeWithTag("task_detail_subtasks_header")
            .assertTextContains("SUBTASKS: 0 / 1")

        // Verify 48dp minimum touch target for checkbox and delete action
        composeTestRule.onNodeWithTag("subtask_checkbox_$subtaskId")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        composeTestRule.onNodeWithTag("subtask_delete_$subtaskId")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)

        // Toggle checkbox
        composeTestRule.onNodeWithTag("subtask_checkbox_$subtaskId")
            .performClick()
        composeTestRule.waitForIdle()

        // Header updates to "SUBTASKS: 1 / 1"
        composeTestRule.onNodeWithTag("task_detail_subtasks_header")
            .assertTextContains("SUBTASKS: 1 / 1")
        composeTestRule.onNodeWithTag("task_detail_subtasks_progress_bar")
            .assertIsDisplayed()

        // Delete subtask
        composeTestRule.onNodeWithTag("subtask_delete_$subtaskId")
            .performClick()
        composeTestRule.waitForIdle()

        // Subtask disappears
        composeTestRule.onNodeWithTag("subtask_row_$subtaskId")
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag("task_detail_subtasks_header")
            .assertTextEquals("SUBTASKS")
    }

    @Test
    fun taskDetail_subtasks_keyboardImeActionAddsSubtask() {
        val task = Task(
            id = 7L,
            title = "Task with IME action",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.addTask(task)

        val viewModel = createViewModel(7L)

        composeTestRule.setContent {
            PosTheme {
                TaskDetailScreen(
                    taskId = 7L,
                    onBack = {},
                    viewModel = viewModel
                )
            }
        }

        // Type subtask and press keyboard ImeAction.Done
        composeTestRule.onNodeWithTag("task_detail_subtask_input")
            .performTextInput("Quick added via keyboard")
        composeTestRule.onNodeWithTag("task_detail_subtask_input")
            .performImeAction()
        composeTestRule.waitForIdle()

        // Verify subtask was added
        assertEquals(1, viewModel.uiState.value.subtasks.size)
        assertEquals("Quick added via keyboard", viewModel.uiState.value.subtasks[0].title)
    }
}

private class TaskDetailFakeTaskRepository : TaskRepository {
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
