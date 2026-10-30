package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.domain.model.Subtask
import com.lifekernel.app.domain.repository.SubtaskRepository
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
import java.time.Instant
import java.time.LocalDate

class SubtaskUseCasesTest {

    private lateinit var fakeRepository: FakeSubtaskRepository
    private lateinit var observeSubtasksUseCase: ObserveSubtasksForTaskUseCase
    private lateinit var addSubtaskUseCase: AddSubtaskUseCase
    private lateinit var toggleSubtaskUseCase: ToggleSubtaskUseCase
    private lateinit var deleteSubtaskUseCase: DeleteSubtaskUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeSubtaskRepository()
        observeSubtasksUseCase = ObserveSubtasksForTaskUseCase(fakeRepository)
        addSubtaskUseCase = AddSubtaskUseCase(fakeRepository)
        toggleSubtaskUseCase = ToggleSubtaskUseCase(fakeRepository)
        deleteSubtaskUseCase = DeleteSubtaskUseCase(fakeRepository)
    }

    @Test
    fun addSubtask_validTitle_assignsSequentialPositions() = runBlocking {
        val taskId = 1L

        val id1 = addSubtaskUseCase(taskId, "First subtask").getOrNull()!!
        val id2 = addSubtaskUseCase(taskId, "Second subtask").getOrNull()!!
        val id3 = addSubtaskUseCase(taskId, "Third subtask").getOrNull()!!

        val subtasks = observeSubtasksUseCase(taskId).first()
        assertEquals(3, subtasks.size)
        assertEquals(0, subtasks[0].position)
        assertEquals(1, subtasks[1].position)
        assertEquals(2, subtasks[2].position)

        assertEquals("First subtask", subtasks[0].title)
        assertEquals("Second subtask", subtasks[1].title)
        assertEquals("Third subtask", subtasks[2].title)
    }

    @Test
    fun addSubtask_trimsWhitespace() = runBlocking {
        val taskId = 1L
        val result = addSubtaskUseCase(taskId, "   Clean room   ")
        assertTrue(result.isSuccess)

        val subtasks = observeSubtasksUseCase(taskId).first()
        assertEquals(1, subtasks.size)
        assertEquals("Clean room", subtasks[0].title)
    }

    @Test
    fun addSubtask_blankOrTooLong_fails() = runBlocking {
        val taskId = 1L
        val blankResult = addSubtaskUseCase(taskId, "   ")
        assertTrue(blankResult.isFailure)
        assertTrue(blankResult.errorOrNull() is AppError.Validation)

        val tooLongResult = addSubtaskUseCase(taskId, "a".repeat(201))
        assertTrue(tooLongResult.isFailure)
        assertTrue(tooLongResult.errorOrNull() is AppError.Validation)
    }

    @Test
    fun toggleSubtask_togglesCompletionStateIndependently() = runBlocking {
        val taskId = 1L
        val subtaskId = addSubtaskUseCase(taskId, "Check lights").getOrNull()!!

        var subtask = fakeRepository.getSubtaskById(subtaskId)!!
        assertFalse(subtask.isCompleted)

        val toggle1 = toggleSubtaskUseCase(subtaskId)
        assertTrue(toggle1.isSuccess)
        subtask = fakeRepository.getSubtaskById(subtaskId)!!
        assertTrue(subtask.isCompleted)

        val toggle2 = toggleSubtaskUseCase(subtaskId)
        assertTrue(toggle2.isSuccess)
        subtask = fakeRepository.getSubtaskById(subtaskId)!!
        assertFalse(subtask.isCompleted)
    }

    @Test
    fun deleteSubtask_maintainsPositionContinuity() = runBlocking {
        val taskId = 1L
        val id1 = addSubtaskUseCase(taskId, "Step 1").getOrNull()!!
        val id2 = addSubtaskUseCase(taskId, "Step 2").getOrNull()!!
        val id3 = addSubtaskUseCase(taskId, "Step 3").getOrNull()!!

        // Delete the middle item (Step 2)
        val deleteResult = deleteSubtaskUseCase(id2)
        assertTrue(deleteResult.isSuccess)

        val remaining = observeSubtasksUseCase(taskId).first()
        assertEquals(2, remaining.size)
        assertEquals("Step 1", remaining[0].title)
        assertEquals(0, remaining[0].position)
        assertEquals("Step 3", remaining[1].title)
        assertEquals(1, remaining[1].position)
    }

    @Test
    fun observeSubtasks_emitsOrderedByPosition() = runBlocking {
        val taskId = 1L
        fakeRepository.subtasks.value = listOf(
            Subtask(id = 2, taskId = taskId, title = "Second", position = 1),
            Subtask(id = 1, taskId = taskId, title = "First", position = 0),
            Subtask(id = 3, taskId = taskId, title = "Third", position = 2)
        )

        val result = observeSubtasksUseCase(taskId).first()
        assertEquals(3, result.size)
        assertEquals("First", result[0].title)
        assertEquals("Second", result[1].title)
        assertEquals("Third", result[2].title)
    }
}

