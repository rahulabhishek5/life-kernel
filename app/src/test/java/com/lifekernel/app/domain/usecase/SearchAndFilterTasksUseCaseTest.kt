package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskFilterCriteria
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Unit test suite for [SearchAndFilterTasksUseCase] per PRD §8.2 (FR-PLAN-004..006).
 */
class SearchAndFilterTasksUseCaseTest {

    private lateinit var fakeTaskRepository: FakeSearchTaskRepository
    private lateinit var useCase: SearchAndFilterTasksUseCase

    private val now = Instant.parse("2026-10-30T10:00:00Z")
    private val today = LocalDate.of(2026, 10, 30)
    private val studyTag = Tag(id = 1L, name = "study", createdAt = now)
    private val urgentTag = Tag(id = 2L, name = "urgent", createdAt = now)

    @Before
    fun setUp() {
        fakeTaskRepository = FakeSearchTaskRepository()
        useCase = SearchAndFilterTasksUseCase(fakeTaskRepository)
    }

    @Test
    fun crossDateSearch_returnsFutureAndPastTasks_whenMatchingQuery() = runBlocking {
        val pastTask = Task(
            id = 1,
            title = "Review past quarterly budget",
            dueDate = today.minusDays(10),
            createdAt = now,
            updatedAt = now
        )
        val todayTask = Task(
            id = 2,
            title = "Review today's meeting notes",
            dueDate = today,
            createdAt = now,
            updatedAt = now
        )
        val futureTask = Task(
            id = 3,
            title = "Review quarterly roadmap 30 days ahead",
            dueDate = today.plusDays(30),
            createdAt = now,
            updatedAt = now
        )
        val unscheduledTask = Task(
            id = 4,
            title = "Review long-term life vision",
            dueDate = null,
            createdAt = now,
            updatedAt = now
        )
        val unrelatedTask = Task(
            id = 5,
            title = "Buy groceries",
            dueDate = today,
            createdAt = now,
            updatedAt = now
        )

        fakeTaskRepository.setTasks(listOf(pastTask, todayTask, futureTask, unscheduledTask, unrelatedTask))

        val criteria = TaskFilterCriteria(searchQuery = "review")
        assertTrue(criteria.isActive)

        val results = useCase(criteria).first()

        assertEquals(4, results.size)
        assertTrue(results.any { it.id == 1L })
        assertTrue(results.any { it.id == 2L })
        assertTrue(results.any { it.id == 3L })
        assertTrue(results.any { it.id == 4L })
        assertFalse(results.any { it.id == 5L })
    }

    @Test
    fun titleMatching_isCaseInsensitive_andMatchesSubstrings() = runBlocking {
        val task = Task(
            id = 10,
            title = "Deep Work: Kotlin Compiler Architecture",
            createdAt = now,
            updatedAt = now
        )
        fakeTaskRepository.setTasks(listOf(task))

        // Exact match
        assertEquals(1, useCase(TaskFilterCriteria(searchQuery = "Deep Work")).first().size)
        // Substring match
        assertEquals(1, useCase(TaskFilterCriteria(searchQuery = "compiler")).first().size)
        // Case-insensitive match
        assertEquals(1, useCase(TaskFilterCriteria(searchQuery = "KoTlIn")).first().size)
        // Non-matching
        assertEquals(0, useCase(TaskFilterCriteria(searchQuery = "Swift")).first().size)
    }

    @Test
    fun descriptionMatching_returnsTask_whenQueryAppearsInDescription() = runBlocking {
        val task = Task(
            id = 11,
            title = "Project Plan",
            description = "Detailed specifications for Android 15 edge-to-edge support",
            createdAt = now,
            updatedAt = now
        )
        fakeTaskRepository.setTasks(listOf(task))

        assertEquals(1, useCase(TaskFilterCriteria(searchQuery = "edge-to-edge")).first().size)
    }

