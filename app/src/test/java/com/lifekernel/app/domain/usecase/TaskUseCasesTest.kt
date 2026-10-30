package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskPriority
import com.lifekernel.app.domain.model.TaskStatus
import com.lifekernel.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit tests for domain use cases using a fake in-memory repository and [FixedClock].
 */
class TaskUseCasesTest {

    private lateinit var fakeRepository: FakeTaskRepository
    private lateinit var clock: FixedClock
    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")
    private val testDate = LocalDate.of(2026, 10, 25)

    @Before
    fun setUp() {
        clock = FixedClock(testInstant, ZoneId.of("UTC"))
        fakeRepository = FakeTaskRepository()
    }

    @Test
    fun createTaskUseCase_validTask_populatesTimestampsAndCreates() = runBlocking {
        val useCase = CreateTaskUseCase(fakeRepository, clock)
        val task = Task(
            title = "Ship Milestone M2",
            dueDate = testDate,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )

        val result = useCase(task)
        assertTrue(result.isSuccess)
        val taskId = result.getOrNull()!!

        val createdTask = fakeRepository.getTaskById(taskId)
        assertNotNull(createdTask)
        assertEquals("Ship Milestone M2", createdTask?.title)
        assertEquals(testInstant, createdTask?.createdAt)
        assertEquals(testInstant, createdTask?.updatedAt)
    }

    @Test
    fun createTaskUseCase_invalidTitle_returnsValidationError() = runBlocking {
        val useCase = CreateTaskUseCase(fakeRepository, clock)
        val invalidTask = Task(
            title = "   ",
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )

        val result = useCase(invalidTask)
        assertTrue(result.isFailure)
        assertTrue(result.errorOrNull() is AppError.Validation)
        assertEquals(0, fakeRepository.tasks.size)
    }

    @Test
    fun completeTaskUseCase_marksTaskDone() = runBlocking {
        val task = Task(
            id = 1L,
            title = "Draft release notes",
            status = TaskStatus.OPEN,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[1L] = task

        val useCase = CompleteTaskUseCase(fakeRepository)
        val result = useCase(1L)
        assertTrue(result.isSuccess)

        val updated = fakeRepository.getTaskById(1L)
        assertEquals(TaskStatus.DONE, updated?.status)
    }

    @Test
    fun reopenTaskUseCase_revertsTaskToOpen() = runBlocking {
        val task = Task(
            id = 1L,
            title = "Draft release notes",
            status = TaskStatus.DONE,
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[1L] = task

        val useCase = ReopenTaskUseCase(fakeRepository)
        val result = useCase(1L)
        assertTrue(result.isSuccess)

        val updated = fakeRepository.getTaskById(1L)
        assertEquals(TaskStatus.OPEN, updated?.status)
    }

    @Test
    fun deleteTaskUseCase_removesTask() = runBlocking {
        val task = Task(
            id = 42L,
            title = "To be deleted",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[42L] = task

        val useCase = DeleteTaskUseCase(fakeRepository)
        val result = useCase(42L)
        assertTrue(result.isSuccess)
        assertEquals(0, fakeRepository.tasks.size)
    }

    @Test
    fun updateTaskUseCase_refreshesUpdatedAtAndValidates() = runBlocking {
        val task = Task(
            id = 5L,
            title = "Initial title",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[5L] = task

        val newInstant = Instant.parse("2026-10-25T12:00:00Z")
        clock.setInstant(newInstant)

        val useCase = UpdateTaskUseCase(fakeRepository, clock)
        val updateResult = useCase(task.copy(title = "Updated title"))
        assertTrue(updateResult.isSuccess)

        val updated = fakeRepository.getTaskById(5L)
        assertEquals("Updated title", updated?.title)
        assertEquals(newInstant, updated?.updatedAt)
    }

    @Test
    fun observePlannerTasksUseCase_separatesOverdueTodayAndCompleted() = runBlocking {
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
        val completedTask = Task(
            id = 3L,
            title = "Completed today task",
            dueDate = testDate,
            status = TaskStatus.DONE,
            createdAt = testInstant,
            updatedAt = testInstant
        )

        fakeRepository.addTask(overdueTask)
        fakeRepository.addTask(todayTask)
        fakeRepository.addTask(completedTask)
        fakeRepository.setCompletedCount(testDate, 1)

        val useCase = ObservePlannerTasksUseCase(fakeRepository, clock)
        val snapshot = useCase(testDate).first()

        assertEquals(1, snapshot.overdueTasks.size)
        assertEquals(1L, snapshot.overdueTasks.first().id)

        assertEquals(1, snapshot.todayTasks.size)
        assertEquals(2L, snapshot.todayTasks.first().id)

        assertEquals(1, snapshot.completedTodayTasks.size)
        assertEquals(3L, snapshot.completedTodayTasks.first().id)

        assertEquals(1, snapshot.completedTodayCount)
        assertEquals(2, snapshot.totalTodayCount) // 1 open today + 1 completed today
    }
}

/**
 * In-memory fake implementation of [TaskRepository] for unit testing.
 */
class FakeTaskRepository : TaskRepository {
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
        if (!tasks.containsKey(taskId)) {
            return AppResult.failure(AppError.NotFound("Task $taskId not found"))
        }
        tasks.remove(taskId)
        notifyChanged()
        return AppResult.success(Unit)
    }

    override suspend fun completeTask(taskId: Long, occurrenceDate: LocalDate?): AppResult<Unit> {
        val task = tasks[taskId] ?: return AppResult.failure(AppError.NotFound("Task $taskId not found"))
        if (task.status == TaskStatus.DONE) {
            return AppResult.failure(AppError.Validation("Task $taskId already completed"))
        }
        tasks[taskId] = task.copy(status = TaskStatus.DONE)
        val date = occurrenceDate ?: task.dueDate ?: LocalDate.now()
        completedCounts[date] = (completedCounts[date] ?: 0) + 1
        notifyChanged()
        return AppResult.success(Unit)
    }

    override suspend fun reopenTask(taskId: Long, occurrenceDate: LocalDate?): AppResult<Unit> {
        val task = tasks[taskId] ?: return AppResult.failure(AppError.NotFound("Task $taskId not found"))
        if (task.status == TaskStatus.OPEN) {
            return AppResult.failure(AppError.Validation("Task $taskId already open"))
        }
        tasks[taskId] = task.copy(status = TaskStatus.OPEN)
        val date = occurrenceDate ?: task.dueDate ?: LocalDate.now()
        val current = completedCounts[date] ?: 0
        if (current > 0) completedCounts[date] = current - 1
        notifyChanged()
        return AppResult.success(Unit)
    }
}
