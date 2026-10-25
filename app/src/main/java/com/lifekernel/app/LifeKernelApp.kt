package com.lifekernel.app

import android.app.Application
import android.content.pm.ApplicationInfo
import com.lifekernel.app.core.logging.AppLogger
import com.lifekernel.app.di.AppContainer
import com.lifekernel.app.di.DefaultAppContainer

/**
 * Application class for Life Kernel.
 *
 * Responsibilities:
 * - Lazy initialization of the manual DI [AppContainer].
 * - Configures runtime logging environment based on build configuration.
 * - Invariant: Performs no heavy work or background scheduling in onCreate.
 */
class LifeKernelApp : Application() {

    val appContainer: AppContainer by lazy {
        DefaultAppContainer(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppLogger.isDebuggable = isDebug
        AppLogger.i("LifeKernelApp") { "Life Kernel initialized" }
    }
}
