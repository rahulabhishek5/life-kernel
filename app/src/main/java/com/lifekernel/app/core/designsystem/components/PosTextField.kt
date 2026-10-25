package com.lifekernel.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Neo-brutalist text field with persistent label, outline borders, and error state slot.
 */
@Composable
fun PosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    inputModifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val isError = errorMessage != null
    val shape = PosTheme.shapes.radiusMedium

    val borderColor = when {
        isError -> PosTheme.colors.stateError
        isFocused -> PosTheme.colors.accent
        else -> PosTheme.colors.outlineStrong
    }

    val borderWidth = if (isFocused || isError) {
        PosTheme.borders.borderStrong
    } else {
        PosTheme.borders.borderStandard
    }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = PosTheme.typography.label,
                color = if (isError) PosTheme.colors.stateError else PosTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = PosTheme.spacing.space1)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(PosTheme.colors.surfaceVariant)
                .border(borderWidth, borderColor, shape)
                .padding(
                    horizontal = PosTheme.spacing.space4,
                    vertical = PosTheme.spacing.space3
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = PosTheme.typography.body,
                        color = PosTheme.colors.textMuted
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().then(inputModifier),
                    enabled = enabled,
                    singleLine = singleLine,
                    textStyle = PosTheme.typography.body.copy(
                        color = if (enabled) PosTheme.colors.textPrimary else PosTheme.colors.textDisabled
                    ),
                    cursorBrush = SolidColor(PosTheme.colors.accent),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource
                )
            }

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(PosTheme.spacing.space2))
                trailingIcon()
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(PosTheme.spacing.space1))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚠",
                    color = PosTheme.colors.stateError,
                    style = PosTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = PosTheme.spacing.space1)
                )
                Text(
                    text = errorMessage,
                    style = PosTheme.typography.bodySmall,
                    color = PosTheme.colors.stateError
                )
            }
        }
    }
}
