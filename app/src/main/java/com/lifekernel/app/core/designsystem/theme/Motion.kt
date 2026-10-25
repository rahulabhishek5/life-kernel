package com.lifekernel.app.core.designsystem.theme

import androidx.compose.runtime.Immutable

/**
 * Motion duration tokens in milliseconds per ui-context.md.
 * Invariant: Short, subtle, interruptible animations (100 to 220 ms).
 */
@Immutable
data class PosMotion(
    val motionFast: Int = 100,
    val motionStandard: Int = 160,
    val motionEmphasized: Int = 220
)
