package com.lifekernel.app.feature.planner

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Tag
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.domain.repository.TagRepository
import com.lifekernel.app.domain.repository.TaskRepository
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.CreateTaskUseCase
import com.lifekernel.app.domain.usecase.ObserveAllTagsUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import com.lifekernel.app.domain.usecase.SearchAndFilterTasksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit test suite specifically testing Planner search and global filtering per PRD §8.2.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelFilterTest {

    private lateinit var clock: FixedClock
    private lateinit var fakeTaskRepository: FilterTestTaskRepository
    private lateinit var fakeTagRepository: FilterTestTagRepository
    private lateinit var viewModel: PlannerViewModel
    private lateinit var testScope: CoroutineScope
    private lateinit var collectorJob: Job

    private val testInstant = Instant.parse("2026-10-30T10:00:00Z")
    private val today = LocalDate.of(2026, 10, 30)

    private val workTag = Tag(id = 1L, name = "work", createdAt = testInstant)
    private val personalTag = Tag(id = 2L, name = "personal", createdAt = testInstant)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeTaskRepository = FilterTestTaskRepository()
        fakeTagRepository = FilterTestTagRepository()

        val observePlannerTasksUseCase = ObservePlannerTasksUseCase(fakeTaskRepository, clock)
        val completeTaskUseCase = CompleteTaskUseCase(fakeTaskRepository)
        val reopenTaskUseCase = ReopenTaskUseCase(fakeTaskRepository)
        val createTaskUseCase = CreateTaskUseCase(fakeTaskRepository, clock)
        val searchAndFilterTasksUseCase = SearchAndFilterTasksUseCase(fakeTaskRepository)
        val observeAllTagsUseCase = ObserveAllTagsUseCase(fakeTagRepository)

        testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        viewModel = PlannerViewModel(
            observePlannerTasksUseCase = observePlannerTasksUseCase,
            completeTaskUseCase = completeTaskUseCase,
            reopenTaskUseCase = reopenTaskUseCase,
            createTaskUseCase = createTaskUseCase,
            searchAndFilterTasksUseCase = searchAndFilterTasksUseCase,
            observeAllTagsUseCase = observeAllTagsUseCase,
            clock = clock,
            coroutineScope = testScope
        )

        collectorJob = testScope.launch {
            viewModel.uiState.collect {}
        }

        fakeTagRepository.setTags(listOf(workTag, personalTag))
    }

    @After
    fun tearDown() {
        collectorJob.cancel()
    }

    @Test
    fun searchToggleAndQueryUpdates_reflectImmediatelyInState() = runBlocking {
        assertFalse(viewModel.uiState.value.isSearchActive)
        assertEquals("", viewModel.uiState.value.searchQuery)

        viewModel.onToggleSearch()
        assertTrue(viewModel.uiState.value.isSearchActive)

        viewModel.onUpdateSearchQuery("Android")
        assertEquals("Android", viewModel.uiState.value.searchQuery)
        assertTrue(viewModel.uiState.value.isFilterActive)

        viewModel.onCloseSearch()
        assertFalse(viewModel.uiState.value.isSearchActive)
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertFalse(viewModel.uiState.value.isFilterActive)
    }

    @Test
    fun queryDebounce_returnsMatchingTasksAcrossAllDates() = runBlocking {
        val pastTask = Task(
            id = 1,
            title = "Release v1.0 notes",
            dueDate = today.minusDays(5),
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val todayTask = Task(
            id = 2,
            title = "Release v1.1 deployment",
            dueDate = today,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val futureTask = Task(
            id = 3,
            title = "Release v2.0 roadmap",
            dueDate = today.plusDays(45),
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val unrelatedTask = Task(
            id = 4,
            title = "Cook dinner",
            dueDate = today,
            createdAt = testInstant,
            updatedAt = testInstant
        )

        fakeTaskRepository.setTasks(listOf(pastTask, todayTask, futureTask, unrelatedTask))

        viewModel.onUpdateSearchQuery("release")

        // Wait past the 250ms debounce threshold
        delay(350)

        val state = viewModel.uiState.first { it.filteredTasks.size == 3 }
        assertEquals(3, state.filteredTasks.size)
        assertTrue(state.filteredTasks.any { it.id == 1L })
        assertTrue(state.filteredTasks.any { it.id == 2L })
        assertTrue(state.filteredTasks.any { it.id == 3L })
        assertFalse(state.filteredTasks.any { it.id == 4L })
        assertFalse(state.isSearchEmpty)
    }

    @Test
    fun priorityFilter_togglesAndFiltersImmediatelyWithoutDebounce() = runBlocking {
        val highTask = Task(
            id = 10,
            title = "Fix critical crash",
            priority = TaskPriority.HIGH,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val lowTask = Task(
            id = 11,
            title = "Update documentation",
            priority = TaskPriority.LOW,
            createdAt = testInstant,
            updatedAt = testInstant
        )

        fakeTaskRepository.setTasks(listOf(highTask, lowTask))

        assertNull(viewModel.uiState.value.selectedPriorityFilter)

        // Select HIGH priority
        viewModel.onTogglePriorityFilter(TaskPriority.HIGH)
        assertEquals(TaskPriority.HIGH, viewModel.uiState.value.selectedPriorityFilter)
        assertTrue(viewModel.uiState.value.isFilterActive)

        val stateHigh = viewModel.uiState.first { it.filteredTasks.isNotEmpty() }
        assertEquals(1, stateHigh.filteredTasks.size)
        assertEquals(10L, stateHigh.filteredTasks.first().id)

        // Deselect HIGH priority by toggling again
        viewModel.onTogglePriorityFilter(TaskPriority.HIGH)
        assertNull(viewModel.uiState.value.selectedPriorityFilter)
        assertFalse(viewModel.uiState.value.isFilterActive)
    }

    @Test
    fun tagFilter_togglesAndFiltersImmediately() = runBlocking {
        val workTask = Task(
            id = 20,
            title = "Team sync meeting",
            tags = listOf(workTag),
            createdAt = testInstant,
            updatedAt = testInstant
        )
        val personalTask = Task(
            id = 21,
            title = "Grocery run",
            tags = listOf(personalTag),
            createdAt = testInstant,
            updatedAt = testInstant
        )

        fakeTaskRepository.setTasks(listOf(workTask, personalTask))

        // Available tags from repo
        val stateTags = viewModel.uiState.first { it.availableFilterTags.isNotEmpty() }
        assertEquals(listOf("work", "personal"), stateTags.availableFilterTags)

        // Filter by tag "work"
        viewModel.onToggleTagFilter("work")
        assertEquals("work", viewModel.uiState.value.selectedTagFilter)
        assertTrue(viewModel.uiState.value.isFilterActive)

        val stateFiltered = viewModel.uiState.first { it.filteredTasks.isNotEmpty() }
        assertEquals(1, stateFiltered.filteredTasks.size)
        assertEquals(20L, stateFiltered.filteredTasks.first().id)

        // Deselect tag
        viewModel.onToggleTagFilter("work")
        assertNull(viewModel.uiState.value.selectedTagFilter)
        assertFalse(viewModel.uiState.value.isFilterActive)
    }

    @Test
    fun clearAllFilters_resetsQueryPriorityAndTag() = runBlocking {
        val task = Task(id = 30, title = "Important project task", priority = TaskPriority.HIGH, tags = listOf(workTag), createdAt = testInstant, updatedAt = testInstant)
        fakeTaskRepository.setTasks(listOf(task))

        viewModel.onUpdateSearchQuery("project")
        viewModel.onTogglePriorityFilter(TaskPriority.HIGH)
        viewModel.onToggleTagFilter("work")

        assertTrue(viewModel.uiState.value.isFilterActive)
        assertEquals("project", viewModel.uiState.value.searchQuery)
        assertEquals(TaskPriority.HIGH, viewModel.uiState.value.selectedPriorityFilter)
        assertEquals("work", viewModel.uiState.value.selectedTagFilter)

        viewModel.onClearAllFilters()

        assertFalse(viewModel.uiState.value.isFilterActive)
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertNull(viewModel.uiState.value.selectedPriorityFilter)
        assertNull(viewModel.uiState.value.selectedTagFilter)
    }

    @Test
    fun emptySearchResults_triggersIsSearchEmpty() = runBlocking {
        val task = Task(id = 40, title = "Prepare presentation", createdAt = testInstant, updatedAt = testInstant)
        fakeTaskRepository.setTasks(listOf(task))

        viewModel.onUpdateSearchQuery("NonExistentKeywordXYZ")
        delay(350)

        val state = viewModel.uiState.first { it.isFilterActive && it.filteredTasks.isEmpty() }
        assertTrue(state.isFilterActive)
        assertTrue(state.isSearchEmpty)
        assertEquals(0, state.filteredTasks.size)
    }

    private class FilterTestTaskRepository : TaskRepository {
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
        override suspend fun createTask(task: Task, tagIds: List<Long>): AppResult<Long> = AppResult.success(1L)
        override suspend fun updateTask(task: Task, tagIds: List<Long>?): AppResult<Unit> = AppResult.success(Unit)
        override suspend fun deleteTask(taskId: Long): AppResult<Unit> = AppResult.success(Unit)
        override suspend fun completeTask(taskId: Long, occurrenceDate: LocalDate?): AppResult<Unit> = AppResult.success(Unit)
        override suspend fun reopenTask(taskId: Long, occurrenceDate: LocalDate?): AppResult<Unit> = AppResult.success(Unit)
    }

    private class FilterTestTagRepository : TagRepository {
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
}
