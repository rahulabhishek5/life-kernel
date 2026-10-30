package com.lifekernel.app.data.repository

import com.lifekernel.app.core.result.AppError
import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.domain.repository.BackupRepository
import com.lifekernel.app.infra.backup.model.BackupManifest
import com.lifekernel.app.infra.backup.registry.BackupJson
import com.lifekernel.app.infra.backup.writer.BackupWriter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

/**
 * Implementation of [BackupRepository] coordinating [BackupWriter] and archive inspection.
 *
 * Invariant: All file I/O operations execute strictly on [dispatcher] (Dispatchers.IO).
 */
class BackupRepositoryImpl(
    private val backupWriter: BackupWriter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BackupRepository {

    override suspend fun createBackup(targetFile: File): AppResult<File> = withContext(dispatcher) {
        try {
            backupWriter.writeBackup(targetFile)
            AppResult.success(targetFile)
        } catch (e: Exception) {
            AppResult.failure(AppError.Storage("Failed to generate backup archive: ${e.message}", e))
        }
    }

    override suspend fun readManifest(backupFile: File): AppResult<BackupManifest> = withContext(dispatcher) {
        try {
            if (!backupFile.exists() || !backupFile.isFile) {
                return@withContext AppResult.failure(
                    AppError.NotFound("Backup file not found: ${backupFile.absolutePath}")
                )
            }
            ZipFile(backupFile).use { zip ->
                val entry = zip.getEntry("manifest.json")
                    ?: return@withContext AppResult.failure(
                        AppError.Validation("Backup archive missing manifest.json")
                    )
                val manifestContent = zip.getInputStream(entry).bufferedReader().readText()
                val manifest = BackupJson.decodeFromString(BackupManifest.serializer(), manifestContent)
                AppResult.success(manifest)
            }
        } catch (e: Exception) {
            AppResult.failure(
                AppError.Validation("Failed to read backup manifest: ${e.message}", cause = e)
            )
        }
    }
}
