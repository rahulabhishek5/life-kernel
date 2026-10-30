package com.lifekernel.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifekernel.app.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tags per DATABASE_SCHEMA.md §6.1.
 */
@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getTagById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun observeAllTags(): Flow<List<TagEntity>>

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: Long): Int

    @Query("DELETE FROM tags WHERE id NOT IN (SELECT DISTINCT tag_id FROM task_tags)")
    suspend fun deleteOrphanTags(): Int

    @Query("SELECT * FROM tags ORDER BY id ASC")
    suspend fun getAllTags(): List<TagEntity>
}
