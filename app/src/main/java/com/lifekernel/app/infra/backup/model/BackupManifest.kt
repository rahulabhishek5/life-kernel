package com.lifekernel.app.infra.backup.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root metadata descriptor for backup archives per DATABASE_SCHEMA.md §11.2 and PRD §8.11 (FR-BKP-002).
 *
 * Stored at the root of the ZIP archive as `manifest.json`.
 */
@Serializable
data class BackupManifest(
    @SerialName("format_version")
    val formatVersion: Int = CURRENT_FORMAT_VERSION,

    @SerialName("app_version")
    val appVersion: String = DEFAULT_APP_VERSION,

    @SerialName("room_schema_version")
    val roomSchemaVersion: Int,

    @SerialName("created_at_utc_ms")
    val createdAtUtcMs: Long,

    @SerialName("device_zone_id")
    val deviceZoneId: String,

    @SerialName("files")
    val files: List<BackupFileEntry>
) {
    companion object {
        const val CURRENT_FORMAT_VERSION = 1
        const val DEFAULT_APP_VERSION = "0.1.0"
    }
}

/**
 * Individual data file entry in the backup archive per DATABASE_SCHEMA.md §11.2.
 */
@Serializable
data class BackupFileEntry(
    @SerialName("path")
    val path: String,

    @SerialName("rows")
    val rows: Int,

    @SerialName("sha256")
    val sha256: String
)
