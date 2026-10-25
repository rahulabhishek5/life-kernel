package com.lifekernel.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Empty state per design-system.md §9:
 * Short mono label, one sentence of guidance, and primary call-to-action button.
 */
@Composable
fun PosEmptyState(
    label: String,
    guidance: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .fillMaxWidth()
            .padding(PosTheme.spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label.uppercase(),
            style = PosTheme.typography.dataLarge,
            color = PosTheme.colors.textMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(PosTheme.spacing.space2))

        Text(
            text = guidance,
            style = PosTheme.typography.bodySmall,
            color = PosTheme.colors.textMuted,
            textAlign = TextAlign.Center
        )

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(PosTheme.spacing.space4))
            PosButton(
                text = actionText,
                onClick = onActionClick,
                variant = PosButtonVariant.Primary
            )
        }
    }
}

/**
 * Standard error state layout with stateError indicator, error message, and optional retry action.
 */
@Composable
fun PosErrorState(
    message: String,
    onRetryClick: (() -> Unit)? = null,
    retryText: String = "Retry",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PosTheme.spacing.space6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠",
            style = PosTheme.typography.headline,
            color = PosTheme.colors.stateError
        )

        Spacer(modifier = Modifier.height(PosTheme.spacing.space2))

        Text(
            text = message,
            style = PosTheme.typography.body,
            color = PosTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        if (onRetryClick != null) {
            Spacer(modifier = Modifier.height(PosTheme.spacing.space4))
            PosButton(
                text = retryText,
                onClick = onRetryClick,
                variant = PosButtonVariant.Secondary
            )
        }
    }
}
