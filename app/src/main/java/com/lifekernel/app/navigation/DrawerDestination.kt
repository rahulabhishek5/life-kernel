package com.lifekernel.app.navigation

/**
 * Drawer groups per design-system.md §7 & §8.3.
 */
enum class DrawerGroup {
    Daily,
    System
}

/**
 * Top-level drawer destination model per ARCHITECTURE.md §12a.
 */
data class DrawerDestination(
    val screen: Screen,
    val title: String,
    val group: DrawerGroup
)

/**
 * Centralized list of top-level destinations per FR-NAV-002.
 * Strictly includes only built modules; unbuilt modules are omitted entirely.
 */
val DRAWER_DESTINATIONS: List<DrawerDestination> = listOf(
    DrawerDestination(
        screen = Screen.Home,
        title = "Home / Today",
        group = DrawerGroup.Daily
    ),
    DrawerDestination(
        screen = Screen.Settings,
        title = "Settings",
        group = DrawerGroup.System
    )
)
