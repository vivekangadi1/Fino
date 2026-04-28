package com.fino.app.domain.model

import com.fino.app.data.local.entity.EventMemberEntity

data class EventMember(
    val id: Long = 0,
    val eventId: Long,
    val name: String,
    val avatarSeed: String,
    val sharePercent: Float = 0f,
    val isPayer: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun EventMemberEntity.toDomain(): EventMember = EventMember(
    id = id,
    eventId = eventId,
    name = name,
    avatarSeed = avatarSeed,
    sharePercent = sharePercent,
    isPayer = isPayer,
    createdAt = createdAt
)

fun EventMember.toEntity(): EventMemberEntity = EventMemberEntity(
    id = id,
    eventId = eventId,
    name = name,
    avatarSeed = avatarSeed,
    sharePercent = sharePercent,
    isPayer = isPayer,
    createdAt = createdAt
)
