package com.lifekernel.app.infra.backup.serializer

import com.lifekernel.app.data.local.entity.TagEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Serialization DTO for the `tags` table in JSONL format per DATABASE_SCHEMA.md §6.1 & §11.1.
 */
@Serializable
data class TagBackupDto(
    @SerialName("id")
    val id: Long,

    @SerialName("name")
    val name: String,

    @SerialName("created_at")
    val createdAt: Long
)

fun TagEntity.toBackupDto(): TagBackupDto = TagBackupDto(
    id = id,
    name = name,
    createdAt = createdAt
)

fun TagBackupDto.toEntity(): TagEntity = TagEntity(
    id = id,
    name = name,
    createdAt = createdAt
)
