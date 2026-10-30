package com.lifekernel.app.infra.backup

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.data.repository.BackupRepositoryImpl
import com.lifekernel.app.infra.backup.model.BackupFileEntry
import com.lifekernel.app.infra.backup.model.BackupManifest
import com.lifekernel.app.infra.backup.registry.BackupJson
import com.lifekernel.app.infra.backup.writer.BackupWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Unit tests for [BackupRepositoryImpl] archive generation delegation, manifest reading,
 * and error propagation per ARCHITECTURE.md §11 and PRD §8.11.
 */
class BackupRepositoryTest {

    private lateinit var tempDir: File
    private lateinit var repository: BackupRepositoryImpl
    private var writerThrows: Boolean = false

    @Before
    fun setUp() {
        tempDir = File.createTempFile("backup_repo_test_", "").apply {
            delete()
            mkdirs()
        }
        writerThrows = false

        val fakeWriter = object : BackupWriter {
            override suspend fun writeBackup(targetFile: File): BackupManifest {
                if (writerThrows) {
                    throw IOException("Simulated disk I/O failure")
                }
                targetFile.writeText("sample zip content")
                return BackupManifest(
                    formatVersion = 1,
                    appVersion = "0.1.0",
                    roomSchemaVersion = 1,
                    createdAtUtcMs = 1790000000000L,
                    deviceZoneId = "UTC",
                    files = emptyList()
                )
            }
        }
        repository = BackupRepositoryImpl(fakeWriter, Dispatchers.IO)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun createBackup_delegatesToWriterAndReturnsSuccess() = runBlocking {
        val target = File(tempDir, "test_backup.zip")
        val result = repository.createBackup(target)

        assertTrue("createBackup must succeed", result is AppResult.Success)
        val file = (result as AppResult.Success).data
        assertEquals(target, file)
        assertTrue(target.exists())
    }

    @Test
    fun createBackup_handlesWriterExceptionAndReturnsStorageError() = runBlocking {
        writerThrows = true
        val target = File(tempDir, "test_backup.zip")
        val result = repository.createBackup(target)

        assertTrue("createBackup must return Failure on error", result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue("Error must be Storage error", error is AppError.Storage)
        assertTrue(error.message.contains("Simulated disk I/O failure"))
    }

    @Test
    fun readManifest_parsesValidZipArchive() = runBlocking {
        val zipFile = File(tempDir, "sample_backup.zip")
        val manifest = BackupManifest(
            formatVersion = 1,
            appVersion = "0.1.0",
            roomSchemaVersion = 1,
            createdAtUtcMs = 1790000000000L,
            deviceZoneId = "UTC",
            files = listOf(
                BackupFileEntry("data/tasks.jsonl", 10, "abcdef1234567890")
            )
        )
        val manifestBytes = BackupJson.encodeToString(BackupManifest.serializer(), manifest).toByteArray(Charsets.UTF_8)

        ZipOutputStream(FileOutputStream(zipFile).buffered()).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("manifest.json"))
            zipOut.write(manifestBytes)
            zipOut.closeEntry()
        }

        val result = repository.readManifest(zipFile)
        assertTrue("readManifest must succeed for valid zip", result is AppResult.Success)
        val parsed = (result as AppResult.Success).data
        assertEquals(manifest, parsed)
    }

    @Test
    fun readManifest_returnsFailureWhenManifestIsMissing() = runBlocking {
        val zipFile = File(tempDir, "missing_manifest.zip")
        ZipOutputStream(FileOutputStream(zipFile).buffered()).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("settings.json"))
            zipOut.write("{}".toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()
        }

        val result = repository.readManifest(zipFile)
        assertTrue("readManifest must fail when manifest.json is absent", result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error.message.contains("missing manifest.json"))
    }

    @Test
    fun readManifest_returnsFailureForCorruptFile() = runBlocking {
        val corruptFile = File(tempDir, "corrupt.zip").apply {
            writeText("not a valid zip file")
        }

        val result = repository.readManifest(corruptFile)
        assertTrue("readManifest must fail for corrupted zip", result is AppResult.Failure)
    }

    @Test
    fun readManifest_returnsFailureForNonExistentFile() = runBlocking {
        val missingFile = File(tempDir, "does_not_exist.zip")
        val result = repository.readManifest(missingFile)
        assertTrue("readManifest must fail for non-existent file", result is AppResult.Failure)
    }
}
