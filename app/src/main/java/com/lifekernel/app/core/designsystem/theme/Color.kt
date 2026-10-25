package com.lifekernel.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Foundation and state colors for Life Kernel.
 *
 * Invariant: Dark AMOLED-black foundation only. Tokens defined in ui-context.md.
 */
val PosColorBackground = Color(0xFF000000)
val PosColorSurface = Color(0xFF101010)
val PosColorSurfaceVariant = Color(0xFF171717)

val PosColorTextPrimary = Color(0xFFF5F5F5)
val PosColorTextMuted = Color(0xFFA3A3A3)
val PosColorTextDisabled = Color(0xFF6B6B6B)

val PosColorDivider = Color(0xFF303030)
val PosColorOutlineSubtle = Color(0xFF3D3D3D)
val PosColorOutlineStrong = Color(0xFF8A8A8A)
val PosColorScrim = Color(0x99000000) // 60% alpha over black

val PosColorStateError = Color(0xFFFF5A5F)
val PosColorStateWarning = Color(0xFFFFB020)
val PosColorStateSuccess = Color(0xFF34D399)
val PosColorStateInfo = Color(0xFF60A5FA)

/**
 * Immutable color system exposed via [LocalPosColors].
 */
@Immutable
data class PosColors(
    val background: Color = PosColorBackground,
    val surface: Color = PosColorSurface,
    val surfaceVariant: Color = PosColorSurfaceVariant,
    val textPrimary: Color = PosColorTextPrimary,
    val textMuted: Color = PosColorTextMuted,
    val textDisabled: Color = PosColorTextDisabled,
    val divider: Color = PosColorDivider,
    val outlineSubtle: Color = PosColorOutlineSubtle,
    val outlineStrong: Color = PosColorOutlineStrong,
    val scrim: Color = PosColorScrim,
    val stateError: Color = PosColorStateError,
    val stateWarning: Color = PosColorStateWarning,
    val stateSuccess: Color = PosColorStateSuccess,
    val stateInfo: Color = PosColorStateInfo,
    val accent: Color,
    val onAccent: Color,
    val accentSubtle: Color,
    val accentPressed: Color,
    val heat1: Color,
    val heat2: Color,
    val heat3: Color,
    val heat4: Color
)
