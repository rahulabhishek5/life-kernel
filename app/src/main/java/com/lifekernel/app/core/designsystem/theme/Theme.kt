package com.lifekernel.app.core.designsystem.theme

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext

val LocalPosColors = staticCompositionLocalOf<PosColors> {
    error("No PosColors provided. Ensure PosTheme wraps your UI.")
}

val LocalPosTypography = staticCompositionLocalOf { PosTypography() }
val LocalPosSpacing = staticCompositionLocalOf { PosSpacing() }
val LocalPosShapes = staticCompositionLocalOf { PosShapes() }
val LocalPosBorders = staticCompositionLocalOf { PosBorders() }
val LocalPosMotion = staticCompositionLocalOf { PosMotion() }
val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * Accessor object for Life Kernel design tokens.
 */
object PosTheme {
    val colors: PosColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPosColors.current

    val typography: PosTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalPosTypography.current

    val spacing: PosSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalPosSpacing.current

    val shapes: PosShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalPosShapes.current

    val borders: PosBorders
        @Composable
        @ReadOnlyComposable
        get() = LocalPosBorders.current

    val motion: PosMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalPosMotion.current

    val reducedMotion: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalReducedMotion.current
}

/**
 * Primary theme composable for Life Kernel.
 *
 * Invariant: Dark AMOLED-black foundation only. No light mode in v1.
 */
@Composable
fun PosTheme(
    palette: AccentPalette = AccentPalettes.Default,
    forceReducedMotion: Boolean? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val reducedMotion = forceReducedMotion ?: remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Throwable) {
            false
        }
    }

    val motionTokens = PosMotion()
    val animationDuration = if (reducedMotion) 0 else motionTokens.motionStandard

    // Animate only accent-derived colors when theme switches (per design-system.md §4)
    val animatedAccent = animateColorAsState(
        targetValue = palette.accent,
        animationSpec = tween(durationMillis = animationDuration),
        label = "PosThemeAccent"
    ).value

    val baseColors = remember(palette) {
        AccentPalettes.createPosColors(palette)
    }

    val animatedAccentSubtle = animatedAccent.copy(alpha = 0.16f)
    val animatedAccentPressed = animatedAccent.copy(alpha = 0.80f).compositeOver(PosColorSurface)

    val dynamicColors = baseColors.copy(
        accent = animatedAccent,
        onAccent = palette.onAccent,
        accentSubtle = animatedAccentSubtle,
        accentPressed = animatedAccentPressed
    )

    val materialColorScheme = remember(dynamicColors) {
        darkColorScheme(
            primary = dynamicColors.accent,
            onPrimary = dynamicColors.onAccent,
            background = dynamicColors.background,
            onBackground = dynamicColors.textPrimary,
            surface = dynamicColors.surface,
            onSurface = dynamicColors.textPrimary,
            surfaceVariant = dynamicColors.surfaceVariant,
            onSurfaceVariant = dynamicColors.textMuted,
            outline = dynamicColors.outlineStrong,
            outlineVariant = dynamicColors.outlineSubtle,
            error = dynamicColors.stateError,
            onError = PosColorBackground
        )
    }

    CompositionLocalProvider(
        LocalPosColors provides dynamicColors,
        LocalPosTypography provides PosTypography(),
        LocalPosSpacing provides PosSpacing(),
        LocalPosShapes provides PosShapes(),
        LocalPosBorders provides PosBorders(),
        LocalPosMotion provides motionTokens,
        LocalReducedMotion provides reducedMotion
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
