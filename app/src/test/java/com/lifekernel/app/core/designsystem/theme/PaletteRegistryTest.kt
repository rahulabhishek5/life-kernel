package com.lifekernel.app.core.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PaletteRegistryTest {

    @Test
    fun registry_containsAllSevenPalettes() {
        assertEquals(7, AccentPalettes.all.size)
        val expectedIds = setOf("lime", "orange", "red", "blue", "purple", "amber", "mono")
        val actualIds = AccentPalettes.all.map { it.id }.toSet()
        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun defaultPalette_isLime() {
        assertEquals(AccentPalettes.Lime, AccentPalettes.Default)
        assertEquals("lime", AccentPalettes.Default.id)
    }

    @Test
    fun fromId_returnsCorrectPaletteForKnownIds() {
        assertEquals(AccentPalettes.Lime, AccentPalettes.fromId("lime"))
        assertEquals(AccentPalettes.Orange, AccentPalettes.fromId("orange"))
        assertEquals(AccentPalettes.Red, AccentPalettes.fromId("red"))
        assertEquals(AccentPalettes.Blue, AccentPalettes.fromId("blue"))
        assertEquals(AccentPalettes.Purple, AccentPalettes.fromId("purple"))
        assertEquals(AccentPalettes.Amber, AccentPalettes.fromId("amber"))
        assertEquals(AccentPalettes.Mono, AccentPalettes.fromId("mono"))
    }

    @Test
    fun fromId_fallsBackToDefaultOnUnknownOrNullId() {
        assertEquals(AccentPalettes.Default, AccentPalettes.fromId("non_existent_palette"))
        assertEquals(AccentPalettes.Default, AccentPalettes.fromId(""))
        assertEquals(AccentPalettes.Default, AccentPalettes.fromId(null))
    }

    @Test
    fun limePalette_resolvesToLimeGreen_neverRed() {
        val lime = AccentPalettes.fromId("lime")
        assertEquals(AccentPalettes.Lime, lime)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFA3E635), lime.accent)
        org.junit.Assert.assertNotEquals(androidx.compose.ui.graphics.Color(0xFFF87171), lime.accent)
        org.junit.Assert.assertNotEquals(androidx.compose.ui.graphics.Color(0xFFFF5A5F), lime.accent)
    }

    @Test
    fun createPosColors_derivesTokensCorrectly() {
        for (palette in AccentPalettes.all) {
            val colors = AccentPalettes.createPosColors(palette)
            assertEquals(palette.accent, colors.accent)
            assertEquals(palette.onAccent, colors.onAccent)
            assertEquals(0.16f, colors.accentSubtle.alpha, 0.01f)
            assertNotNull(colors.accentPressed)
            assertNotNull(colors.heat1)
            assertNotNull(colors.heat2)
            assertNotNull(colors.heat3)
            assertNotNull(colors.heat4)
        }
    }
}
