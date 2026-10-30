package com.lifekernel.app.infra.backup.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Portable user preferences exported from DataStore per DATABASE_SCHEMA.md §11.1.
 * Stored at the root of the ZIP archive as `settings.json`.
 *
 * Invariant: Strictly excludes device-local state (backup folder URI, dirty flag, snooze state).
 */
@Serializable
data class BackupSettings(
    @SerialName("theme_palette_id")
    val themePaletteId: String = "lime",

    @SerialName("default_planner_view")
    val defaultPlannerView: String = "TASKS",

    @SerialName("week_start")
    val weekStart: String? = null,

    @SerialName("time_format")
    val timeFormat: String? = null
)
