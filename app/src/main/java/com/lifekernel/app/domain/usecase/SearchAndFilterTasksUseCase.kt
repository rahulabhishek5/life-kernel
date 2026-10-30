package com.lifekernel.app.domain.usecase

import com.lifekernel.app.domain.model.Task
import com.lifekernel.app.domain.model.TaskFilterCriteria
import com.lifekernel.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to search and filter tasks across all dates per PRD §8.2 (FR-PLAN-004..006).
 *
 * Invariants:
 * - When [criteria.isActive] is true, queries across all dates in the database (past, today, future, unscheduled).
 * - Matches case-insensitive substring on title and description.
 * - Conjunctive (AND) logic across search query, priority, and tag filters.
 * - Excludes subtasks (top-level tasks only).
 * - Pure Kotlin domain logic without framework dependencies (LAYER-INVARIANT #1).
 */
class SearchAndFilterTasksUseCase(
    private val taskRepository: TaskRepository
) {

    operator fun invoke(criteria: TaskFilterCriteria): Flow<List<Task>> {
        return taskRepository.observeAllTasks().map { tasks ->
            filterTasks(tasks, criteria)
        }
    }

    fun filterTasks(tasks: List<Task>, criteria: TaskFilterCriteria): List<Task> {
        if (!criteria.isActive) {
            return tasks.filter { !it.isSubtask }
        }

        val trimmed = criteria.trimmedQuery

        return tasks.filter { task ->
            if (task.isSubtask) return@filter false

            val matchesQuery = if (trimmed.isEmpty()) {
                true
            } else {
                task.title.contains(trimmed, ignoreCase = true) ||
                    (task.description?.contains(trimmed, ignoreCase = true) == true)
            }

            val matchesPriority = if (criteria.selectedPriority == null) {
                true
            } else {
                task.priority == criteria.selectedPriority
            }

            val matchesTag = if (criteria.selectedTagName.isNullOrBlank()) {
                true
            } else {
                val tagTarget = criteria.selectedTagName.trim().removePrefix("#")
                task.tags.any { it.name.equals(tagTarget, ignoreCase = true) }
            }

            matchesQuery && matchesPriority && matchesTag
        }
    }
}
