package com.lifekernel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.navigation.AppNavHost

/**
 * Single activity entry point for Life Kernel per ARCHITECTURE.md §12a.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PosTheme {
                AppNavHost()
            }
        }
    }
}