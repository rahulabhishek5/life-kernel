package com.lifekernel.app.core.designsystem.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Corner radii per ui-context.md.
 * Invariant: Restrained radii (4/6/8dp). Large pills or circular surfaces are avoided.
 */
@Immutable
data class PosShapes(
    val radiusSmall: CornerBasedShape = RoundedCornerShape(4.dp),
    val radiusMedium: CornerBasedShape = RoundedCornerShape(6.dp),
    val radiusLarge: CornerBasedShape = RoundedCornerShape(8.dp)
)

/**
 * Border and offset depth tokens for warm neo-brutalism.
 */
@Immutable
data class PosBorders(
    val borderHairline: Dp = 1.dp,
    val borderStandard: Dp = 1.5.dp,
    val borderStrong: Dp = 2.dp,
    val offsetShadow: Dp = 3.dp
)