    @Test
    fun combinedFilterLogic_appliesConjunctiveAndLogic() = runBlocking {
        val matchingAll = Task(
            id = 20,
            title = "Study Algorithms",
            priority = TaskPriority.HIGH,
            tags = listOf(studyTag),
            createdAt = now,
            updatedAt = now
        )
        val wrongPriority = Task(
            id = 21,
            title = "Study System Design",
            priority = TaskPriority.LOW,
            tags = listOf(studyTag),
            createdAt = now,
            updatedAt = now
        )
        val wrongTag = Task(
            id = 22,
            title = "Study Taxes",
            priority = TaskPriority.HIGH,
            tags = listOf(urgentTag),
            createdAt = now,
            updatedAt = now
        )
        val wrongQuery = Task(
            id = 23,
            title = "Workout Session",
            priority = TaskPriority.HIGH,
            tags = listOf(studyTag),
            createdAt = now,
            updatedAt = now
        )

        fakeTaskRepository.setTasks(listOf(matchingAll, wrongPriority, wrongTag, wrongQuery))

        val criteria = TaskFilterCriteria(
            searchQuery = "study",
            selectedPriority = TaskPriority.HIGH,
            selectedTagName = "#study"
        )

        val results = useCase(criteria).first()

        assertEquals(1, results.size)
        assertEquals(20L, results.first().id)
    }

    @Test
    fun emptyCriteria_returnsAllTopLevelTasksUnfiltered() = runBlocking {
        val task1 = Task(id = 1, title = "Task 1", createdAt = now, updatedAt = now)
        val task2 = Task(id = 2, title = "Task 2", createdAt = now, updatedAt = now)
        val subtask = Task(id = 3, title = "Subtask", parentId = 1L, createdAt = now, updatedAt = now)

        fakeTaskRepository.setTasks(listOf(task1, task2, subtask))

        val emptyCriteria = TaskFilterCriteria()
        assertFalse(emptyCriteria.isActive)

        val results = useCase(emptyCriteria).first()

        assertEquals(2, results.size)
        assertTrue(results.none { it.isSubtask })
    }

    @Test
    fun subtasks_areExcludedFromFilterResults() = runBlocking {
        val parent = Task(id = 1, title = "Search keyword in parent", createdAt = now, updatedAt = now)
        val child = Task(id = 2, title = "Search keyword in subtask", parentId = 1L, createdAt = now, updatedAt = now)

        fakeTaskRepository.setTasks(listOf(parent, child))

        val criteria = TaskFilterCriteria(searchQuery = "search keyword")
        val results = useCase(criteria).first()

        assertEquals(1, results.size)
        assertEquals(1L, results.first().id)
    }

    private class FakeSearchTaskRepository : TaskRepository {
        private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

        fun setTasks(tasks: List<Task>) {
            tasksFlow.value = tasks
        }

        override fun observeAllTasks(): Flow<List<Task>> = tasksFlow
        override suspend fun getAllTasks(): List<Task> = tasksFlow.value

        override fun observeOpenTasksDueOnOrBefore(date: LocalDate): Flow<List<Task>> = tasksFlow
        override fun observeOpenTasksForDate(date: LocalDate): Flow<List<Task>> = tasksFlow
        override fun observeOpenTasksWithoutDueDate(): Flow<List<Task>> = tasksFlow
        override fun observeCompletedTasksForDate(date: LocalDate): Flow<List<Task>> = tasksFlow
        override fun observeCompletedCountForDate(date: LocalDate): Flow<Int> = MutableStateFlow(0)
        override fun observeTaskById(id: Long): Flow<Task?> = MutableStateFlow(null)
        override fun observeSubtasks(parentId: Long): Flow<List<Task>> = MutableStateFlow(emptyList())
        override suspend fun getTaskById(id: Long): Task? = tasksFlow.value.firstOrNull { it.id == id }
        override suspend fun createTask(task: Task, tagIds: List<Long>): AppResult<Long> = AppResult.Success(1L)
        override suspend fun updateTask(task: Task, tagIds: List<Long>?): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun deleteTask(taskId: Long): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun completeTask(taskId: Long, occurrenceDate: LocalDate?): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun reopenTask(taskId: Long, occurrenceDate: LocalDate?): AppResult<Unit> = AppResult.Success(Unit)
    }
}
