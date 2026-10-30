package com.lifekernel.app.infra.backup

import com.lifekernel.app.infra.backup.model.BackupFileEntry
import com.lifekernel.app.infra.backup.model.BackupManifest
import com.lifekernel.app.infra.backup.model.BackupSettings
import com.lifekernel.app.infra.backup.registry.BackupJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

/**
 * Unit tests for backup manifest and portable settings serialization per DATABASE_SCHEMA.md §11.2.
 */
class BackupManifestTest {

    @Test
    fun manifest_serializesAndDeserializesCorrectly() {
        val manifest = BackupManifest(
            formatVersion = 1,
            appVersion = "0.1.0",
            roomSchemaVersion = 1,
            createdAtUtcMs = 1790000000000L,
            deviceZoneId = "Europe/Berlin",
            files = listOf(
                BackupFileEntry("data/tags.jsonl", 3, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                BackupFileEntry("data/tasks.jsonl", 42, "384b6f17e2c918a24c53ef8d523171305bbd9a9f2ce5f3405e3b69e46a788cb5")
            )
        )

        val jsonString = BackupJson.encodeToString(BackupManifest.serializer(), manifest)

        // Verify keys match DATABASE_SCHEMA.md §11.2 exactly
        assertTrue(jsonString.contains("\"format_version\":1"))
        assertTrue(jsonString.contains("\"app_version\":\"0.1.0\""))
        assertTrue(jsonString.contains("\"room_schema_version\":1"))
        assertTrue(jsonString.contains("\"created_at_utc_ms\":1790000000000"))
        assertTrue(jsonString.contains("\"device_zone_id\":\"Europe/Berlin\""))
        assertTrue(jsonString.contains("\"files\":["))
        assertTrue(jsonString.contains("\"path\":\"data/tasks.jsonl\""))
        assertTrue(jsonString.contains("\"rows\":42"))

        val decoded = BackupJson.decodeFromString(BackupManifest.serializer(), jsonString)
        assertEquals(manifest, decoded)
    }

    @Test
    fun settings_serializesOnlyPortableState() {
        val settings = BackupSettings(
            themePaletteId = "tokyo_night",
            defaultPlannerView = "TASKS",
            weekStart = "MONDAY",
            timeFormat = "H24"
        )

        val jsonString = BackupJson.encodeToString(BackupSettings.serializer(), settings)

        assertTrue(jsonString.contains("\"theme_palette_id\":\"tokyo_night\""))
        assertTrue(jsonString.contains("\"default_planner_view\":\"TASKS\""))
        assertTrue(jsonString.contains("\"week_start\":\"MONDAY\""))
        assertTrue(jsonString.contains("\"time_format\":\"H24\""))

        // Excludes device-local state
        assertFalse(jsonString.contains("backup_folder"))
        assertFalse(jsonString.contains("dirty_flag"))
        assertFalse(jsonString.contains("snooze"))

        val decoded = BackupJson.decodeFromString(BackupSettings.serializer(), jsonString)
        assertEquals(settings, decoded)
    }

    @Test
    fun sha256_computesLowercaseHexChecksum() {
        val sampleBytes = "LifeKernel Backup SHA256 Test".toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(sampleBytes).joinToString("") { "%02x".format(it) }

        // Expected SHA-256 hash
        assertEquals(64, hash.length)
        assertTrue(hash.all { it in '0'..'9' || it in 'a'..'f' })
        assertFalse(hash.any { it in 'A'..'F' })
    }
}
