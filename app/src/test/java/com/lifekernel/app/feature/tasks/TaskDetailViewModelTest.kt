package com.lifekernel.app.feature.tasks

import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.domain.usecase.DeleteTaskUseCase
import com.lifekernel.app.domain.usecase.FakeTaskRepository
import com.lifekernel.app.domain.usecase.GetTaskByIdUseCase
import com.lifekernel.app.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TaskDetailViewModelTest {

    private lateinit var fakeRepository: FakeTaskRepository
    private lateinit var clock: FixedClock
    private lateinit var getTaskByIdUseCase: GetTaskByIdUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var testScope: CoroutineScope

    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeRepository = FakeTaskRepository()
        getTaskByIdUseCase = GetTaskByIdUseCase(fakeRepository)
        updateTaskUseCase = UpdateTaskUseCase(fakeRepository, clock)
        deleteTaskUseCase = DeleteTaskUseCase(fakeRepository)
        testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    private fun createViewModel(taskId: Long): TaskDetailViewModel {
        return TaskDetailViewModel(
            taskId = taskId,
            getTaskByIdUseCase = getTaskByIdUseCase,
            updateTaskUseCase = updateTaskUseCase,
            deleteTaskUseCase = deleteTaskUseCase,
            clock = clock,
            coroutineScope = testScope
        )
    }

    @Test
    fun init_loadsTaskAndPopulatesState() = runBlocking {
        val task = Task(
            id = 1L,
            title = "Review quarterly roadmap",
            description = "Gather engineering OKRs",
            priority = TaskPriority.HIGH,
            startDate = testDate,
            dueDate = testDate.plusDays(3),
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[1L] = task

        val viewModel = createViewModel(1L)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Review quarterly roadmap", state.title)
        assertEquals("Gather engineering OKRs", state.description)
        assertEquals(TaskPriority.HIGH, state.priority)
        assertEquals(testDate, state.startDate)
        assertEquals(testDate.plusDays(3), state.dueDate)
        assertFalse(state.isCompleted)
        assertTrue(state.canSave)
    }

    @Test
    fun onTitleChange_blankTitle_setsErrorAndDisablesSave() = runBlocking {
        val task = Task(
            id = 2L,
            title = "Valid Title",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[2L] = task

        val viewModel = createViewModel(2L)

        viewModel.onTitleChange("   ")
        val state = viewModel.uiState.value
        assertEquals("Title cannot be empty", state.titleError)
        assertFalse(state.canSave)

        viewModel.onTitleChange("New valid title")
        val updatedState = viewModel.uiState.value
        assertNull(updatedState.titleError)
        assertTrue(updatedState.canSave)
    }

    @Test
    fun onStartDateChange_afterDueDate_setsDateErrorAndDisablesSave() = runBlocking {
        val task = Task(
            id = 3L,
            title = "Test task",
            startDate = testDate,
            dueDate = testDate.plusDays(2),
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[3L] = task

        val viewModel = createViewModel(3L)

        viewModel.onStartDateChange(testDate.plusDays(5))
        val state = viewModel.uiState.value
        assertEquals("Start date cannot be after due date", state.dateError)
        assertFalse(state.canSave)

        viewModel.onStartDateChange(testDate)
        val fixedState = viewModel.uiState.value
        assertNull(fixedState.dateError)
        assertTrue(fixedState.canSave)
    }

    @Test
    fun onSave_persistsChangesAndSetsIsSaved() = runBlocking {
        val task = Task(
            id = 4L,
            title = "Before Edit",
            description = "Before Desc",
            priority = TaskPriority.NONE,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[4L] = task

        val viewModel = createViewModel(4L)

        viewModel.onTitleChange("After Edit")
        viewModel.onDescriptionChange("After Desc")
        viewModel.onPriorityChange(TaskPriority.MEDIUM)
        viewModel.onSave()

        val state = viewModel.uiState.value
        assertTrue("isSaved must be true after successful save", state.isSaved)

        val stored = fakeRepository.getTaskById(4L)
        assertNotNull(stored)
        assertEquals("After Edit", stored?.title)
        assertEquals("After Desc", stored?.description)
        assertEquals(TaskPriority.MEDIUM, stored?.priority)
    }

    @Test
    fun deleteFlow_showDismissAndConfirm() = runBlocking {
        val task = Task(
            id = 5L,
            title = "Task to delete",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[5L] = task

        val viewModel = createViewModel(5L)

        assertFalse(viewModel.uiState.value.showDeleteDialog)

        viewModel.onShowDeleteDialog()
        assertTrue(viewModel.uiState.value.showDeleteDialog)

        viewModel.onDismissDeleteDialog()
        assertFalse(viewModel.uiState.value.showDeleteDialog)

        viewModel.onShowDeleteDialog()
        viewModel.onConfirmDelete()

        val state = viewModel.uiState.value
        assertFalse(state.showDeleteDialog)
        assertTrue("isDeleted must be true after confirmation", state.isDeleted)
        assertNull("Task must be removed from repository", fakeRepository.getTaskById(5L))
    }

    @Test
    fun editingCompletedTask_preservesDoneStatus() = runBlocking {
        val completedTask = Task(
            id = 6L,
            title = "Completed task",
            status = TaskStatus.DONE,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[6L] = completedTask

        val viewModel = createViewModel(6L)

        assertTrue(viewModel.uiState.value.isCompleted)

        viewModel.onTitleChange("Updated completed task title")
        viewModel.onSave()

        val stored = fakeRepository.getTaskById(6L)
        assertNotNull(stored)
        assertEquals("Updated completed task title", stored?.title)
        assertEquals("Status must remain DONE", TaskStatus.DONE, stored?.status)
    }

    @Test
    fun tagManagement_addRemoveAndDuplicate() = runBlocking {
        val task = Task(
            id = 7L,
            title = "Task with tags",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[7L] = task

        val viewModel = createViewModel(7L)

        // Initial tags empty
        assertTrue(viewModel.uiState.value.tags.isEmpty())

        // Add valid tag with uppercase and whitespace
        viewModel.onTagInputChanged("  Urgent  ")
        assertNull(viewModel.uiState.value.tagInputError)
        viewModel.onAddTag("  Urgent  ")

        assertEquals(listOf("urgent"), viewModel.uiState.value.tags)
        assertEquals("", viewModel.uiState.value.tagInput)

        // Adding duplicate (case-insensitive) should be ignored
        viewModel.onAddTag("URGENT")
        assertEquals(listOf("urgent"), viewModel.uiState.value.tags)

        // Add second tag
        viewModel.onAddTag("work-project")
        assertEquals(listOf("urgent", "work-project"), viewModel.uiState.value.tags)

        // Remove tag
        viewModel.onRemoveTag("urgent")
        assertEquals(listOf("work-project"), viewModel.uiState.value.tags)
    }

    @Test
    fun tagManagement_invalidTagInput_setsErrorAndDisablesSave() = runBlocking {
        val task = Task(
            id = 8L,
            title = "Task for validation",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[8L] = task

        val viewModel = createViewModel(8L)

        // Invalid tag with spaces
        viewModel.onTagInputChanged("work space")
        assertNotNull(viewModel.uiState.value.tagInputError)
        assertFalse(viewModel.uiState.value.canSave)

        // Clear or fix input
        viewModel.onTagInputChanged("work-space")
        assertNull(viewModel.uiState.value.tagInputError)
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun subtask_quickAddToggleDeleteAndProgressCalculation() = runBlocking {
        val task = Task(
            id = 9L,
            title = "Task with subtasks",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[9L] = task

        val viewModel = createViewModel(9L)

        // Initial state: empty subtasks, progress 0
        assertEquals(0, viewModel.uiState.value.subtasksCount)
        assertEquals(0, viewModel.uiState.value.completedSubtasksCount)
        assertEquals(0f, viewModel.uiState.value.subtaskProgress, 0.001f)

        // Attempt blank subtask
        viewModel.onSubtaskInputChange("   ")
        viewModel.onAddSubtask()
        assertNotNull(viewModel.uiState.value.subtaskInputError)
        assertEquals(0, viewModel.uiState.value.subtasksCount)

        // Add valid subtasks
        viewModel.onSubtaskInputChange("First subtask")
        viewModel.onAddSubtask()
        assertEquals(1, viewModel.uiState.value.subtasksCount)
        assertEquals("", viewModel.uiState.value.subtaskInput)
        assertNull(viewModel.uiState.value.subtaskInputError)

        viewModel.onSubtaskInputChange("Second subtask")
        viewModel.onAddSubtask()
        assertEquals(2, viewModel.uiState.value.subtasksCount)
        assertEquals(0, viewModel.uiState.value.completedSubtasksCount)
        assertEquals(0f, viewModel.uiState.value.subtaskProgress, 0.001f)

        // Toggle first subtask completed
        val subtask1 = viewModel.uiState.value.subtasks[0]
        viewModel.onToggleSubtask(subtask1)
        assertEquals(1, viewModel.uiState.value.completedSubtasksCount)
        assertEquals(0.5f, viewModel.uiState.value.subtaskProgress, 0.001f)

        // Invariant: parent task completion status remains false!
        assertFalse(viewModel.uiState.value.isCompleted)

        // Toggle second subtask completed
        val subtask2 = viewModel.uiState.value.subtasks[1]
        viewModel.onToggleSubtask(subtask2)
        assertEquals(2, viewModel.uiState.value.completedSubtasksCount)
        assertEquals(1.0f, viewModel.uiState.value.subtaskProgress, 0.001f)

        // Invariant: all subtasks done does NOT mark parent task completed!
        assertFalse(viewModel.uiState.value.isCompleted)

        // Delete first subtask
        viewModel.onDeleteSubtask(subtask1)
        assertEquals(1, viewModel.uiState.value.subtasksCount)
        assertEquals(1, viewModel.uiState.value.completedSubtasksCount)
        assertEquals(1.0f, viewModel.uiState.value.subtaskProgress, 0.001f)
    }
}

