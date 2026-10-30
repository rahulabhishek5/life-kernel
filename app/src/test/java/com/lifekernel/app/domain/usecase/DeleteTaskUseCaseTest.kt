package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.domain.model.Task
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class DeleteTaskUseCaseTest {

    private lateinit var fakeRepository: FakeTaskRepository
    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")

    @Before
    fun setUp() {
        fakeRepository = FakeTaskRepository()
    }

    @Test
    fun deleteTask_existingTask_removesFromRepository() = runBlocking {
        val task = Task(
            id = 100L,
            title = "Task to be removed",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[100L] = task

        val useCase = DeleteTaskUseCase(fakeRepository)
        val result = useCase(100L)

        assertTrue(result.isSuccess)
        assertNull(fakeRepository.getTaskById(100L))
        assertEquals(0, fakeRepository.tasks.size)
    }

    @Test
    fun deleteTask_nonExistentTask_returnsNotFoundError() = runBlocking {
        val useCase = DeleteTaskUseCase(fakeRepository)
        val result = useCase(999L)

        assertTrue(result.isFailure)
        val error = result.errorOrNull()
        assertTrue(error is AppError.NotFound)
    }
}
