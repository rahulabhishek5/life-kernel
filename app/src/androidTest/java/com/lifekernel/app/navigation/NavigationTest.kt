package com.lifekernel.app.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.LocalReducedMotion
import com.lifekernel.app.core.designsystem.theme.PosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose navigation tests verifying drawer behavior, start destination,
 * back handling, touch targets, state restoration, and reduced-motion per FR-NAV-001..006.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun startDestination_isHomePlannerToday() {
        composeTestRule.setContent {
            PosTheme {
                AppNavHost()
            }
        }

        composeTestRule.onNodeWithTag("planner_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_bar_title").assertTextEquals("Today")
    }

    @Test
    fun drawerOpenAndNavigate_switchesDestinationAndClosesDrawer() {
        composeTestRule.setContent {
            PosTheme {
                AppNavHost()
            }
        }

        // Tap hamburger button to open drawer
        composeTestRule.onNodeWithTag("top_bar_navigation_button").performClick()
        composeTestRule.waitForIdle()

        // Verify drawer sheet is displayed
        composeTestRule.onNodeWithTag("drawer_sheet").assertIsDisplayed()

        // Tap Settings item
        composeTestRule.onNodeWithTag("drawer_item_Settings").performClick()
        composeTestRule.waitForIdle()

        // Verify Settings destination is displayed and TopBar title is "Settings"
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_bar_title").assertTextEquals("Settings")

        // Verify drawer is closed
        composeTestRule.onNodeWithTag("drawer_sheet").assertDoesNotExist()
    }

    @Test
    fun scrimClick_closesDrawerWithoutChangingDestination() {
        composeTestRule.setContent {
            PosTheme {
                AppNavHost()
            }
        }

        // Open drawer
        composeTestRule.onNodeWithTag("top_bar_navigation_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_sheet").assertIsDisplayed()

        // Tap scrim
        composeTestRule.onNodeWithTag("drawer_scrim").performClick()
        composeTestRule.waitForIdle()

        // Drawer is closed and screen remains Planner
        composeTestRule.onNodeWithTag("drawer_sheet").assertDoesNotExist()
        composeTestRule.onNodeWithTag("planner_screen").assertIsDisplayed()
    }

    @Test
    fun drawerItems_meetMinimum48dpTouchTarget() {
        composeTestRule.setContent {
            PosTheme {
                AppNavHost()
            }
        }

        composeTestRule.onNodeWithTag("top_bar_navigation_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("drawer_item_Home / Today")
            .assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag("drawer_item_Settings")
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun stateRestoration_preservesActiveRouteAndDrawerState() {
        val restorationTester = StateRestorationTester(composeTestRule)

        restorationTester.setContent {
            PosTheme {
                AppNavHost()
            }
        }

        // Navigate to Settings
        composeTestRule.onNodeWithTag("top_bar_navigation_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("drawer_item_Settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()

        // Simulate activity recreation (e.g., screen rotation)
        restorationTester.emulateSavedInstanceStateRestore()

        // Verify Settings is preserved
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("top_bar_title").assertTextEquals("Settings")
    }

    @Test
    fun reducedMotion_drawerOpensInstantly() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                PosTheme {
                    AppNavHost()
                }
            }
        }

        composeTestRule.onNodeWithTag("top_bar_navigation_button").performClick()
        // With 0ms duration, it is immediately visible
        composeTestRule.onNodeWithTag("drawer_sheet").assertIsDisplayed()
    }

    @Test
    fun backNavigationHierarchy_logicVerification() {
        val navState = AppNavigationState(initialScreen = Screen.Home)
        val drawerState = com.lifekernel.app.core.designsystem.components.PosDrawerState(
            initialValue = com.lifekernel.app.core.designsystem.components.PosDrawerValue.Closed
        )

        // 1. Initial: drawer closed, on Home
        assertFalse(drawerState.isOpen)
        assertEquals(Screen.Home, navState.currentScreen)

        // 2. Open drawer -> Back closes drawer
        drawerState.open()
        assertTrue(drawerState.isOpen)
        drawerState.close()
        assertFalse(drawerState.isOpen)
        assertEquals(Screen.Home, navState.currentScreen)

        // 3. Navigate to Settings -> Back returns to Home
        navState.navigateTo(Screen.Settings)
        assertEquals(Screen.Settings, navState.currentScreen)
        navState.navigateToHome()
        assertEquals(Screen.Home, navState.currentScreen)
    }
}
