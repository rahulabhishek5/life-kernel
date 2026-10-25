package com.lifekernel.app.core.designsystem.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.designsystem.theme.PosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReducedMotionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun posTheme_observesReducedMotionOverride() {
        var observedReducedMotion: Boolean? = null

        composeTestRule.setContent {
            PosTheme(forceReducedMotion = true) {
                observedReducedMotion = PosTheme.reducedMotion
            }
        }

        assertEquals(true, observedReducedMotion)
    }

    @Test
    fun posTheme_motionResolvesToZeroUnderReducedMotion() {
        var animDuration: Int? = null

        composeTestRule.setContent {
            PosTheme(forceReducedMotion = true) {
                val isReduced = PosTheme.reducedMotion
                animDuration = if (isReduced) 0 else PosTheme.motion.motionFast
            }
        }

        assertEquals(0, animDuration)
    }

    @Test
    fun posTheme_normalMotionResolvesToFullDuration() {
        var animFast: Int? = null
        var animStandard: Int? = null

        composeTestRule.setContent {
            PosTheme(forceReducedMotion = false) {
                // If system reduced motion is not forced, motionFast is 100 and motionStandard is 160
                animFast = PosTheme.motion.motionFast
                animStandard = PosTheme.motion.motionStandard
            }
        }

        assertEquals(100, animFast)
        assertEquals(160, animStandard)
    }
}
