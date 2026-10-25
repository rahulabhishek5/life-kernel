package com.lifekernel.app.feature.planner.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.lifekernel.app.core.designsystem.components.PosButton
import com.lifekernel.app.core.designsystem.components.PosButtonVariant
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Prominent neo-brutalist primary button pinned to the bottom-right of the planner screen.
 * Features accent fill, 2dp accent border, 3dp offset drop block, and tactile press animation.
 */
@Composable
fun PlannerQuickAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PosButton(
        text = "+ NEW TASK",
        onClick = onClick,
        variant = PosButtonVariant.Primary,
        modifier = modifier
            .testTag("planner_quick_add_fab")
            .padding(PosTheme.spacing.space4)
    )
}
