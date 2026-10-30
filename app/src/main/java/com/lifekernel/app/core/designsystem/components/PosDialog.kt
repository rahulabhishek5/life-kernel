package com.lifekernel.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Neo-brutalist modal dialog per design-system.md §6.
 *
 * Adheres strictly to design tokens:
 * - Surface #101010 (PosTheme.colors.surface)
 * - 2dp outlineStrong border (PosTheme.borders.borderStrong, PosTheme.colors.outlineStrong)
 * - 8dp corner radius (PosTheme.shapes.radiusLarge)
 * - 60% alpha AMOLED black scrim (PosTheme.colors.scrim)
 * - Action buttons with strictly >= 48dp touch targets
 */
@Composable
fun PosDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    body: String? = null,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: (@Composable () -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PosTheme.colors.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                )
                .testTag("pos_dialog_scrim"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = PosTheme.spacing.space4)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Intercept clicks inside the dialog
                    )
                    .background(PosTheme.colors.surface, PosTheme.shapes.radiusLarge)
                    .border(
                        width = PosTheme.borders.borderStrong,
                        color = PosTheme.colors.outlineStrong,
                        shape = PosTheme.shapes.radiusLarge
                    )
                    .padding(PosTheme.spacing.space6)
                    .testTag("pos_dialog")
                    .semantics { paneTitle = title }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        style = PosTheme.typography.title,
                        color = PosTheme.colors.textPrimary,
                        modifier = Modifier.testTag("pos_dialog_title")
                    )

                    if (body != null) {
                        Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
                        Text(
                            text = body,
                            style = PosTheme.typography.body,
                            color = PosTheme.colors.textMuted,
                            modifier = Modifier.testTag("pos_dialog_body")
                        )
                    }

                    if (content != null) {
                        Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
                        content()
                    }

                    Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space3)
                    ) {
                        if (dismissButton != null) {
                            Box(modifier = Modifier.weight(1f)) {
                                dismissButton()
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Specialized confirmation dialog conforming to design-system.md §6.
 * Dismiss/Cancel on the left (Secondary outline style).
 * Affirmative/Confirm on the right (Primary or Destructive style).
 */
@Composable
fun PosConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "CONFIRM",
    dismissText: String = "CANCEL",
    isDestructive: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    PosDialog(
        title = title,
        body = message,
        onDismissRequest = onDismiss,
        modifier = modifier,
        dismissButton = {
            PosButton(
                text = dismissText,
                onClick = onDismiss,
                variant = PosButtonVariant.Secondary,
                fillMaxWidth = true,
                modifier = Modifier.testTag("pos_dialog_cancel_button")
            )
        },
        confirmButton = {
            PosButton(
                text = confirmText,
                onClick = onConfirm,
                variant = if (isDestructive) PosButtonVariant.Destructive else PosButtonVariant.Primary,
                fillMaxWidth = true,
                modifier = Modifier.testTag("pos_dialog_confirm_button")
            )
        },
        content = content
    )
}
