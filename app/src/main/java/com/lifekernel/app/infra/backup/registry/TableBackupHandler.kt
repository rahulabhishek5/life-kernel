package com.lifekernel.app.infra.backup.registry

import com.lifekernel.app.data.local.LifeKernelDatabase
import kotlin.reflect.KClass

/**
 * Contract for a table-specific backup handler.
 * Maps a Room database entity to its backup JSONL file per DATABASE_SCHEMA.md §11.1.
 */
interface TableBackupHandler {
    val tableName: String
    val entityClass: KClass<out Any>
    val relativePath: String

    /**
     * Streams all rows for this table from [database], writing each row as a single JSON line
     * via [writeLine]. Returns the total number of rows exported.
     */
    suspend fun exportRows(database: LifeKernelDatabase, writeLine: (String) -> Unit): Int
}
