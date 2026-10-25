package com.lifekernel.app.feature.settings

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.AccentPalette
import com.lifekernel.app.core.designsystem.theme.AccentPalettes
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.domain.model.DefaultPlannerView
import com.lifekernel.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for [SettingsScreen] per PRD §8.13 (FR-SET-001, FR-SET-002) and design-system.md §4.
 * Verifies rendering of the 7 palettes, touch target sizes (>= 48dp), selection interactions,
 * and default planner view segmented toggle.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeSettingsTestRepository
    private lateinit var viewModel: SettingsViewModel
    private var drawerOpenClicked = false

    @Before
    fun setUp() {
        drawerOpenClicked = false
        fakeRepository = FakeSettingsTestRepository()
        viewModel = SettingsViewModel(
            settingsRepository = fakeRepository
        )

        composeTestRule.setContent {
            PosTheme {
                SettingsScreen(
                    onOpenDrawer = { drawerOpenClicked = true },
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun settingsScreen_rendersTopBarAndAllSevenPaletteOptions() {
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("ACCENT THEME").assertIsDisplayed()

        for (palette in AccentPalettes.all) {
            composeTestRule.onNodeWithTag("palette_option_${palette.id}")
                .performScrollTo()
                .assertIsDisplayed()
            composeTestRule.onNodeWithText(palette.displayName).assertIsDisplayed()
        }

        composeTestRule.onNodeWithText("DEFAULT PLANNER VIEW").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("default_planner_view_toggle").assertIsDisplayed()
    }

    @Test
    fun tapPaletteOption_invokesSelectionHandlerAndUpdatesState() {
        // Initially, default Lime palette is selected
        composeTestRule.onNodeWithTag("palette_option_lime")
            .performScrollTo()
            .assertTextContains("ACTIVE")

        // Select Orange palette
        composeTestRule.onNodeWithTag("palette_option_orange")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals("orange", fakeRepository.currentPaletteId)
        composeTestRule.onNodeWithTag("palette_option_orange")
            .assertTextContains("ACTIVE")

        // Select Purple palette
        composeTestRule.onNodeWithTag("palette_option_purple")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals("purple", fakeRepository.currentPaletteId)
        composeTestRule.onNodeWithTag("palette_option_purple")
            .assertTextContains("ACTIVE")
    }

    @Test
    fun tapDefaultPlannerView_invokesSelectionHandlerAndUpdatesState() {
        composeTestRule.onNodeWithTag("default_planner_view_toggle")
            .performScrollTo()
            .assertIsDisplayed()

        // Select Timetable view
        composeTestRule.onNodeWithText("Timetable")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(DefaultPlannerView.TIMETABLE, fakeRepository.currentView)

        // Select Tasks view
        composeTestRule.onNodeWithText("Tasks")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        assertEquals(DefaultPlannerView.TASKS, fakeRepository.currentView)
    }

    @Test
    fun allInteractiveSelectors_satisfyMinimum48dpTouchTarget() {
        for (palette in AccentPalettes.all) {
            composeTestRule.onNodeWithTag("palette_option_${palette.id}")
                .performScrollTo()
                .assertHeightIsAtLeast(48.dp)
        }

        composeTestRule.onNodeWithTag("default_planner_view_toggle")
            .performScrollTo()
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun topBarNavigationButton_triggersOpenDrawerCallback() {
        composeTestRule.onNodeWithText("Open navigation drawer").performClick()
        assertEquals(true, drawerOpenClicked)
    }
}

private class FakeSettingsTestRepository : SettingsRepository {
    var currentPaletteId: String = AccentPalettes.Default.id
    var currentView: DefaultPlannerView = DefaultPlannerView.TASKS

    private val paletteFlow = MutableStateFlow(AccentPalettes.Default)
    private val viewFlow = MutableStateFlow(DefaultPlannerView.TASKS)

    override val themePalette: Flow<AccentPalette> = paletteFlow
    override fun observeThemePalette(): Flow<AccentPalette> = paletteFlow

    override val defaultPlannerView: Flow<DefaultPlannerView> = viewFlow
    override fun observeDefaultPlannerView(): Flow<DefaultPlannerView> = viewFlow

    override suspend fun setThemePalette(paletteId: String) {
        currentPaletteId = paletteId
        paletteFlow.value = AccentPalettes.fromId(paletteId)
    }

    override suspend fun setDefaultPlannerView(view: DefaultPlannerView) {
        currentView = view
        viewFlow.value = view
    }
}
