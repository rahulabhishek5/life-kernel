package com.lifekernel.app.infra.backup

import com.lifekernel.app.infra.backup.registry.BackupRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Automated completeness enforcement test for the table backup registry.
 *
 * Verifies the architectural invariant from IMPLEMENTATION_PLAN.md T-017 and DATABASE_SCHEMA.md §11:
 * Every Room database entity and table must be explicitly mapped in [BackupRegistry].
 * This test statically inspects [LifeKernelDatabase] and committed Room schemas to guarantee
 * zero omitted tables during schema migrations.
 */
class BackupRegistryTest {

    @Test
    fun databaseEntities_areAllRegisteredInBackupRegistry() {
        val databaseSource = findDatabaseSourceFile()
        val content = databaseSource.readText()

        val entitiesRegex = Regex("""entities\s*=\s*\[([^\]]+)\]""", RegexOption.DOT_MATCHES_ALL)
        val match = entitiesRegex.find(content)
        assertNotNull("LifeKernelDatabase.kt must declare entities = [...]", match)

        val declaredEntityNames = match!!.groupValues[1]
            .split(",")
            .map { it.trim().removeSuffix("::class").trim() }
            .filter { it.isNotBlank() }

        assertTrue("LifeKernelDatabase must have declared entities", declaredEntityNames.isNotEmpty())

        for (entityName in declaredEntityNames) {
            val hasHandler = BackupRegistry.handlers.any { it.entityClass.simpleName == entityName }
            assertTrue(
                "Room entity $entityName is declared in LifeKernelDatabase but missing from BackupRegistry! " +
                    "Add a TableBackupHandler to BackupRegistry to ensure backups do not omit this table.",
                hasHandler
            )
        }

        assertEquals(
            "BackupRegistry handlers count must exactly match the number of entities in LifeKernelDatabase",
            declaredEntityNames.size,
            BackupRegistry.handlers.size
        )
    }

    @Test
    fun roomExportedSchemaTables_areAllRegisteredInBackupRegistry() {
        val schemasDir = findSchemasDirectory()
        val schemaFiles = schemasDir.listFiles { _, name -> name.endsWith(".json") }
            ?.sortedBy { it.nameWithoutExtension.toIntOrNull() ?: 0 }

        assertTrue("Room schema JSON files must exist in $schemasDir", !schemaFiles.isNullOrEmpty())
        val latestSchema = schemaFiles!!.last()
        val schemaContent = latestSchema.readText()

        val tableNamesRegex = Regex(""""tableName"\s*:\s*"([^"]+)"""")
        val exportedTableNames = tableNamesRegex.findAll(schemaContent)
            .map { it.groupValues[1] }
            .distinct()
            .toList()

        assertTrue("Exported Room schema must contain tables", exportedTableNames.isNotEmpty())

        for (tableName in exportedTableNames) {
            val handler = BackupRegistry.getHandlerForTable(tableName)
            assertNotNull(
                "Table '$tableName' in Room schema (${latestSchema.name}) is missing from BackupRegistry!",
                handler
            )
        }

        assertEquals(
            "BackupRegistry handlers count must match number of tables in exported Room schema",
            exportedTableNames.size,
            BackupRegistry.handlers.size
        )
    }

    @Test
    fun backupRegistry_allHandlersHaveValidPathsAndTables() {
        for (handler in BackupRegistry.handlers) {
            assertTrue("Handler table name must not be blank", handler.tableName.isNotBlank())
            assertEquals(
                "Relative path must follow 'data/<table>.jsonl' convention per DATABASE_SCHEMA.md §11.1",
                "data/${handler.tableName}.jsonl",
                handler.relativePath
            )
            val retrieved = BackupRegistry.getHandlerForTable(handler.tableName)
            assertEquals("getHandlerForTable must locate handler by table name", handler, retrieved)
        }
    }

    private fun findDatabaseSourceFile(): File {
        val candidates = listOf(
            File("app/src/main/java/com/lifekernel/app/data/local/LifeKernelDatabase.kt"),
            File("src/main/java/com/lifekernel/app/data/local/LifeKernelDatabase.kt"),
            File("../app/src/main/java/com/lifekernel/app/data/local/LifeKernelDatabase.kt")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Cannot locate LifeKernelDatabase.kt source file in candidates: $candidates")
    }

    private fun findSchemasDirectory(): File {
        val candidates = listOf(
            File("app/schemas/com.lifekernel.app.data.local.LifeKernelDatabase"),
            File("schemas/com.lifekernel.app.data.local.LifeKernelDatabase"),
            File("../app/schemas/com.lifekernel.app.data.local.LifeKernelDatabase")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Cannot locate Room schemas directory in candidates: $candidates")
    }
}
