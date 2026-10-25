package com.lifekernel.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/**
 * Specification for an accent palette in Life Kernel.
 */
@Immutable
data class AccentPalette(
    val id: String,
    val displayName: String,
    val accent: Color,
    val onAccent: Color
)

/**
 * Registry of available accent palettes.
 *
 * Invariant: Palettes are applied through semantic tokens. Features never hardcode palette names.
 */
object AccentPalettes {
    val Lime = AccentPalette(
        id = "lime",
        displayName = "Lime / Green",
        accent = Color(0xFFA3E635),
        onAccent = Color(0xFF000000)
    )

    val Orange = AccentPalette(
        id = "orange",
        displayName = "Orange",
        accent = Color(0xFFFB923C),
        onAccent = Color(0xFF000000)
    )

    val Red = AccentPalette(
        id = "red",
        displayName = "Red",
        accent = Color(0xFFF87171),
        onAccent = Color(0xFF000000)
    )

    val Blue = AccentPalette(
        id = "blue",
        displayName = "Blue",
        accent = Color(0xFF60A5FA),
        onAccent = Color(0xFF000000)
    )

    val Purple = AccentPalette(
        id = "purple",
        displayName = "Purple",
        accent = Color(0xFFA78BFA),
        onAccent = Color(0xFF000000)
    )

    val Amber = AccentPalette(
        id = "amber",
        displayName = "Warm Amber",
        accent = Color(0xFFFBBF24),
        onAccent = Color(0xFF000000)
    )

    val Mono = AccentPalette(
        id = "mono",
        displayName = "Neutral Mono",
        accent = Color(0xFFE5E5E5),
        onAccent = Color(0xFF000000)
    )

    val Default: AccentPalette = Lime

    val all: List<AccentPalette> = listOf(
        Lime,
        Orange,
        Red,
        Blue,
        Purple,
        Amber,
        Mono
    )

    private val byId: Map<String, AccentPalette> = all.associateBy { it.id }

    /**
     * Resolves a palette by its identifier, falling back to [Default] (Lime) if unknown or null.
     */
    fun fromId(id: String?): AccentPalette {
        if (id == null) return Default
        return byId[id] ?: Default
    }

    /**
     * Constructs full [PosColors] dynamically derived from the given [palette].
     */
    fun createPosColors(palette: AccentPalette = Default): PosColors {
        val accent = palette.accent
        val onAccent = palette.onAccent

        val accentSubtle = accent.copy(alpha = 0.16f)
        val accentPressed = accent.copy(alpha = 0.80f).compositeOver(PosColorSurface)

        val heat1 = accent.copy(alpha = 0.25f).compositeOver(PosColorBackground)
        val heat2 = accent.copy(alpha = 0.45f).compositeOver(PosColorBackground)
        val heat3 = accent.copy(alpha = 0.70f).compositeOver(PosColorBackground)
        val heat4 = accent.copy(alpha = 1.00f).compositeOver(PosColorBackground)

        return PosColors(
            accent = accent,
            onAccent = onAccent,
            accentSubtle = accentSubtle,
            accentPressed = accentPressed,
            heat1 = heat1,
            heat2 = heat2,
            heat3 = heat3,
            heat4 = heat4
        )
    }
}
