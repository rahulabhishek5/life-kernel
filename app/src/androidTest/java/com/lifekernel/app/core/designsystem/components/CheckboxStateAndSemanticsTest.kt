package com.lifekernel.app.core.designsystem.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.PosTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckboxStateAndSemanticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun checkbox_exposesRoleAndStateDescription() {
        composeTestRule.setContent {
            PosTheme {
                PosCheckbox(
                    checked = false,
                    onCheckedChange = {},
                    contentDescription = "Task completion",
                    modifier = Modifier.testTag("checkbox")
                )
            }
        }

        composeTestRule.onNodeWithTag("checkbox")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Not checked"))
            .assertContentDescriptionEquals("Task completion")
    }

    @Test
    fun checkbox_toggleUpdatesStateAndInvokesCallback() {
        var checkedState by mutableStateOf(false)
        var callbackValue: Boolean? = null

        composeTestRule.setContent {
            PosTheme {
                PosCheckbox(
                    checked = checkedState,
                    onCheckedChange = {
                        callbackValue = it
                        checkedState = it
                    },
                    modifier = Modifier.testTag("checkbox")
                )
            }
        }

        composeTestRule.onNodeWithTag("checkbox").performClick()

        assertEquals(true, callbackValue)
        composeTestRule.onNodeWithTag("checkbox")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Checked"))
    }

    @Test
    fun checkbox_disabledDoesNotInvokeCallback() {
        var callbackInvoked = false

        composeTestRule.setContent {
            PosTheme {
                PosCheckbox(
                    checked = false,
                    enabled = false,
                    onCheckedChange = { callbackInvoked = true },
                    modifier = Modifier.testTag("disabled_checkbox")
                )
            }
        }

        composeTestRule.onNodeWithTag("disabled_checkbox").performClick()
        assertEquals(false, callbackInvoked)
    }
}
