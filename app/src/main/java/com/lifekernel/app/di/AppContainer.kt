package com.lifekernel.app.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.core.time.DefaultClock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.repository.TagRepositoryImpl
import com.lifekernel.app.data.repository.TaskRepositoryImpl
import com.lifekernel.app.domain.repository.TagRepository
import com.lifekernel.app.domain.repository.TaskRepository
import com.lifekernel.app.domain.usecase.CompleteTaskUseCase
import com.lifekernel.app.domain.usecase.CreateTaskUseCase
import com.lifekernel.app.domain.usecase.ObservePlannerTasksUseCase
import com.lifekernel.app.domain.usecase.ReopenTaskUseCase
import com.lifekernel.app.feature.planner.PlannerViewModel
import kotlinx.coroutines.Dispatchers

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided. Ensure AppNavHost or MainActivity provides LocalAppContainer.")
}

/**
 * Dependency injection container for manual DI across Life Kernel.
 *
 * Invariant: All singletons (database, clock, repositories) are registered here.
 * No service locators or hidden singletons outside this container.
 */
interface AppContainer {
    val clock: Clock
    val database: LifeKernelDatabase
    val taskRepository: TaskRepository
    val tagRepository: TagRepository
    val observePlannerTasksUseCase: ObservePlannerTasksUseCase
    val completeTaskUseCase: CompleteTaskUseCase
    val reopenTaskUseCase: ReopenTaskUseCase
    val createTaskUseCase: CreateTaskUseCase
    fun createPlannerViewModel(): PlannerViewModel
}

/**
 * Default implementation of [AppContainer], initialized in [com.lifekernel.app.LifeKernelApp].
 */
class DefaultAppContainer(
    private val context: Context
) : AppContainer {
    override val clock: Clock by lazy { DefaultClock() }
    override val database: LifeKernelDatabase by lazy { LifeKernelDatabase.create(context) }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(
            database = database,
            clock = clock,
            dispatcher = Dispatchers.IO
        )
    }

    override val tagRepository: TagRepository by lazy {
        TagRepositoryImpl(
            database = database,
            clock = clock,
            dispatcher = Dispatchers.IO
        )
    }

    override val observePlannerTasksUseCase: ObservePlannerTasksUseCase by lazy {
        ObservePlannerTasksUseCase(
            taskRepository = taskRepository,
            clock = clock
        )
    }

    override val completeTaskUseCase: CompleteTaskUseCase by lazy {
        CompleteTaskUseCase(
            taskRepository = taskRepository
        )
    }

    override val reopenTaskUseCase: ReopenTaskUseCase by lazy {
        ReopenTaskUseCase(
            taskRepository = taskRepository
        )
    }

    override val createTaskUseCase: CreateTaskUseCase by lazy {
        CreateTaskUseCase(
            taskRepository = taskRepository,
            clock = clock
        )
    }

    override fun createPlannerViewModel(): PlannerViewModel {
        return PlannerViewModel(
            observePlannerTasksUseCase = observePlannerTasksUseCase,
            completeTaskUseCase = completeTaskUseCase,
            reopenTaskUseCase = reopenTaskUseCase,
            createTaskUseCase = createTaskUseCase,
            clock = clock
        )
    }
}
