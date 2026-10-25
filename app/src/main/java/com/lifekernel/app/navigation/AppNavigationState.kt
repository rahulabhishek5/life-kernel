package com.lifekernel.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * State holder for top-level navigation per ARCHITECTURE.md §12a.
 * Maintains state restoration and single-top semantics when reselecting a top-level route.
 */
@Stable
class AppNavigationState(
    initialScreen: Screen = Screen.Home
) {
    var currentScreen by mutableStateOf(initialScreen)
        private set

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            currentScreen = screen
        }
    }

    fun navigateToHome() {
        navigateTo(Screen.Home)
    }

    companion object {
        val Saver: Saver<AppNavigationState, String> = Saver(
            save = { it.currentScreen.route },
            restore = { AppNavigationState(Screen.fromRoute(it)) }
        )
    }
}

@Composable
fun rememberAppNavigationState(
    initialScreen: Screen = Screen.Home
): AppNavigationState {
    return rememberSaveable(saver = AppNavigationState.Saver) {
        AppNavigationState(initialScreen)
    }
}
