package com.lifekernel.app.domain.model

/**
 * Immutable search and filter criteria model per PRD §8.2 (FR-PLAN-004..006).
 *
 * Invariant:
 * - Pure Kotlin domain model without framework dependencies.
 * - [isActive] evaluates to true if any filter or query constraint is applied.
 */
data class TaskFilterCriteria(
    val searchQuery: String = "",
    val selectedPriority: TaskPriority? = null,
    val selectedTagName: String? = null
) {
    val trimmedQuery: String
        get() = searchQuery.trim()

    val isActive: Boolean
        get() = trimmedQuery.isNotEmpty() || selectedPriority != null || !selectedTagName.isNullOrBlank()
}
