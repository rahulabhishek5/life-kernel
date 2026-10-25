package com.lifekernel.app.di

import android.content.Context
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.core.time.DefaultClock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.repository.TagRepositoryImpl
import com.lifekernel.app.data.repository.TaskRepositoryImpl
import com.lifekernel.app.domain.repository.TagRepository
import com.lifekernel.app.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers

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
}
