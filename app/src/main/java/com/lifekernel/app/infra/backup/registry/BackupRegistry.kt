package com.lifekernel.app.infra.backup.registry

import com.lifekernel.app.data.local.LifeKernelDatabase
import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity
import com.lifekernel.app.infra.backup.serializer.TagBackupDto
import com.lifekernel.app.infra.backup.serializer.TaskBackupDto
import com.lifekernel.app.infra.backup.serializer.TaskCompletionBackupDto
import com.lifekernel.app.infra.backup.serializer.TaskTagBackupDto
import com.lifekernel.app.infra.backup.serializer.toBackupDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

val BackupJson = Json {
    explicitNulls = true
    encodeDefaults = true
    ignoreUnknownKeys = true
}

object TagTableBackupHandler : TableBackupHandler {
    override val tableName: String = "tags"
    override val entityClass: KClass<out Any> = TagEntity::class
    override val relativePath: String = "data/tags.jsonl"

    override suspend fun exportRows(database: LifeKernelDatabase, writeLine: (String) -> Unit): Int {
        val rows = database.tagDao().getAllTags()
        for (row in rows) {
            writeLine(BackupJson.encodeToString(TagBackupDto.serializer(), row.toBackupDto()))
        }
        return rows.size
    }
}

object TaskTableBackupHandler : TableBackupHandler {
    override val tableName: String = "tasks"
    override val entityClass: KClass<out Any> = TaskEntity::class
    override val relativePath: String = "data/tasks.jsonl"

    override suspend fun exportRows(database: LifeKernelDatabase, writeLine: (String) -> Unit): Int {
        val rows = database.taskDao().getAllTasks()
        for (row in rows) {
            writeLine(BackupJson.encodeToString(TaskBackupDto.serializer(), row.toBackupDto()))
        }
        return rows.size
    }
}

object TaskTagTableBackupHandler : TableBackupHandler {
    override val tableName: String = "task_tags"
    override val entityClass: KClass<out Any> = TaskTagEntity::class
    override val relativePath: String = "data/task_tags.jsonl"

    override suspend fun exportRows(database: LifeKernelDatabase, writeLine: (String) -> Unit): Int {
        val rows = database.taskTagDao().getAllTaskTags()
        for (row in rows) {
            writeLine(BackupJson.encodeToString(TaskTagBackupDto.serializer(), row.toBackupDto()))
        }
        return rows.size
    }
}

object TaskCompletionTableBackupHandler : TableBackupHandler {
    override val tableName: String = "task_completions"
    override val entityClass: KClass<out Any> = TaskCompletionEntity::class
    override val relativePath: String = "data/task_completions.jsonl"

    override suspend fun exportRows(database: LifeKernelDatabase, writeLine: (String) -> Unit): Int {
        val rows = database.taskCompletionDao().getAllCompletions()
        for (row in rows) {
            writeLine(BackupJson.encodeToString(TaskCompletionBackupDto.serializer(), row.toBackupDto()))
        }
        return rows.size
    }
}

/**
 * Central backup registry mapping Room entities to table serializers per DATABASE_SCHEMA.md §11 & IMPLEMENTATION_PLAN.md T-017.
 *
 * Guaranteed complete by [BackupRegistryTest]: any entity declared in [LifeKernelDatabase] must be registered here.
 */
object BackupRegistry {
    /**
     * Handlers in export dependency order:
     * `tags`, `tasks`, `task_tags`, `task_completions`
     */
    val handlers: List<TableBackupHandler> = listOf(
        TagTableBackupHandler,
        TaskTableBackupHandler,
        TaskTagTableBackupHandler,
        TaskCompletionTableBackupHandler
    )

    fun getHandlerForEntity(entityClass: KClass<*>): TableBackupHandler? {
        return handlers.find { it.entityClass == entityClass }
    }

    fun getHandlerForTable(tableName: String): TableBackupHandler? {
        return handlers.find { it.tableName == tableName }
    }
}
