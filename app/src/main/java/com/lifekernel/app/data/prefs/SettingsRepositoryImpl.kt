package com.lifekernel.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.lifekernel.app.core.designsystem.theme.AccentPalette
import com.lifekernel.app.core.designsystem.theme.AccentPalettes
import com.lifekernel.app.domain.model.DefaultPlannerView
import com.lifekernel.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Implementation of [SettingsRepository] backed by AndroidX DataStore (Preferences).
 *
 * All operations execute off the main thread via the injected [dispatcher] (defaults to [Dispatchers.IO]).
 * Fallback rules:
 * - Unrecognized, null, or corrupted palette IDs safely fall back to [AccentPalettes.Default] (Lime).
 * - Unrecognized or null default planner view values fall back to [DefaultPlannerView.TASKS].
 */
class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SettingsRepository {

    override val themePalette: Flow<AccentPalette> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val paletteId = preferences[SettingsPreferences.KEY_THEME_PALETTE_ID]
                ?: SettingsPreferences.DEFAULT_THEME_PALETTE_ID
            AccentPalettes.fromId(paletteId)
        }
        .flowOn(dispatcher)

    override fun observeThemePalette(): Flow<AccentPalette> = themePalette

    override val defaultPlannerView: Flow<DefaultPlannerView> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val viewName = preferences[SettingsPreferences.KEY_DEFAULT_PLANNER_VIEW]
            parsePlannerView(viewName)
        }
        .flowOn(dispatcher)

    override fun observeDefaultPlannerView(): Flow<DefaultPlannerView> = defaultPlannerView

    override suspend fun setThemePalette(paletteId: String) {
        withContext(dispatcher) {
            dataStore.edit { preferences ->
                // Always persist, including the default "lime" id. Canonicalizing through the
                // registry guarantees the stored id round-trips to the exact selected palette.
                preferences[SettingsPreferences.KEY_THEME_PALETTE_ID] =
                    AccentPalettes.fromId(paletteId).id
            }
        }
    }

    override suspend fun setDefaultPlannerView(view: DefaultPlannerView) {
        withContext(dispatcher) {
            dataStore.edit { preferences ->
                preferences[SettingsPreferences.KEY_DEFAULT_PLANNER_VIEW] = view.name
            }
        }
    }

    private fun parsePlannerView(raw: String?): DefaultPlannerView {
        if (raw == null) return DefaultPlannerView.TASKS
        return try {
            DefaultPlannerView.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            DefaultPlannerView.TASKS
        }
    }
}
