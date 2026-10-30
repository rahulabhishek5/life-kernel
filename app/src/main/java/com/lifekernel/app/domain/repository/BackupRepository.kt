package com.lifekernel.app.domain.repository

import com.lifekernel.app.core.result.AppResult
import com.lifekernel.app.infra.backup.model.BackupManifest
import java.io.File

/**
 * Domain contract for backup archive creation and inspection per PRD §8.11 (FR-BKP-001, FR-BKP-002)
 * and ARCHITECTURE.md §9.
 *
 * Invariant: All file operations and database extraction must execute off the main thread.
 */
interface BackupRepository {
    /**
     * Creates a full, unencrypted backup archive of the database and portable settings.
     * The archive is written atomically to a temporary file before being finalized at [targetFile].
     *
     * @param targetFile Destination file for the .zip archive.
     * @return [AppResult.Success] containing the finalized archive [File], or [AppResult.Failure].
     */
    suspend fun createBackup(targetFile: File): AppResult<File>

    /**
     * Reads and parses the [BackupManifest] embedded in an existing backup archive.
     *
     * @param backupFile The .zip archive file to inspect.
     * @return [AppResult.Success] with the parsed [BackupManifest], or [AppResult.Failure].
     */
    suspend fun readManifest(backupFile: File): AppResult<BackupManifest>
}