private class FakeSubtaskRepository : SubtaskRepository {
    val subtasks = MutableStateFlow<List<Subtask>>(emptyList())
    private var nextId = 1L

    override fun observeSubtasksForTask(taskId: Long): Flow<List<Subtask>> {
        return subtasks.map { list ->
            list.filter { it.taskId == taskId }.sortedBy { it.position }
        }
    }

    override suspend fun getSubtasksForTask(taskId: Long): List<Subtask> {
        return subtasks.value.filter { it.taskId == taskId }.sortedBy { it.position }
    }

    override suspend fun getSubtaskById(subtaskId: Long): Subtask? {
        return subtasks.value.find { it.id == subtaskId }
    }

    override suspend fun addSubtask(taskId: Long, title: String): AppResult<Long> {
        val currentForTask = subtasks.value.filter { it.taskId == taskId }
        val maxPos = currentForTask.maxOfOrNull { it.position } ?: -1
        val newSubtask = Subtask(
            id = nextId++,
            taskId = taskId,
            title = title,
            isCompleted = false,
            position = maxPos + 1,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        subtasks.value = subtasks.value + newSubtask
        return AppResult.success(newSubtask.id)
    }

    override suspend fun toggleSubtask(subtaskId: Long): AppResult<Unit> {
        val item = subtasks.value.find { it.id == subtaskId }
            ?: return AppResult.failure(AppError.NotFound("Subtask not found"))

        subtasks.value = subtasks.value.map {
            if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted, updatedAt = 2000L) else it
        }
        return AppResult.success(Unit)
    }

    override suspend fun deleteSubtask(subtaskId: Long): AppResult<Unit> {
        val item = subtasks.value.find { it.id == subtaskId }
            ?: return AppResult.failure(AppError.NotFound("Subtask not found"))

        val filtered = subtasks.value.filterNot { it.id == subtaskId }
        // Compact positions for taskId
        val taskItems = filtered.filter { it.taskId == item.taskId }.sortedBy { it.position }
        val reindexedTaskItems = taskItems.mapIndexed { index, subtask ->
            subtask.copy(position = index)
        }
        val otherTasksItems = filtered.filterNot { it.taskId == item.taskId }
        subtasks.value = otherTasksItems + reindexedTaskItems
        return AppResult.success(Unit)
    }

    override suspend fun reorderSubtasks(taskId: Long, subtaskIds: List<Long>): AppResult<Unit> {
        val currentForTask = subtasks.value.filter { it.taskId == taskId }
        val reordered = subtaskIds.mapIndexedNotNull { index, id ->
            currentForTask.find { it.id == id }?.copy(position = index)
        }
        val otherTasksItems = subtasks.value.filterNot { it.taskId == taskId }
        subtasks.value = otherTasksItems + reordered
        return AppResult.success(Unit)
    }
}
