package com.lifekernel.app.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifekernel.app.core.designsystem.components.PosBanner
import com.lifekernel.app.core.designsystem.components.PosButton
import com.lifekernel.app.core.designsystem.components.PosButtonVariant
import com.lifekernel.app.core.designsystem.components.PosCard
import com.lifekernel.app.core.designsystem.components.PosCheckbox
import com.lifekernel.app.core.designsystem.components.PosChip
import com.lifekernel.app.core.designsystem.components.PosEmptyState
import com.lifekernel.app.core.designsystem.components.PosErrorState
import com.lifekernel.app.core.designsystem.components.PosProgressBar
import com.lifekernel.app.core.designsystem.components.PosSegmentedToggle
import com.lifekernel.app.core.designsystem.components.PosTextField
import com.lifekernel.app.core.designsystem.theme.AccentPalette
import com.lifekernel.app.core.designsystem.theme.AccentPalettes
import com.lifekernel.app.core.designsystem.theme.PosTheme

/**
 * Component catalog demonstrating all core neo-brutalist components across all 7 accent palettes.
 */
@Composable
fun ComponentCatalogScreen(
    modifier: Modifier = Modifier
) {
    var selectedPalette by remember { mutableStateOf(AccentPalettes.Default) }
    var forceReducedMotion by remember { mutableStateOf(false) }

    PosTheme(
        palette = selectedPalette,
        forceReducedMotion = forceReducedMotion
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(PosTheme.colors.background)
                .verticalScroll(scrollState)
                .padding(PosTheme.spacing.space4)
        ) {
            Text(
                text = "Life Kernel Component Catalog",
                style = PosTheme.typography.headline,
                color = PosTheme.colors.textPrimary
            )
            Text(
                text = "Warm Neo-Brutalist Design System · Milestone M1",
                style = PosTheme.typography.bodySmall,
                color = PosTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(PosTheme.spacing.space4))

            // Palette Switcher
            CatalogSectionHeader(title = "1. Accent Palette Selector")
            val paletteScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(paletteScrollState),
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
            ) {
                AccentPalettes.all.forEach { palette ->
                    PosChip(
                        text = palette.displayName,
                        selected = palette.id == selectedPalette.id,
                        onClick = { selectedPalette = palette }
                    )
                }
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space2))

            // Motion Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
            ) {
                PosChip(
                    text = if (forceReducedMotion) "Reduced Motion: ON" else "Reduced Motion: OFF",
                    selected = forceReducedMotion,
                    onClick = { forceReducedMotion = !forceReducedMotion }
                )
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // Buttons
            CatalogSectionHeader(title = "2. PosButton")
            Row(
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PosButton(
                    text = "Primary",
                    onClick = {},
                    variant = PosButtonVariant.Primary
                )
                PosButton(
                    text = "Secondary",
                    onClick = {},
                    variant = PosButtonVariant.Secondary
                )
            }
            Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
            Row(
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PosButton(
                    text = "Destructive",
                    onClick = {},
                    variant = PosButtonVariant.Destructive
                )
                PosButton(
                    text = "Text Button",
                    onClick = {},
                    variant = PosButtonVariant.Text
                )
            }
            Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
            PosButton(
                text = "Disabled Button",
                onClick = {},
                enabled = false,
                variant = PosButtonVariant.Primary
            )

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // Cards
            CatalogSectionHeader(title = "3. PosCard")
            PosCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Standard Card",
                        style = PosTheme.typography.title,
                        color = PosTheme.colors.textPrimary
                    )
                    Text(
                        text = "AMOLED surface #101010 with subtle outline #3D3D3D.",
                        style = PosTheme.typography.bodySmall,
                        color = PosTheme.colors.textMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
            var cardSelected by remember { mutableStateOf(true) }
            PosCard(
                modifier = Modifier.fillMaxWidth(),
                isSelected = cardSelected,
                onClick = { cardSelected = !cardSelected }
            ) {
                Column {
                    Text(
                        text = if (cardSelected) "Selected Card (Click to Toggle)" else "Unselected Card",
                        style = PosTheme.typography.title,
                        color = if (cardSelected) PosTheme.colors.accent else PosTheme.colors.textPrimary
                    )
                    Text(
                        text = "Features 2dp accent border and 3dp tactile offset shadow.",
                        style = PosTheme.typography.bodySmall,
                        color = PosTheme.colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // Text Fields
            CatalogSectionHeader(title = "4. PosTextField")
            var textValue1 by remember { mutableStateOf("") }
            PosTextField(
                value = textValue1,
                onValueChange = { textValue1 = it },
                label = "Task Title",
                placeholder = "e.g. Submit lab record",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
            var textValue2 by remember { mutableStateOf("Invalid input text") }
            PosTextField(
                value = textValue2,
                onValueChange = { textValue2 = it },
                label = "Duration",
                errorMessage = "Duration must be between 1 and 1440 minutes",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // Checkboxes
            CatalogSectionHeader(title = "5. PosCheckbox")
            var checked1 by remember { mutableStateOf(false) }
            var checked2 by remember { mutableStateOf(true) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space4)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PosCheckbox(checked = checked1, onCheckedChange = { checked1 = it })
                    Spacer(modifier = Modifier.width(PosTheme.spacing.space2))
                    Text(text = "Unchecked", style = PosTheme.typography.body, color = PosTheme.colors.textPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PosCheckbox(checked = checked2, onCheckedChange = { checked2 = it })
                    Spacer(modifier = Modifier.width(PosTheme.spacing.space2))
                    Text(text = "Checked", style = PosTheme.typography.body, color = PosTheme.colors.textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // Chips
            CatalogSectionHeader(title = "6. PosChip")
            val chipOptions = listOf("All", "High Priority", "Today", "Completed")
            var selectedChip by remember { mutableStateOf("High Priority") }
            Row(
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
            ) {
                chipOptions.forEach { option ->
                    PosChip(
                        text = option,
                        selected = option == selectedChip,
                        onClick = { selectedChip = option }
                    )
                }
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // Segmented Toggle
            CatalogSectionHeader(title = "7. PosSegmentedToggle")
            var selectedToggleIndex by remember { mutableIntStateOf(0) }
            PosSegmentedToggle(
                options = listOf("Tasks", "Timetable"),
                selectedIndex = selectedToggleIndex,
                onOptionSelected = { selectedToggleIndex = it }
            )

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // Banner
            CatalogSectionHeader(title = "8. PosBanner")
            var showBanner by remember { mutableStateOf(true) }
            if (showBanner) {
                PosBanner(
                    title = "Last backup 7 days ago",
                    message = "Data changed since last backup.",
                    actionLabel = "BACK UP",
                    onActionClick = {},
                    onDismissClick = { showBanner = false }
                )
            } else {
                PosButton(
                    text = "Restore Banner",
                    onClick = { showBanner = true },
                    variant = PosButtonVariant.Secondary
                )
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // Progress Bar
            CatalogSectionHeader(title = "9. PosProgressBar")
            var progress by remember { mutableFloatStateOf(0.45f) }
            Text(
                text = "${(progress * 100).toInt()}% COMPLETED",
                style = PosTheme.typography.data,
                color = PosTheme.colors.accent
            )
            Spacer(modifier = Modifier.height(PosTheme.spacing.space1))
            PosProgressBar(progress = progress)
            Spacer(modifier = Modifier.height(PosTheme.spacing.space2))
            Row(horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)) {
                PosButton(text = "25%", onClick = { progress = 0.25f }, variant = PosButtonVariant.Text)
                PosButton(text = "50%", onClick = { progress = 0.50f }, variant = PosButtonVariant.Text)
                PosButton(text = "75%", onClick = { progress = 0.75f }, variant = PosButtonVariant.Text)
                PosButton(text = "100%", onClick = { progress = 1.0f }, variant = PosButtonVariant.Text)
            }

            Spacer(modifier = Modifier.height(PosTheme.spacing.space6))

            // States
            CatalogSectionHeader(title = "10. PosEmptyState & PosErrorState")
            PosEmptyState(
                label = "NOTHING PLANNED",
                guidance = "Add your first task for today.",
                actionText = "Add task",
                onActionClick = {}
            )
            Spacer(modifier = Modifier.height(PosTheme.spacing.space4))
            PosErrorState(
                message = "Failed to load database. Check file permissions.",
                onRetryClick = {}
            )

            Spacer(modifier = Modifier.height(PosTheme.spacing.space12))
        }
    }
}

@Composable
private fun CatalogSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = PosTheme.typography.data,
        color = PosTheme.colors.accent,
        modifier = Modifier.padding(bottom = PosTheme.spacing.space2)
    )
}
