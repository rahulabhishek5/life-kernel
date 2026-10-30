package com.lifekernel.app.feature.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import com.lifekernel.app.core.designsystem.components.PosChip
import com.lifekernel.app.core.designsystem.components.PosRemovableChip
import com.lifekernel.app.core.designsystem.components.PosTextField
import com.lifekernel.app.core.designsystem.theme.PosFonts
import com.lifekernel.app.core.designsystem.theme.PosTheme
import com.lifekernel.app.domain.model.Tag

/**
 * Neo-brutalist inline tag picker component per PRD §8.4, AC-TAG, and design-system.md §6.
 *
 * Features:
 * - FlowRow of assigned tags with dedicated >=48dp touch-target remove action.
 * - Input field to type tag name with live validation.
 * - Suggestion chips for existing matching tags.
 * - Create action chip for novel tags.
 * - Automatic lowercase normalization and duplicate avoidance.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PosTagPicker(
    assignedTags: List<String>,
    availableTags: List<Tag>,
    query: String,
    onQueryChange: (String) -> Unit,
    errorMessage: String?,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalizedQuery = query.trim().removePrefix("#").lowercase()
    val assignedSet = assignedTags.map { it.lowercase() }.toSet()

    // Filter available tags that match query and are not yet assigned
    val matchingSuggestions = if (normalizedQuery.isNotEmpty()) {
        availableTags.filter { tag ->
            val tagName = tag.name.lowercase()
            tagName.contains(normalizedQuery) && tagName !in assignedSet
        }
    } else {
        emptyList()
    }

    val canCreateNovelTag = normalizedQuery.isNotEmpty() &&
        normalizedQuery !in assignedSet &&
        availableTags.none { it.name.equals(normalizedQuery, ignoreCase = true) } &&
        errorMessage == null

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 1. Assigned tag chips
        if (assignedTags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("assigned_tags_flow"),
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2),
                verticalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
            ) {
                assignedTags.forEach { tagName ->
                    PosRemovableChip(
                        tagText = tagName,
                        onRemove = { onRemoveTag(tagName) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(PosTheme.spacing.space3))
        } else {
            Text(
                text = "No tags assigned",
                style = PosTheme.typography.bodySmall.copy(fontFamily = PosFonts.mono),
                color = PosTheme.colors.textMuted,
                modifier = Modifier.testTag("no_tags_text")
            )
            Spacer(modifier = Modifier.height(PosTheme.spacing.space2))
        }

        // 2. Tag input field
        PosTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "TAGS",
            placeholder = "Add tag (e.g. urgent, work)...",
            errorMessage = errorMessage,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (normalizedQuery.isNotEmpty() && errorMessage == null) {
                        onAddTag(normalizedQuery)
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tag_picker_input")
        )

        // 3. Suggestions & Create chip row
        if (canCreateNovelTag || matchingSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(PosTheme.spacing.space2))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tag_suggestions_flow"),
                horizontalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2),
                verticalArrangement = Arrangement.spacedBy(PosTheme.spacing.space2)
            ) {
                if (canCreateNovelTag) {
                    PosChip(
                        text = "+ Create #$normalizedQuery",
                        selected = true,
                        onClick = { onAddTag(normalizedQuery) },
                        modifier = Modifier.testTag("create_tag_chip")
                    )
                }

                matchingSuggestions.take(5).forEach { tag ->
                    PosChip(
                        text = "#${tag.name}",
                        selected = false,
                        onClick = { onAddTag(tag.name) },
                        modifier = Modifier.testTag("suggest_tag_${tag.name}")
                    )
                }
            }
        }
    }
}
