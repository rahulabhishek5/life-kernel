package com.lifekernel.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * AndroidX DataStore configuration, preference keys, and default values for application settings
 * per ARCHITECTURE.md §1 & §4 and PRD §8.13.
 *
 * Invariant: DataStore holds only small scalar preferences and bookkeeping flags. User records
 * (tasks, tags, notes) must never be stored here (ARCHITECTURE.md Invariant #6).
 */
object SettingsPreferences {
    const val DATASTORE_NAME = "settings_preferences"

    val KEY_THEME_PALETTE_ID = stringPreferencesKey("theme_palette_id")
    val KEY_DEFAULT_PLANNER_VIEW = stringPreferencesKey("default_planner_view")

    const val DEFAULT_THEME_PALETTE_ID = "lime"
    const val DEFAULT_PLANNER_VIEW = "TASKS"

    /**
     * Creates a production DataStore instance scoped to [Dispatchers.IO].
     */
    fun createDataStore(
        context: Context,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }

    /**
     * Creates a DataStore instance backed by the provided [file] for isolated testing.
     */
    fun createTestDataStore(
        file: File,
        scope: CoroutineScope
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )
    }
}
