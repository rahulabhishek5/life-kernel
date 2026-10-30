package com.lifekernel.app.domain.usecase

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.domain.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetTaskByIdUseCaseTest {

    private lateinit var fakeRepository: FakeTaskRepository
    private val testInstant = Instant.parse("2026-10-25T10:00:00Z")

    @Before
    fun setUp() {
        fakeRepository = FakeTaskRepository()
    }

    @Test
    fun get_existingTask_returnsSuccessWithTask() = runBlocking {
        val task = Task(
            id = 55L,
            title = "Observable task",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[55L] = task

        val useCase = GetTaskByIdUseCase(fakeRepository)
        val result = useCase.get(55L)

        assertTrue(result.isSuccess)
        val retrieved = result.getOrNull()
        assertNotNull(retrieved)
        assertEquals("Observable task", retrieved?.title)
    }

    @Test
    fun get_nonExistentTask_returnsNotFoundError() = runBlocking {
        val useCase = GetTaskByIdUseCase(fakeRepository)
        val result = useCase.get(999L)

        assertTrue(result.isFailure)
        assertTrue(result.errorOrNull() is AppError.NotFound)
    }

    @Test
    fun invoke_observesReactiveFlow() = runBlocking {
        val task = Task(
            id = 77L,
            title = "Flow test task",
            createdAt = testInstant,
            updatedAt = testInstant
        )
        fakeRepository.tasks[77L] = task
        fakeRepository.notifyChanged()

        val useCase = GetTaskByIdUseCase(fakeRepository)
        val emission = useCase(77L).first()
        assertNotNull(emission)
        assertEquals("Flow test task", emission?.title)

        val nonExistentEmission = useCase(888L).first()
        assertNull(nonExistentEmission)
    }
}
