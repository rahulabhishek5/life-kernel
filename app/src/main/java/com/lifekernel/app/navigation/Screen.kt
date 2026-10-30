package com.lifekernel.app.navigation

/**
 * Type-safe navigation routes for Life Kernel per ARCHITECTURE.md §12a.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data class TaskDetail(val taskId: Long) : Screen("tasks/$taskId") {
        companion object {
            const val ROUTE_PREFIX = "tasks/"
            fun createRoute(taskId: Long): String = "tasks/$taskId"
        }
    }

    companion object {
        fun fromRoute(route: String?): Screen = when {
            route == null -> Home
            route == Settings.route -> Settings
            route == Home.route -> Home
            route.startsWith(TaskDetail.ROUTE_PREFIX) -> {
                val id = route.removePrefix(TaskDetail.ROUTE_PREFIX).toLongOrNull() ?: 0L
                TaskDetail(id)
            }
            else -> Home
        }
    }
}
