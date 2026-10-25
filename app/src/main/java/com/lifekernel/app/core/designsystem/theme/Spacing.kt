package com.lifekernel.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 4dp base grid spacing tokens per ui-context.md.
 */
@Immutable
data class PosSpacing(
    val space1: Dp = 4.dp,
    val space2: Dp = 8.dp,
    val space3: Dp = 12.dp,
    val space4: Dp = 16.dp,
    val space6: Dp = 24.dp,
    val space8: Dp = 32.dp,
    val space12: Dp = 48.dp,
    val minTouchTarget: Dp = 48.dp
)
