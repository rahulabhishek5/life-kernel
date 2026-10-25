package com.lifekernel.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Validates WCAG contrast ratios across all 7 accent palettes per design-system.md §4.
 *
 * Requirements:
 * - Text vs background / surfaces: at least 4.5:1
 * - Control boundaries vs background: at least 3:1
 * - onAccent (black) vs accent: at least 4.5:1
 */
class PaletteContrastTest {

    private val minTextContrast = 4.5
    private val minUiContrast = 3.0

    @Test
    fun allPalettes_meetContrastAgainstBlackBackground() {
        for (palette in AccentPalettes.all) {
            val ratio = calculateContrastRatio(palette.accent, PosColorBackground)
            assertTrue(
                "Palette '${palette.displayName}' accent vs background contrast ratio $ratio is below $minUiContrast:1",
                ratio >= minUiContrast
            )
        }
    }

    @Test
    fun allPalettes_meetContrastAgainstSurfaces() {
        for (palette in AccentPalettes.all) {
            val vsSurface = calculateContrastRatio(palette.accent, PosColorSurface)
            assertTrue(
                "Palette '${palette.displayName}' accent vs surface contrast ratio $vsSurface is below $minUiContrast:1",
                vsSurface >= minUiContrast
            )

            val vsSurfaceVariant = calculateContrastRatio(palette.accent, PosColorSurfaceVariant)
            assertTrue(
                "Palette '${palette.displayName}' accent vs surfaceVariant contrast ratio $vsSurfaceVariant is below $minUiContrast:1",
                vsSurfaceVariant >= minUiContrast
            )
        }
    }

    @Test
    fun allPalettes_onAccentMeetsTextContrastAgainstAccent() {
        for (palette in AccentPalettes.all) {
            val ratio = calculateContrastRatio(palette.onAccent, palette.accent)
            assertTrue(
                "Palette '${palette.displayName}' onAccent vs accent contrast ratio $ratio is below $minTextContrast:1",
                ratio >= minTextContrast
            )
        }
    }

    @Test
    fun foundationTypography_meetsTextContrast() {
        val primaryRatio = calculateContrastRatio(PosColorTextPrimary, PosColorBackground)
        assertTrue("TextPrimary contrast ratio $primaryRatio must exceed 4.5:1", primaryRatio >= minTextContrast)

        val mutedRatio = calculateContrastRatio(PosColorTextMuted, PosColorBackground)
        assertTrue("TextMuted contrast ratio $mutedRatio must exceed 4.5:1", mutedRatio >= minTextContrast)
    }

    /**
     * Calculates WCAG 2.1 relative luminance and contrast ratio between two colors.
     */
    private fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        val argb = color.toArgb()
        val r = linearize(((argb shr 16) and 0xFF) / 255.0)
        val g = linearize(((argb shr 8) and 0xFF) / 255.0)
        val b = linearize((argb and 0xFF) / 255.0)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun linearize(channel: Double): Double {
        return if (channel <= 0.04045) {
            channel / 12.92
        } else {
            ((channel + 0.055) / 1.055).pow(2.4)
        }
    }
}
