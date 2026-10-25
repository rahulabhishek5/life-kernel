package com.lifekernel.app.di

import android.content.Context
import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.core.time.DefaultClock
import com.lifekernel.app.data.local.LifeKernelDatabase

/**
 * Dependency injection container for manual DI across Life Kernel.
 *
 * Invariant: All singletons (database, clock, repositories) are registered here.
 * No service locators or hidden singletons outside this container.
 */
interface AppContainer {
    val clock: Clock
    val database: LifeKernelDatabase
}

/**
 * Default implementation of [AppContainer], initialized in [com.lifekernel.app.LifeKernelApp].
 */
class DefaultAppContainer(
    private val context: Context
) : AppContainer {
    override val clock: Clock by lazy { DefaultClock() }
    override val database: LifeKernelDatabase by lazy { LifeKernelDatabase.create(context) }
}
