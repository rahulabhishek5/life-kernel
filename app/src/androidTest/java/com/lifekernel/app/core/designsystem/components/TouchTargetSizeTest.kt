package com.lifekernel.app.core.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.PosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TouchTargetSizeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun posButton_meetsMinimum48dpTouchTarget() {
        composeTestRule.setContent {
            PosTheme {
                PosButton(
                    text = "OK",
                    onClick = {},
                    modifier = Modifier.testTag("test_button")
                )
            }
        }

        composeTestRule.onNodeWithTag("test_button")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun posCheckbox_meetsMinimum48dpTouchTarget() {
        composeTestRule.setContent {
            PosTheme {
                PosCheckbox(
                    checked = false,
                    onCheckedChange = {},
                    modifier = Modifier.testTag("test_checkbox")
                )
            }
        }

        composeTestRule.onNodeWithTag("test_checkbox")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun posChip_meetsMinimum48dpTouchTarget() {
        composeTestRule.setContent {
            PosTheme {
                PosChip(
                    text = "Tag",
                    selected = false,
                    onClick = {},
                    modifier = Modifier.testTag("test_chip")
                )
            }
        }

        composeTestRule.onNodeWithTag("test_chip")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }
}
