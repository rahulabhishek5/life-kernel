package com.lifekernel.app.domain.model

import java.time.Instant

/**
 * Tag domain model per DATABASE_SCHEMA.md §6.1.
 */
data class Tag(
    val id: Long = 0,
    val name: String,
    val createdAt: Instant
)
