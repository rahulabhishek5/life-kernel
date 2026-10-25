package com.lifekernel.app.feature.settings

import com.lifekernel.app.core.designsystem.theme.AccentPalette
import com.lifekernel.app.core.designsystem.theme.AccentPalettes
import com.lifekernel.app.domain.model.DefaultPlannerView

/**
 * UI state for the Settings screen per PRD §8.13 (FR-SET-001, FR-SET-002).
 */
data class SettingsUiState(
    val selectedPalette: AccentPalette = AccentPalettes.Default,
    val availablePalettes: List<AccentPalette> = AccentPalettes.all,
    val selectedDefaultView: DefaultPlannerView = DefaultPlannerView.TASKS
)
