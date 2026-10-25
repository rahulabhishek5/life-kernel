package com.lifekernel.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lifekernel.app.core.designsystem.components.PosDrawer
import com.lifekernel.app.core.designsystem.components.PosDrawerDivider
import com.lifekernel.app.core.designsystem.components.PosDrawerHeader
import com.lifekernel.app.core.designsystem.components.PosDrawerItem
import com.lifekernel.app.core.designsystem.components.PosDrawerState
import com.lifekernel.app.core.designsystem.components.rememberPosDrawerState
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.feature.planner.PlannerScreen
import com.lifekernel.app.feature.settings.SettingsScreen

/**
 * Top-level application shell and navigation host per ARCHITECTURE.md §12a.
 * Hosts modal navigation drawer and switches between top-level destinations with a 160ms crossfade.
 *
 * Implements predictable back navigation hierarchy:
 * 1. Closes drawer if open.
 * 2. Returns to Home / Today if on Settings.
 * 3. Pops backstack / exits when already on Home / Today.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navigationState: AppNavigationState = rememberAppNavigationState(),
    drawerState: PosDrawerState = rememberPosDrawerState()
) {
    // Back navigation rule 2: If drawer is closed and screen is not Home, Back returns to Home.
    // If screen is Home, this handler is disabled so the system back action exits the app.
    BackHandler(enabled = drawerState.isClosed && navigationState.currentScreen != Screen.Home) {
        navigationState.navigateToHome()
    }

    PosDrawer(
        drawerState = drawerState,
        drawerContent = {
            PosDrawerHeader()
            PosDrawerDivider()

            val dailyDestinations = DRAWER_DESTINATIONS.filter { it.group == DrawerGroup.Daily }
            val systemDestinations = DRAWER_DESTINATIONS.filter { it.group == DrawerGroup.System }

            dailyDestinations.forEach { dest ->
                PosDrawerItem(
                    title = dest.title,
                    isSelected = navigationState.currentScreen == dest.screen,
                    onClick = {
                        drawerState.close()
                        navigationState.navigateTo(dest.screen)
                    }
                )
            }

            if (systemDestinations.isNotEmpty()) {
                PosDrawerDivider()
                systemDestinations.forEach { dest ->
                    PosDrawerItem(
                        title = dest.title,
                        isSelected = navigationState.currentScreen == dest.screen,
                        onClick = {
                            drawerState.close()
                            navigationState.navigateTo(dest.screen)
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) {
        val duration = if (PosTheme.reducedMotion) 0 else PosTheme.motion.motionStandard
        Crossfade(
            targetState = navigationState.currentScreen,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
            label = "top_level_crossfade",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                Screen.Home -> PlannerScreen(
                    onOpenDrawer = { drawerState.open() }
                )
                Screen.Settings -> SettingsScreen(
                    onOpenDrawer = { drawerState.open() }
                )
            }
        }
    }
}
