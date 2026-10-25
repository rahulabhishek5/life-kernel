package com.lifekernel.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * System font families.
 * Sans for readable UI text; Monospace selectively for dates, statistics, and technical labels.
 */
object PosFonts {
    val sans: FontFamily = FontFamily.Default
    val mono: FontFamily = FontFamily.Monospace
}

/**
 * Typography scale for Life Kernel per ui-context.md.
 */
@Immutable
data class PosTypography(
    val headline: TextStyle = TextStyle(
        fontFamily = PosFonts.sans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    val title: TextStyle = TextStyle(
        fontFamily = PosFonts.sans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    val body: TextStyle = TextStyle(
        fontFamily = PosFonts.sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    val bodySmall: TextStyle = TextStyle(
        fontFamily = PosFonts.sans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    val label: TextStyle = TextStyle(
        fontFamily = PosFonts.sans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    val data: TextStyle = TextStyle(
        fontFamily = PosFonts.mono,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    val dataLarge: TextStyle = TextStyle(
        fontFamily = PosFonts.mono,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )
)
