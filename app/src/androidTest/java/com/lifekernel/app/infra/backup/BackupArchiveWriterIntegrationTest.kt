package com.lifekernel.app.infra.backup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.core.time.FixedClock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity
import com.lifekernel.app.data.prefs.SettingsPreferences
import com.lifekernel.app.data.prefs.SettingsRepositoryImpl
import com.lifekernel.app.data.repository.BackupRepositoryImpl
import com.lifekernel.app.domain.model.DefaultPlannerView
import com.lifekernel.app.infra.backup.model.BackupManifest
import com.lifekernel.app.infra.backup.model.BackupSettings
import com.lifekernel.app.infra.backup.registry.BackupJson
import com.lifekernel.app.infra.backup.writer.BackupWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.util.zip.ZipFile

/**
 * End-to-end integration test verifying backup archive creation, ZIP packaging,
 * per-table streaming JSONL, SHA-256 integrity hashes, and portable settings extraction
 * per DATABASE_SCHEMA.md §11, PRD §8.11 (FR-BKP-001, FR-BKP-002), and IMPLEMENTATION_PLAN.md T-017.
 */
@RunWith(AndroidJUnit4::class)
class BackupArchiveWriterIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: LifeKernelDatabase
    private lateinit var clock: FixedClock
    private lateinit var settingsRepository: SettingsRepositoryImpl
    private lateinit var backupWriter: BackupWriter
    private lateinit var backupRepository: BackupRepositoryImpl
    private lateinit var testDir: File
    private lateinit var testDataStoreFile: File
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val fixedInstant = Instant.parse("2026-10-30T15:30:00Z")
    private val fixedZone = ZoneId.of("Europe/Berlin")

    @Before
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        testDir = File(context.cacheDir, "backup_test_${System.currentTimeMillis()}").apply { mkdirs() }
        testDataStoreFile = File(testDir, "test_settings.preferences_pb")

        val dataStore = SettingsPreferences.createTestDataStore(testDataStoreFile, scope)
        settingsRepository = SettingsRepositoryImpl(dataStore, Dispatchers.IO)
        database = LifeKernelDatabase.createInMemory(context)
        clock = FixedClock(fixedInstant, fixedZone)

        backupWriter = BackupWriter(
            database = database,
            settingsRepository = settingsRepository,
            clock = clock,
            dispatcher = Dispatchers.IO,
            appVersion = "0.1.0"
        )
        backupRepository = BackupRepositoryImpl(backupWriter, Dispatchers.IO)

        // Seed settings
        settingsRepository.setThemePalette("tokyo_night")
        settingsRepository.setDefaultPlannerView(DefaultPlannerView.TASKS)

        // Seed database
        val tag1Id = database.tagDao().insertTag(TagEntity(name = "work", createdAt = 1000L))
        val tag2Id = database.tagDao().insertTag(TagEntity(name = "personal", createdAt = 1000L))

        val parentTaskId = database.taskDao().insertTask(
            TaskEntity(
                title = "Launch Life Kernel M5",
                description = "Backup and restore milestone",
                status = 0,
                priority = 3,
                startDate = "2026-10-30",
                dueDate = "2026-10-31",
                parentId = null,
                createdAt = 1000L,
                updatedAt = 1000L
            )
        )

        val subtaskId = database.taskDao().insertTask(
            TaskEntity(
                title = "Implement T-017 writer",
                description = "ZIP, manifest, JSONL",
                status = 1,
                priority = 2,
                parentId = parentTaskId,
                sortOrder = 0,
                createdAt = 1050L,
                updatedAt = 1050L
            )
        )

        database.taskTagDao().insertTaskTag(TaskTagEntity(taskId = parentTaskId, tagId = tag1Id))
        database.taskTagDao().insertTaskTag(TaskTagEntity(taskId = parentTaskId, tagId = tag2Id))

        database.taskCompletionDao().insertCompletion(
            TaskCompletionEntity(
                taskId = subtaskId,
                occurrenceDate = null,
                completedAt = 1100L,
                completedOn = "2026-10-30"
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
        testDir.deleteRecursively()
    }

    @Test
    fun createBackup_generatesValidZipWithCorrectManifestAndHashes() = runBlocking {
        val targetZip = File(testDir, "lifekernel-backup-20261030-1530.zip")

        val result = backupRepository.createBackup(targetZip)
        assertTrue("Backup creation must succeed", result is AppResult.Success)
        val createdFile = (result as AppResult.Success).data
        assertEquals(targetZip.absolutePath, createdFile.absolutePath)
        assertTrue("Backup file must exist", targetZip.exists())
        assertTrue("Backup file size must be > 0", targetZip.length() > 0)

        // Verify temporary file does not linger
        val tempFile = File(testDir, "${targetZip.name}.tmp")
        assertFalse("Temporary file must be cleaned up after completion", tempFile.exists())

        // Inspect ZIP structure
        ZipFile(targetZip).use { zip ->
            val manifestEntry = zip.getEntry("manifest.json")
            assertNotNull("manifest.json must exist at ZIP root", manifestEntry)

            val settingsEntry = zip.getEntry("settings.json")
            assertNotNull("settings.json must exist at ZIP root", settingsEntry)

            val tagsEntry = zip.getEntry("data/tags.jsonl")
            assertNotNull("data/tags.jsonl must exist", tagsEntry)

            val tasksEntry = zip.getEntry("data/tasks.jsonl")
            assertNotNull("data/tasks.jsonl must exist", tasksEntry)

            val taskTagsEntry = zip.getEntry("data/task_tags.jsonl")
            assertNotNull("data/task_tags.jsonl must exist", taskTagsEntry)

            val taskCompletionsEntry = zip.getEntry("data/task_completions.jsonl")
            assertNotNull("data/task_completions.jsonl must exist", taskCompletionsEntry)

            // Read and verify settings.json
            val settingsContent = zip.getInputStream(settingsEntry).bufferedReader().readText()
            val settings = BackupJson.decodeFromString(BackupSettings.serializer(), settingsContent)
            assertEquals("tokyo_night", settings.themePaletteId)
            assertEquals("TASKS", settings.defaultPlannerView)

            // Read and verify manifest.json
            val manifestContent = zip.getInputStream(manifestEntry).bufferedReader().readText()
            val manifest = BackupJson.decodeFromString(BackupManifest.serializer(), manifestContent)
            assertEquals(1, manifest.formatVersion)
            assertEquals("0.1.0", manifest.appVersion)
            assertEquals(1, manifest.roomSchemaVersion)
            assertEquals(fixedInstant.toEpochMilli(), manifest.createdAtUtcMs)
            assertEquals(fixedZone.id, manifest.deviceZoneId)
            assertEquals(4, manifest.files.size)

            // Verify row counts and recalculated SHA-256 hashes per data file
            for (fileEntry in manifest.files) {
                val entry = zip.getEntry(fileEntry.path)
                assertNotNull("File listed in manifest must exist in ZIP: ${fileEntry.path}", entry)

                val bytes = zip.getInputStream(entry).readBytes()
                val lines = bytes.toString(Charsets.UTF_8).lines().filter { it.isNotBlank() }
                assertEquals("Row count in manifest must match actual lines: ${fileEntry.path}", fileEntry.rows, lines.size)

                val digest = MessageDigest.getInstance("SHA-256")
                val actualSha256 = digest.digest(bytes).joinToString("") { "%02x".format(it) }
                assertEquals("SHA-256 in manifest must match actual checksum: ${fileEntry.path}", fileEntry.sha256, actualSha256)
            }

            // Verify specific row counts match database
            val tagsFile = manifest.files.first { it.path == "data/tags.jsonl" }
            assertEquals(2, tagsFile.rows)

            val tasksFile = manifest.files.first { it.path == "data/tasks.jsonl" }
            assertEquals(2, tasksFile.rows)

            val taskTagsFile = manifest.files.first { it.path == "data/task_tags.jsonl" }
            assertEquals(2, taskTagsFile.rows)

            val taskCompletionsFile = manifest.files.first { it.path == "data/task_completions.jsonl" }
            assertEquals(1, taskCompletionsFile.rows)
        }

        // Verify readManifest
        val manifestResult = backupRepository.readManifest(targetZip)
        assertTrue("readManifest must succeed", manifestResult is AppResult.Success)
        val readManifest = (manifestResult as AppResult.Success).data
        assertEquals(1, readManifest.formatVersion)
        assertEquals(1, readManifest.roomSchemaVersion)
        assertEquals(4, readManifest.files.size)
    }

    @Test
    fun readManifest_returnsFailureForInvalidFile() = runBlocking {
        val nonExistent = File(testDir, "missing.zip")
        val result = backupRepository.readManifest(nonExistent)
        assertTrue("Reading non-existent file must fail", result is AppResult.Failure)

        val invalidZip = File(testDir, "corrupted.zip").apply { writeText("not a zip file") }
        val corruptResult = backupRepository.readManifest(invalidZip)
        assertTrue("Reading corrupted ZIP must fail", corruptResult is AppResult.Failure)
    }
}
