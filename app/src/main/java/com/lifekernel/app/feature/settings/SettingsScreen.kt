package com.lifekernel.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifekernel.app.core.designsystem.components.PosCard
import com.lifekernel.app.core.designsystem.components.PosSegmentedToggle
import com.lifekernel.app.core.designsystem.components.PosTopBar
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.di.LocalAppContainer
import com.lifekernel.app.domain.model.DefaultPlannerView

/**
 * Settings screen per PRD §8.13 (FR-SET-001, FR-SET-002) and design-system.md §4.
 * Provides live accent theme selector (persisted in DataStore) and default planner view selector.
 */
@Composable
fun SettingsScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel? = null
) {
    val container = if (viewModel == null) LocalAppContainer.current else null
    val resolvedViewModel = viewModel ?: remember(container) {
        requireNotNull(container).createSettingsViewModel()
    }
    val uiState by resolvedViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PosTheme.colors.background)
            .testTag("settings_screen")
    ) {
        PosTopBar(
            title = "Settings",
            onNavigationClick = onOpenDrawer
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PosTheme.spacing.space4)
        ) {
            // ACCENT THEME SECTION
            Text(
                text = "ACCENT THEME",
                style = PosTheme.typography.label.copy(
                    fontFamily = PosFonts.mono,
                    fontWeight = FontWeight.SemiBold
                ),
                color = PosTheme.colors.textMuted,
                modifier = Modifier.padding(top = PosTheme.spacing.space4, bottom = PosTheme.spacing.space2)
            )

            uiState.availablePalettes.forEach { palette ->
                val isSelected = palette.id == uiState.selectedPalette.id
                PosCard(
                    isSelected = isSelected,
                    onClick = { resolvedViewModel.selectPalette(palette) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = PosTheme.spacing.space2)
                        .testTag("palette_option_${palette.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = PosTheme.spacing.minTouchTarget),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Visual swatch of the accent color
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(PosTheme.shapes.radiusSmall)
                                .background(palette.accent)
                                .border(
                                    width = PosTheme.borders.borderStandard,
                                    color = if (isSelected) PosTheme.colors.outlineStrong else PosTheme.colors.outlineSubtle,
                                    shape = PosTheme.shapes.radiusSmall
                                )
                        )

                        Spacer(modifier = Modifier.width(PosTheme.spacing.space3))

                        Text(
                            text = palette.displayName,
                            style = PosTheme.typography.body.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = PosTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Text(
                                text = "ACTIVE",
                                style = PosTheme.typography.label.copy(
                                    fontFamily = PosFonts.mono,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PosTheme.colors.accent
                            )
                        }
                    }
                }
            }

            // DEFAULT PLANNER VIEW SECTION
            Text(
                text = "DEFAULT PLANNER VIEW",
                style = PosTheme.typography.label.copy(
                    fontFamily = PosFonts.mono,
                    fontWeight = FontWeight.SemiBold
                ),
                color = PosTheme.colors.textMuted,
                modifier = Modifier.padding(top = PosTheme.spacing.space6, bottom = PosTheme.spacing.space2)
            )

            Text(
                text = "Choose which view opens when launching Life Kernel.",
                style = PosTheme.typography.bodySmall,
                color = PosTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = PosTheme.spacing.space3)
            )

            val plannerViewOptions = listOf("Tasks", "Timetable")
            val selectedIndex = if (uiState.selectedDefaultView == DefaultPlannerView.TASKS) 0 else 1

            PosSegmentedToggle(
                options = plannerViewOptions,
                selectedIndex = selectedIndex,
                onOptionSelected = { index ->
                    val chosen = if (index == 0) DefaultPlannerView.TASKS else DefaultPlannerView.TIMETABLE
                    resolvedViewModel.selectDefaultPlannerView(chosen)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("default_planner_view_toggle")
            )

            Spacer(modifier = Modifier.height(PosTheme.spacing.space8))
        }
    }
}
