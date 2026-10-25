package com.lifekernel.app.core.designsystem.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.PosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SegmentedToggleSelectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun segmentedToggle_exposesTabRoleAndSelectionState() {
        var selectedIndex by mutableIntStateOf(0)

        composeTestRule.setContent {
            PosTheme {
                PosSegmentedToggle(
                    options = listOf("Tasks", "Timetable"),
                    selectedIndex = selectedIndex,
                    onOptionSelected = { selectedIndex = it }
                )
            }
        }

        // Verify Tab roles
        composeTestRule.onNodeWithText("Tasks")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))

        composeTestRule.onNodeWithText("Timetable")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))

        // Click second tab
        composeTestRule.onNodeWithText("Timetable").performClick()

        assertEquals(1, selectedIndex)
        composeTestRule.onNodeWithText("Tasks")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
        composeTestRule.onNodeWithText("Timetable")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
    }
}
