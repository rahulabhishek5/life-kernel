package com.lifekernel.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationStateTest {

    @Test
    fun initialScreen_isHome() {
        val navState = AppNavigationState()
        assertEquals(Screen.Home, navState.currentScreen)
    }

    @Test
    fun navigateTo_switchesDestination() {
        val navState = AppNavigationState()
        navState.navigateTo(Screen.Settings)
        assertEquals(Screen.Settings, navState.currentScreen)

        navState.navigateToHome()
        assertEquals(Screen.Home, navState.currentScreen)
    }

    @Test
    fun screenFromRoute_handlesKnownAndUnknownRoutes() {
        assertEquals(Screen.Home, Screen.fromRoute("home"))
        assertEquals(Screen.Settings, Screen.fromRoute("settings"))
        assertEquals(Screen.TaskDetail(42L), Screen.fromRoute("tasks/42"))
        assertEquals(Screen.Home, Screen.fromRoute("unknown_route"))
        assertEquals(Screen.Home, Screen.fromRoute(null))
    }

    @Test
    fun navigateBack_popsDetailDestinationBackToHome() {
        val navState = AppNavigationState()
        navState.navigateTo(Screen.TaskDetail(10L))
        assertEquals(Screen.TaskDetail(10L), navState.currentScreen)

        val popped = navState.navigateBack()
        assertTrue(popped)
        assertEquals(Screen.Home, navState.currentScreen)

        // At root, navigateBack returns false
        val poppedAtRoot = navState.navigateBack()
        org.junit.Assert.assertFalse(poppedAtRoot)
        assertEquals(Screen.Home, navState.currentScreen)
    }

    @Test
    fun drawerDestinations_omitsUnbuiltModules() {
        // Strict FR-NAV-002 check: only Home/Today and Settings are in the drawer
        val routes = DRAWER_DESTINATIONS.map { it.screen.route }
        assertEquals(2, routes.size)
        assertTrue(routes.contains("home"))
        assertTrue(routes.contains("settings"))
    }
}
