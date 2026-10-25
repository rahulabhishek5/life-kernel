package com.lifekernel.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Inline banner component for alerts and backup nudges.
 */
@Composable
fun PosBanner(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: String = "⚠",
    borderColor: Color = PosTheme.colors.stateWarning,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismissClick: (() -> Unit)? = null
) {
    val shape = PosTheme.shapes.radiusMedium

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PosTheme.colors.surfaceVariant)
            .border(PosTheme.borders.borderStandard, borderColor, shape)
            .padding(PosTheme.spacing.space3)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                color = borderColor,
                style = PosTheme.typography.title,
                modifier = Modifier.padding(end = PosTheme.spacing.space3)
            )

            Column(modifier = Modifier.weight(1f)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = PosTheme.typography.title,
                        color = PosTheme.colors.textPrimary
                    )
                }
                Text(
                    text = message,
                    style = PosTheme.typography.bodySmall,
                    color = PosTheme.colors.textMuted
                )
            }

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.width(PosTheme.spacing.space2))
                PosButton(
                    text = actionLabel,
                    onClick = onActionClick,
                    variant = PosButtonVariant.Primary
                )
            }

            if (onDismissClick != null) {
                Spacer(modifier = Modifier.width(PosTheme.spacing.space2))
                val dismissInteractionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .defaultMinSize(
                            minWidth = PosTheme.spacing.minTouchTarget,
                            minHeight = PosTheme.spacing.minTouchTarget
                        )
                        .clickable(
                            interactionSource = dismissInteractionSource,
                            indication = null,
                            onClick = onDismissClick
                        )
                        .semantics {
                            role = Role.Button
                            contentDescription = "Dismiss banner"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        style = PosTheme.typography.title,
                        color = PosTheme.colors.textMuted
                    )
                }
            }
        }
    }
}
