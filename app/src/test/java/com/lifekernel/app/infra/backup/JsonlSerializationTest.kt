package com.lifekernel.app.infra.backup

import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity
import com.lifekernel.app.infra.backup.registry.BackupJson
import com.lifekernel.app.infra.backup.serializer.TagBackupDto
import com.lifekernel.app.infra.backup.serializer.TaskBackupDto
import com.lifekernel.app.infra.backup.serializer.TaskCompletionBackupDto
import com.lifekernel.app.infra.backup.serializer.TaskTagBackupDto
import com.lifekernel.app.infra.backup.serializer.toBackupDto
import com.lifekernel.app.infra.backup.serializer.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for JSON Lines serialization across all database tables per DATABASE_SCHEMA.md §6 & §11.1.
 */
class JsonlSerializationTest {

    @Test
    fun taskEntity_serializesWithExactDatabaseColumnsAndExplicitNulls() {
        val task = TaskEntity(
            id = 101L,
            title = "Implement backup writer",
            description = "ZIP, manifest, JSONL per table",
            status = 0,
            priority = 3,
            startDate = "2026-10-30",
            dueDate = "2026-10-31",
            parentId = null,
            sortOrder = 0,
            recurType = 0,
            recurInterval = 1,
            recurWeekdays = 0,
            recurUntil = null,
            createdAt = 1790000000000L,
            updatedAt = 1790000005000L
        )

        val dto = task.toBackupDto()
        val json = BackupJson.encodeToString(TaskBackupDto.serializer(), dto)

        // Verify keys match SQLite column names exactly
        assertTrue(json.contains("\"id\":101"))
        assertTrue(json.contains("\"title\":\"Implement backup writer\""))
        assertTrue(json.contains("\"description\":\"ZIP, manifest, JSONL per table\""))
        assertTrue(json.contains("\"status\":0"))
        assertTrue(json.contains("\"priority\":3"))
        assertTrue(json.contains("\"start_date\":\"2026-10-30\""))
        assertTrue(json.contains("\"due_date\":\"2026-10-31\""))
        assertTrue(json.contains("\"parent_id\":null"))
        assertTrue(json.contains("\"sort_order\":0"))
        assertTrue(json.contains("\"recur_type\":0"))
        assertTrue(json.contains("\"recur_interval\":1"))
        assertTrue(json.contains("\"recur_weekdays\":0"))
        assertTrue(json.contains("\"recur_until\":null"))
        assertTrue(json.contains("\"created_at\":1790000000000"))
        assertTrue(json.contains("\"updated_at\":1790000005000"))

        val decoded = BackupJson.decodeFromString(TaskBackupDto.serializer(), json)
        assertEquals(task, decoded.toEntity())
    }

    @Test
    fun tagEntity_serializesWithExactDatabaseColumns() {
        val tag = TagEntity(
            id = 5L,
            name = "deep-work",
            createdAt = 1790000000000L
        )

        val dto = tag.toBackupDto()
        val json = BackupJson.encodeToString(TagBackupDto.serializer(), dto)

        assertTrue(json.contains("\"id\":5"))
        assertTrue(json.contains("\"name\":\"deep-work\""))
        assertTrue(json.contains("\"created_at\":1790000000000"))

        val decoded = BackupJson.decodeFromString(TagBackupDto.serializer(), json)
        assertEquals(tag, decoded.toEntity())
    }

    @Test
    fun taskTagEntity_serializesWithExactDatabaseColumns() {
        val taskTag = TaskTagEntity(
            taskId = 101L,
            tagId = 5L
        )

        val dto = taskTag.toBackupDto()
        val json = BackupJson.encodeToString(TaskTagBackupDto.serializer(), dto)

        assertTrue(json.contains("\"task_id\":101"))
        assertTrue(json.contains("\"tag_id\":5"))

        val decoded = BackupJson.decodeFromString(TaskTagBackupDto.serializer(), json)
        assertEquals(taskTag, decoded.toEntity())
    }

    @Test
    fun taskCompletionEntity_serializesWithExactDatabaseColumns() {
        val completion = TaskCompletionEntity(
            id = 42L,
            taskId = 101L,
            occurrenceDate = "2026-10-30",
            completedAt = 1790000010000L,
            completedOn = "2026-10-30"
        )

        val dto = completion.toBackupDto()
        val json = BackupJson.encodeToString(TaskCompletionBackupDto.serializer(), dto)

        assertTrue(json.contains("\"id\":42"))
        assertTrue(json.contains("\"task_id\":101"))
        assertTrue(json.contains("\"occurrence_date\":\"2026-10-30\""))
        assertTrue(json.contains("\"completed_at\":1790000010000"))
        assertTrue(json.contains("\"completed_on\":\"2026-10-30\""))

        val decoded = BackupJson.decodeFromString(TaskCompletionBackupDto.serializer(), json)
        assertEquals(completion, decoded.toEntity())
    }
}
