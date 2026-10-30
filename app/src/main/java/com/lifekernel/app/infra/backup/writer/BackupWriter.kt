package com.lifekernel.app.infra.backup.writer

import com.lifekernel.app.core.time.Clock
import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.domain.repository.SettingsRepository
import com.lifekernel.app.infra.backup.model.BackupFileEntry
import com.lifekernel.app.infra.backup.model.BackupManifest
import com.lifekernel.app.infra.backup.model.BackupSettings
import com.lifekernel.app.infra.backup.registry.BackupJson
import com.lifekernel.app.infra.backup.registry.BackupRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Contract for backup archive writer engine per DATABASE_SCHEMA.md §11, PRD §8.11 (FR-BKP-001, FR-BKP-002),
 * and ARCHITECTURE.md §9.
 */
interface BackupWriter {
    /**
     * Creates a full, atomic backup archive written to [targetFile].
     *
     * @param targetFile Destination archive file (e.g. `lifekernel-backup-20261030-1830.zip`).
     * @return The [BackupManifest] embedded in the finalized archive.
     */
    suspend fun writeBackup(targetFile: File): BackupManifest
}

/**
 * Factory function for creating a standard [DefaultBackupWriter].
 */
fun BackupWriter(
    database: LifeKernelDatabase,
    settingsRepository: SettingsRepository,
    clock: Clock,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    appVersion: String = BackupManifest.DEFAULT_APP_VERSION
): BackupWriter = DefaultBackupWriter(database, settingsRepository, clock, dispatcher, appVersion)

/**
 * Default implementation of [BackupWriter].
 *
 * Responsibilities:
 * - Reads all table records from Room DAOs.
 * - Streams JSON Lines data files with SHA-256 hashing and row count calculations.
 * - Extracts portable preferences from DataStore (strictly excluding device-local state).
 * - Packages `manifest.json`, `settings.json`, and all `data/{table}.jsonl` files into a ZIP archive.
 * - Enforces atomic file creation: writes to `.tmp` first, then renames on successful completion.
 */
class DefaultBackupWriter(
    private val database: LifeKernelDatabase,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val appVersion: String = BackupManifest.DEFAULT_APP_VERSION
) : BackupWriter {

    override suspend fun writeBackup(targetFile: File): BackupManifest = withContext(dispatcher) {
        val parentDir = targetFile.canonicalFile.parentFile ?: File(".")
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        val tempTargetFile = File(parentDir, "${targetFile.name}.tmp")
        if (tempTargetFile.exists()) {
            tempTargetFile.delete()
        }

        val scratchDir = File.createTempFile("lk_backup_scratch_", "", parentDir).apply {
            delete()
            mkdirs()
        }

        try {
            val fileEntries = mutableListOf<BackupFileEntry>()

            // 1. Export each registered table into scratch files, calculating row count and SHA-256
            for (handler in BackupRegistry.handlers) {
                val scratchFile = File(scratchDir, "${handler.tableName}.jsonl")
                val digest = MessageDigest.getInstance("SHA-256")

                var rows = 0
                scratchFile.outputStream().use { rawOut ->
                    DigestOutputStream(rawOut, digest).use { digestOut ->
                        BufferedWriter(OutputStreamWriter(digestOut, Charsets.UTF_8)).use { writer ->
                            rows = handler.exportRows(database) { line ->
                                writer.write(line)
                                writer.write("\n")
                            }
                            writer.flush()
                        }
                    }
                }

                val sha256Hex = digest.digest().joinToString("") { "%02x".format(it) }
                fileEntries.add(
                    BackupFileEntry(
                        path = handler.relativePath,
                        rows = rows,
                        sha256 = sha256Hex
                    )
                )
            }

            // 2. Export portable settings
            val currentPalette = settingsRepository.themePalette.first().id
            val currentView = settingsRepository.defaultPlannerView.first().name
            val settings = BackupSettings(
                themePaletteId = currentPalette,
                defaultPlannerView = currentView
            )
            val settingsJson = BackupJson.encodeToString(BackupSettings.serializer(), settings)

            // 3. Generate manifest
            val manifest = BackupManifest(
                formatVersion = BackupManifest.CURRENT_FORMAT_VERSION,
                appVersion = appVersion,
                roomSchemaVersion = LifeKernelDatabase.SCHEMA_VERSION,
                createdAtUtcMs = clock.nowEpochMillis(),
                deviceZoneId = clock.zoneId.id,
                files = fileEntries
            )
            val manifestJson = BackupJson.encodeToString(BackupManifest.serializer(), manifest)

            // 4. Stream into temporary ZIP archive
            ZipOutputStream(FileOutputStream(tempTargetFile).buffered()).use { zipOut ->
                // Entry 1: manifest.json at root
                zipOut.putNextEntry(ZipEntry("manifest.json"))
                zipOut.write(manifestJson.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // Entry 2: settings.json at root
                zipOut.putNextEntry(ZipEntry("settings.json"))
                zipOut.write(settingsJson.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // Entries 3..N: data/<table>.jsonl
                for (entry in fileEntries) {
                    val tableName = entry.path.removePrefix("data/").removeSuffix(".jsonl")
                    val scratchFile = File(scratchDir, "$tableName.jsonl")
                    zipOut.putNextEntry(ZipEntry(entry.path))
                    scratchFile.inputStream().buffered().use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                }
            }

            // 5. Atomic rename to destination
            if (targetFile.exists()) {
                targetFile.delete()
            }
            val renamed = tempTargetFile.renameTo(targetFile)
            if (!renamed) {
                tempTargetFile.copyTo(targetFile, overwrite = true)
                tempTargetFile.delete()
            }

            manifest
        } catch (e: Exception) {
            tempTargetFile.delete()
            throw e
        } finally {
            scratchDir.deleteRecursively()
        }
    }
}
