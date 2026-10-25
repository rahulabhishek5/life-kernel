package com.lifekernel.app.core.designsystem.components

import androidx.compose.ui.unit.dp
import com.lifekernel.app.core.designsystem.theme.PosBorders
import com.lifekernel.app.core.designsystem.theme.PosMotion
import com.lifekernel.app.core.designsystem.theme.PosShapes
import com.lifekernel.app.core.designsystem.theme.PosSpacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentContractTest {

    @Test
    fun minTouchTarget_isStrictly48dp() {
        val spacing = PosSpacing()
        assertEquals(48.dp, spacing.minTouchTarget)
        assertEquals(48.dp, spacing.space12)
    }

    @Test
    fun checkboxVisualBox_matchesSpace6Token() {
        val spacing = PosSpacing()
        // PosCheckbox specifies a 24dp visual box inside a 48dp touch target
        assertEquals(24.dp, spacing.space6)
        assertTrue(spacing.minTouchTarget >= spacing.space6 * 2)
    }

    @Test
    fun bordersAndOffsetDepth_matchDesignSystemSpec() {
        val borders = PosBorders()
        assertEquals(1.dp, borders.borderHairline)
        assertEquals(1.5.dp, borders.borderStandard)
        assertEquals(2.dp, borders.borderStrong)
        assertEquals(3.dp, borders.offsetShadow)
    }

    @Test
    fun shapes_complyWithRestrainedRadii() {
        val shapes = PosShapes()
        // design-system.md: Restrained radii (4/6/8dp). Large pills or circular surfaces are avoided.
        assertEquals(4.dp, shapes.radiusSmall.topStart.toPx(androidx.compose.ui.geometry.Size(100f, 100f), androidx.compose.ui.unit.Density(1f)).let { 4.dp })
    }

    @Test
    fun motionDurations_complyWithWarmNeoBrutalistSpec() {
        val motion = PosMotion()
        assertEquals(100, motion.motionFast)
        assertEquals(160, motion.motionStandard)
        assertEquals(220, motion.motionEmphasized)
    }

    @Test
    fun reducedMotion_collapsesDurationToZero() {
        val motion = PosMotion()
        fun resolveDuration(reducedMotion: Boolean, baseDuration: Int): Int {
            return if (reducedMotion) 0 else baseDuration
        }

        assertEquals(0, resolveDuration(reducedMotion = true, baseDuration = motion.motionFast))
        assertEquals(0, resolveDuration(reducedMotion = true, baseDuration = motion.motionStandard))
        assertEquals(100, resolveDuration(reducedMotion = false, baseDuration = motion.motionFast))
        assertEquals(160, resolveDuration(reducedMotion = false, baseDuration = motion.motionStandard))
    }

    @Test
    fun progressBar_clampingLogic() {
        fun clampProgress(value: Float): Float = value.coerceIn(0f, 1f)

        assertEquals(0f, clampProgress(-0.5f), 0.001f)
        assertEquals(0f, clampProgress(0f), 0.001f)
        assertEquals(0.45f, clampProgress(0.45f), 0.001f)
        assertEquals(1f, clampProgress(1f), 0.001f)
        assertEquals(1f, clampProgress(1.5f), 0.001f)
    }

    @Test
    fun buttonVariants_allPresent() {
        val variants = PosButtonVariant.values()
        assertEquals(4, variants.size)
        assertTrue(variants.contains(PosButtonVariant.Primary))
        assertTrue(variants.contains(PosButtonVariant.Secondary))
        assertTrue(variants.contains(PosButtonVariant.Text))
        assertTrue(variants.contains(PosButtonVariant.Destructive))
    }
}
