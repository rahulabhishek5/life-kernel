package com.lifekernel.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.lifekernel.app.core.designsystem.theme.AccentPalettes
import com.lifekernel.app.domain.model.DefaultPlannerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit tests for [SettingsRepositoryImpl] backed by AndroidX DataStore (Preferences).
 * Verifies initial defaults, persistence, update mutations, and fallback handling for unrecognized values.
 */
class SettingsRepositoryTest {

    private lateinit var tempFile: File
    private lateinit var testScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        tempFile = File.createTempFile("test_settings_prefs", ".preferences_pb")
        testScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStore = SettingsPreferences.createTestDataStore(tempFile, testScope)
        repository = SettingsRepositoryImpl(dataStore, Dispatchers.IO)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    @Test
    fun defaultValues_emitsLimePaletteAndTasksPlannerView() = runBlocking {
        val initialPalette = repository.observeThemePalette().first()
        val initialView = repository.observeDefaultPlannerView().first()

        assertEquals("Default palette must be Lime", AccentPalettes.Lime, initialPalette)
        assertEquals("Default planner view must be TASKS", DefaultPlannerView.TASKS, initialView)
    }

    @Test
    fun updateThemePalette_persistsAndEmitsUpdatedPalette() = runBlocking {
        repository.setThemePalette("orange")
        val updatedPalette = repository.observeThemePalette().first()
        assertEquals("Orange palette must be emitted", AccentPalettes.Orange, updatedPalette)

        repository.setThemePalette("purple")
        val purplePalette = repository.observeThemePalette().first()
        assertEquals("Purple palette must be emitted", AccentPalettes.Purple, purplePalette)

        repository.setThemePalette("mono")
        val monoPalette = repository.observeThemePalette().first()
        assertEquals("Mono palette must be emitted", AccentPalettes.Mono, monoPalette)
    }

    @Test
    fun unknownPaletteId_safelyFallsBackToLime() = runBlocking {
        repository.setThemePalette("neon_pink")
        val fallbackPalette = repository.observeThemePalette().first()
        assertEquals(
            "Unknown palette ID must safely fall back to default Lime palette",
            AccentPalettes.Lime,
            fallbackPalette
        )

        repository.setThemePalette("")
        val emptyFallback = repository.observeThemePalette().first()
        assertEquals(
            "Empty string palette ID must safely fall back to default Lime palette",
            AccentPalettes.Lime,
            emptyFallback
        )
    }

    @Test
    fun limePalette_resolvesExplicitlyToLime_andNeverRed() = runBlocking {
        repository.setThemePalette("lime")
        val emitted = repository.observeThemePalette().first()
        assertEquals(AccentPalettes.Lime, emitted)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFA3E635), emitted.accent)
        org.junit.Assert.assertNotEquals(androidx.compose.ui.graphics.Color(0xFFF87171), emitted.accent)
    }

    @Test
    fun updateDefaultPlannerView_persistsAndEmitsUpdatedView() = runBlocking {
        repository.setDefaultPlannerView(DefaultPlannerView.TIMETABLE)
        val timetable = repository.observeDefaultPlannerView().first()
        assertEquals("TIMETABLE view must be emitted", DefaultPlannerView.TIMETABLE, timetable)

        repository.setDefaultPlannerView(DefaultPlannerView.TASKS)
        val tasks = repository.observeDefaultPlannerView().first()
        assertEquals("TASKS view must be emitted", DefaultPlannerView.TASKS, tasks)
    }

    @Test
    fun unknownDefaultPlannerView_safelyFallsBackToTasks() = runBlocking {
        dataStore.edit { preferences ->
            preferences[SettingsPreferences.KEY_DEFAULT_PLANNER_VIEW] = "CORRUPTED_VIEW_NAME"
        }

        val fallbackView = repository.observeDefaultPlannerView().first()
        assertEquals(
            "Corrupted or invalid planner view string must safely fall back to TASKS",
            DefaultPlannerView.TASKS,
            fallbackView
        )
    }

    @Test
    fun allSevenAccentPalettes_persistAndResolveCorrectly() = runBlocking {
        for (palette in AccentPalettes.all) {
            repository.setThemePalette(palette.id)
            val emitted = repository.observeThemePalette().first()
            assertEquals("Palette ${palette.id} must resolve correctly", palette, emitted)
        }
    }
}
