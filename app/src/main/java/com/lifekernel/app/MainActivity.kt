package com.lifekernel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.di.LocalAppContainer
import com.lifekernel.app.navigation.AppNavHost

/**
 * Single activity entry point for Life Kernel per ARCHITECTURE.md §12a.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as LifeKernelApp
        setContent {
            CompositionLocalProvider(LocalAppContainer provides app.appContainer) {
                PosTheme {
                    AppNavHost()
                }
            }
        }
    }
}