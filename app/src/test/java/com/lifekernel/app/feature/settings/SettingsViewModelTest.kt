package com.lifekernel.app.feature.settings

import com.lifekernel.app.core.designsystem.theme.AccentPalette
import com.lifekernel.app.core.designsystem.theme.AccentPalettes
import com.lifekernel.app.domain.model.DefaultPlannerView
import com.lifekernel.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SettingsViewModel].
 * Verifies UI state emissions and dispatch calls for theme palette selection and default planner view selection.
 */
class SettingsViewModelTest {

    private lateinit var testScope: CoroutineScope
    private lateinit var fakeRepository: FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel
    private lateinit var collectorJob: Job

    @Before
    fun setUp() {
        testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        fakeRepository = FakeSettingsRepository()
        viewModel = SettingsViewModel(
            settingsRepository = fakeRepository,
            coroutineScope = testScope
        )
        // Keep state collection active
        collectorJob = testScope.launch {
            viewModel.uiState.collect {}
        }
    }

    @After
    fun tearDown() {
        collectorJob.cancel()
        testScope.cancel()
    }

    @Test
    fun initialState_emitsDefaultLimePaletteAndTasksView() = runBlocking {
        val state = viewModel.uiState.value

        assertEquals("Initial palette must be Lime default", AccentPalettes.Default, state.selectedPalette)
        assertEquals("Initial planner view must be TASKS", DefaultPlannerView.TASKS, state.selectedDefaultView)
        assertEquals("Available palettes must contain all 7 palettes", 7, state.availablePalettes.size)
    }

    @Test
    fun selectPalette_updatesRepositoryAndState() = runBlocking {
        viewModel.selectPalette(AccentPalettes.Orange)

        assertEquals("Repository palette ID must be orange", "orange", fakeRepository.currentPaletteId)
        assertEquals("ViewModel selectedPalette must be Orange", AccentPalettes.Orange, viewModel.uiState.value.selectedPalette)

        viewModel.selectPalette(AccentPalettes.Blue)
        assertEquals("Repository palette ID must be blue", "blue", fakeRepository.currentPaletteId)
        assertEquals("ViewModel selectedPalette must be Blue", AccentPalettes.Blue, viewModel.uiState.value.selectedPalette)
    }

    @Test
    fun selectLimeAfterAnotherPalette_alwaysDispatchesAndRestoresLime() = runBlocking {
        viewModel.selectPalette(AccentPalettes.Blue)
        assertEquals("blue", fakeRepository.currentPaletteId)

        viewModel.selectPalette(AccentPalettes.Lime)

        assertEquals("Lime selection must never be swallowed", "lime", fakeRepository.currentPaletteId)
        assertEquals(AccentPalettes.Lime, viewModel.uiState.value.selectedPalette)
        assertEquals("Selecting the current palette must still dispatch", 2, fakeRepository.setThemePaletteCalls)

        viewModel.selectPalette(AccentPalettes.Lime)
        assertEquals("A repeated Lime tap must still dispatch", 3, fakeRepository.setThemePaletteCalls)
    }

    @Test
    fun selectDefaultPlannerView_updatesRepositoryAndState() = runBlocking {
        viewModel.selectDefaultPlannerView(DefaultPlannerView.TIMETABLE)

        assertEquals("Repository view must be TIMETABLE", DefaultPlannerView.TIMETABLE, fakeRepository.currentView)
        assertEquals("ViewModel selectedDefaultView must be TIMETABLE", DefaultPlannerView.TIMETABLE, viewModel.uiState.value.selectedDefaultView)

        viewModel.selectDefaultPlannerView(DefaultPlannerView.TASKS)
        assertEquals("Repository view must be TASKS", DefaultPlannerView.TASKS, fakeRepository.currentView)
        assertEquals("ViewModel selectedDefaultView must be TASKS", DefaultPlannerView.TASKS, viewModel.uiState.value.selectedDefaultView)
    }
}

/**
 * In-memory test fake for [SettingsRepository].
 */
private class FakeSettingsRepository : SettingsRepository {
    var currentPaletteId: String = AccentPalettes.Default.id
    var currentView: DefaultPlannerView = DefaultPlannerView.TASKS
    var setThemePaletteCalls: Int = 0
        private set

    private val paletteFlow = MutableStateFlow(AccentPalettes.Default)
    private val viewFlow = MutableStateFlow(DefaultPlannerView.TASKS)

    override val themePalette: Flow<AccentPalette> = paletteFlow
    override fun observeThemePalette(): Flow<AccentPalette> = paletteFlow

    override val defaultPlannerView: Flow<DefaultPlannerView> = viewFlow
    override fun observeDefaultPlannerView(): Flow<DefaultPlannerView> = viewFlow

    override suspend fun setThemePalette(paletteId: String) {
        setThemePaletteCalls++
        currentPaletteId = paletteId
        paletteFlow.value = AccentPalettes.fromId(paletteId)
    }

    override suspend fun setDefaultPlannerView(view: DefaultPlannerView) {
        currentView = view
        viewFlow.value = view
    }
}
