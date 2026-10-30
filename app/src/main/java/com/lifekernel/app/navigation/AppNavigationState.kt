package com.lifekernel.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * State holder for top-level and detail navigation per ARCHITECTURE.md §12a.
 * Maintains back stack for detail routes and single-top semantics for top-level routes.
 */
@Stable
class AppNavigationState(
    initialScreen: Screen = Screen.Home
) {
    private val backStack = mutableStateListOf<Screen>(initialScreen)

    val currentScreen: Screen
        get() = backStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            if (screen is Screen.Home) {
                backStack.clear()
                backStack.add(Screen.Home)
            } else if (screen is Screen.Settings) {
                if (backStack.contains(Screen.Settings)) {
                    while (backStack.isNotEmpty() && backStack.last() != Screen.Settings) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                } else {
                    backStack.add(screen)
                }
            } else {
                backStack.add(screen)
            }
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            return true
        }
        return false
    }

    fun navigateToHome() {
        backStack.clear()
        backStack.add(Screen.Home)
    }

    companion object {
        val Saver: Saver<AppNavigationState, String> = Saver(
            save = { it.backStack.joinToString(";") { screen -> screen.route } },
            restore = { saved ->
                val routes = saved.split(";").filter { it.isNotEmpty() }
                val initial = Screen.fromRoute(routes.firstOrNull())
                val state = AppNavigationState(initial)
                state.backStack.clear()
                if (routes.isEmpty()) {
                    state.backStack.add(Screen.Home)
                } else {
                    routes.forEach { state.backStack.add(Screen.fromRoute(it)) }
                }
                state
            }
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
