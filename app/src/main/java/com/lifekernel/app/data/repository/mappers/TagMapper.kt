package com.lifekernel.app.data.repository.mappers

import com.lifekernel.app.data.local.entity.TagEntity
import com.lifekernel.app.domain.model.Tag
import java.time.Instant

/**
 * Bidirectional mappers between [TagEntity] and domain [Tag].
 */
fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    name = name,
    createdAt = Instant.ofEpochMilli(createdAt)
)

fun Tag.toEntity(): TagEntity = TagEntity(
    id = id,
    name = name,
    createdAt = createdAt.toEpochMilli()
)
