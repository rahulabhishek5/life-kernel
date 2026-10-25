package com.lifekernel.app.navigation

/**
 * Type-safe navigation routes for Life Kernel.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")

    companion object {
        fun fromRoute(route: String?): Screen = when (route) {
            Settings.route -> Settings
            else -> Home
        }
    }
}
