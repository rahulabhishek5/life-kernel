package com.lifekernel.app.feature.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.lifekernel.app.core.designsystem.components.PosTopBar
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Daily Planner (Home / Today) destination per PRD §8.2 and IMPLEMENTATION_PLAN.md M3.
 * Full task view is implemented in T-010.
 */
@Composable
fun PlannerScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PosTheme.colors.background)
            .testTag("planner_screen")
    ) {
        PosTopBar(
            title = "Today",
            onNavigationClick = onOpenDrawer
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(PosTheme.spacing.space4)
        ) {
            Text(
                text = "PLANNER",
                style = PosTheme.typography.title,
                color = PosTheme.colors.textPrimary
            )
        }
    }
}
