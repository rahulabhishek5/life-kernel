package com.lifekernel.app.domain.repository

import com.lifekernel.app.core.designsystem.theme.AccentPalette
import com.lifekernel.app.domain.model.DefaultPlannerView
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for application settings and preference storage (PRD §8.13, ARCHITECTURE.md §5).
 *
 * Invariant: DataStore holds only small scalar preferences and bookkeeping flags. User records
 * (tasks, tags, notes) must never be stored here (ARCHITECTURE.md Invariant #6).
 */
interface SettingsRepository {
    /**
     * Reactive stream of the active accent palette. Invalid or unknown palette IDs safely fall back to Lime.
     */
    val themePalette: Flow<AccentPalette>

    /**
     * Observes the active accent palette reactively.
     */
    fun observeThemePalette(): Flow<AccentPalette> = themePalette

    /**
     * Reactive stream of the default planner launch view.
     */
    val defaultPlannerView: Flow<DefaultPlannerView>

    /**
     * Observes the default planner launch view reactively.
     */
    fun observeDefaultPlannerView(): Flow<DefaultPlannerView> = defaultPlannerView

    /**
     * Updates the persistent theme palette identifier.
     */
    suspend fun setThemePalette(paletteId: String)

    /**
     * Updates the persistent default planner launch view.
     */
    suspend fun setDefaultPlannerView(view: DefaultPlannerView)
}
