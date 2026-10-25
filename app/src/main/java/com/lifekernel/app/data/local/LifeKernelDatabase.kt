package com.lifekernel.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lifekernel.app.data.local.dao.TagDao
import com.lifekernel.app.data.local.dao.TaskCompletionDao
import com.lifekernel.app.data.local.dao.TaskDao
import com.lifekernel.app.data.local.dao.TaskTagDao
import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.data.local.entity.TaskCompletionEntity
import com.lifekernel.app.data.local.entity.TaskEntity
import com.lifekernel.app.data.local.entity.TaskTagEntity

/**
 * Version 1 of the Life Kernel Room database per DATABASE_SCHEMA.md §4 & §6.
 * Contains tables: tags, tasks, task_completions, task_tags.
 * Foreign keys are strictly enforced on open.
 * Destructive migration fallback is intentionally omitted.
 */
@Database(
    entities = [
        TagEntity::class,
        TaskEntity::class,
        TaskCompletionEntity::class,
        TaskTagEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LifeKernelDatabase : RoomDatabase() {

    abstract fun tagDao(): TagDao
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun taskTagDao(): TaskTagDao

    companion object {
        const val DATABASE_NAME = "lifekernel.db"

        fun create(context: Context, databaseName: String = DATABASE_NAME): LifeKernelDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                LifeKernelDatabase::class.java,
                databaseName
            )
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys = ON;")
                    }
                })
                .build()
        }

        fun createInMemory(context: Context): LifeKernelDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                LifeKernelDatabase::class.java
            )
                .allowMainThreadQueries()
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys = ON;")
                    }
                })
                .build()
        }
    }
}
