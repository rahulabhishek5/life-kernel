package com.lifekernel.app.feature.planner.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.lifekernel.app.core.designsystem.components.PosCard
import com.lifekernel.app.core.designsystem.components.PosProgressBar
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Header and progress card for the daily planner per design-system.md §8.1.
 */
@Composable
fun PlannerHeader(
    dateHeader: String,
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PosTheme.spacing.space4)
    ) {
        if (dateHeader.isNotEmpty()) {
            Text(
                text = dateHeader,
                style = PosTheme.typography.label.copy(fontFamily = PosFonts.mono),
                color = PosTheme.colors.textMuted,
                modifier = Modifier.testTag("planner_date_header")
            )
            Spacer(modifier = Modifier.height(PosTheme.spacing.space2))
        }

        PosCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("planner_progress_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PosTheme.spacing.space3)
            ) {
                Text(
                    text = "$completedCount / $totalCount DONE",
                    style = PosTheme.typography.title.copy(fontFamily = PosFonts.mono),
                    color = PosTheme.colors.textPrimary,
                    modifier = Modifier.testTag("planner_progress_text")
                )

                Spacer(modifier = Modifier.height(PosTheme.spacing.space2))

                PosProgressBar(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_progress_bar")
                )
            }
        }
    }
}
