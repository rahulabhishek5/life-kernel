package com.lifekernel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.flowWithLifecycle
import com.lifekernel.app.core.designsystem.theme.AccentPalettes
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.di.LocalAppContainer
import com.lifekernel.app.navigation.AppNavHost

/**
 * Single activity entry point for Life Kernel per ARCHITECTURE.md §12a.
 * Observes active theme palette from [com.lifekernel.app.domain.repository.SettingsRepository] with lifecycle awareness
 * and provides dynamic accent theming to [PosTheme].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as LifeKernelApp
        val settingsRepository = app.appContainer.settingsRepository

        setContent {
            val activePalette by remember(settingsRepository, lifecycle) {
                settingsRepository.observeThemePalette().flowWithLifecycle(lifecycle)
            }.collectAsState(initial = AccentPalettes.Default)

            CompositionLocalProvider(LocalAppContainer provides app.appContainer) {
                PosTheme(palette = activePalette) {
                    AppNavHost()
                }
            }
        }
    }
}