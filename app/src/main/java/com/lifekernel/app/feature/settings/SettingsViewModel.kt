package com.lifekernel.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifekernel.app.core.designsystem.theme.AccentPalette
import com.lifekernel.app.core.designsystem.theme.AccentPalettes
import com.lifekernel.app.domain.model.DefaultPlannerView
import com.lifekernel.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing the settings state per PRD §8.13 (FR-SET-001, FR-SET-002) and ARCHITECTURE.md §6.
 * Exposes reactive UI state for theme selection and default planner launch view.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeThemePalette(),
        settingsRepository.observeDefaultPlannerView()
    ) { palette, defaultView ->
        SettingsUiState(
            selectedPalette = palette,
            availablePalettes = AccentPalettes.all,
            selectedDefaultView = defaultView
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun selectPalette(palette: AccentPalette) {
        scope.launch {
            // Resolve through the registry so the canonical id (including "lime") is always
            // persisted, even when the palette object arrives with a differently cased id.
            settingsRepository.setThemePalette(AccentPalettes.fromId(palette.id).id)
        }
    }

    fun selectDefaultPlannerView(view: DefaultPlannerView) {
        scope.launch {
            settingsRepository.setDefaultPlannerView(view)
        }
    }
}
