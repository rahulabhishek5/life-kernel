package com.lifekernel.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lifekernel.app.LifeKernelApp
import com.lifekernel.app.core.designsystem.components.PosDrawer
import com.lifekernel.app.core.designsystem.components.PosDrawerDivider
import com.lifekernel.app.core.designsystem.components.PosDrawerHeader
import com.lifekernel.app.core.designsystem.components.PosDrawerItem
import com.lifekernel.app.core.designsystem.components.PosDrawerState
import com.lifekernel.app.core.designsystem.components.rememberPosDrawerState
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.di.AppContainer
import com.lifekernel.app.di.LocalAppContainer
import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.feature.planner.PlannerScreen
import com.lifekernel.app.feature.settings.SettingsScreen
import com.lifekernel.app.feature.tasks.TaskDetailScreen

/**
 * Top-level application shell and navigation host per ARCHITECTURE.md §12a.
 * Hosts modal navigation drawer and switches between destinations with a 160ms crossfade.
 *
 * Implements predictable back navigation hierarchy:
 * 1. Closes drawer if open.
 * 2. Pops back stack if on detail destination or returns to Home / Today if on Settings.
 * 3. Exits when already on Home / Today.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navigationState: AppNavigationState = rememberAppNavigationState(),
    drawerState: PosDrawerState = rememberPosDrawerState(),
    appContainer: AppContainer? = null
) {
    val context = LocalContext.current
    val resolvedContainer = appContainer
        ?: runCatching { LocalAppContainer.current }.getOrNull()
        ?: (context.applicationContext as? LifeKernelApp)?.appContainer

    if (resolvedContainer != null) {
        CompositionLocalProvider(LocalAppContainer provides resolvedContainer) {
            AppNavHostContent(
                modifier = modifier,
                navigationState = navigationState,
                drawerState = drawerState
            )
        }
    } else {
        AppNavHostContent(
            modifier = modifier,
            navigationState = navigationState,
            drawerState = drawerState
        )
    }
}

@Composable
private fun AppNavHostContent(
    modifier: Modifier = Modifier,
    navigationState: AppNavigationState,
    drawerState: PosDrawerState
) {
    // Back navigation rule 2: If drawer is closed and screen is not Home, Back pops back stack.
    // If screen is Home, this handler is disabled so the system back action exits the app.
    BackHandler(enabled = drawerState.isClosed && navigationState.currentScreen != Screen.Home) {
        if (!navigationState.navigateBack()) {
            navigationState.navigateToHome()
        }
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
            when (val currentScreen = screen) {
                Screen.Home -> PlannerScreen(
                    onOpenDrawer = { drawerState.open() },
                    onTaskClick = { task ->
                        navigationState.navigateTo(Screen.TaskDetail(task.id))
                    }
                )
                Screen.Settings -> SettingsScreen(
                    onOpenDrawer = { drawerState.open() }
                )
                is Screen.TaskDetail -> TaskDetailScreen(
                    taskId = currentScreen.taskId,
                    onBack = {
                        if (!navigationState.navigateBack()) {
                            navigationState.navigateToHome()
                        }
                    }
                )
            }
        }
    }
}
