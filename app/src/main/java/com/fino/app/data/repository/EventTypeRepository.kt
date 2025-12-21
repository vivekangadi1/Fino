package com.fino.app.data.repository

import com.fino.app.data.local.dao.EventTypeDao
import com.fino.app.data.local.entity.EventTypeEntity
import com.fino.app.domain.model.EventType
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventTypeRepository @Inject constructor(
    private val dao: EventTypeDao
) {

    fun getAllActiveFlow(): Flow<List<EventType>> {
        return dao.getAllActiveFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getAllActive(): List<EventType> {
        return dao.getAllActive().map { it.toDomain() }
    }

    suspend fun getById(id: Long): EventType? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun getSystemTypes(): List<EventType> {
        return dao.getSystemTypes().map { it.toDomain() }
    }

    suspend fun getCustomTypes(): List<EventType> {
        return dao.getCustomTypes().map { it.toDomain() }
    }

    suspend fun insert(eventType: EventType): Long {
        return dao.insert(eventType.toEntity())
    }

    suspend fun update(eventType: EventType) {
        dao.update(eventType.toEntity())
    }

    suspend fun delete(eventType: EventType) {
        dao.delete(eventType.toEntity())
    }

    /**
     * Add a custom event type with proper sort order
     */
    suspend fun addCustomType(name: String, emoji: String): Long {
        val maxSortOrder = dao.getMaxSortOrder() ?: 0
        val eventType = EventType(
            name = name,
            emoji = emoji,
            isSystem = false,
            sortOrder = maxSortOrder + 1,
            isActive = true,
            createdAt = LocalDateTime.now()
        )
        return dao.insert(eventType.toEntity())
    }

    private fun EventTypeEntity.toDomain(): EventType {
        return EventType(
            id = id,
            name = name,
            emoji = emoji,
            isSystem = isSystem,
            sortOrder = sortOrder,
            isActive = isActive,
            createdAt = DateUtils.fromEpochMillis(createdAt)
        )
    }

    private fun EventType.toEntity(): EventTypeEntity {
        return EventTypeEntity(
            id = id,
            name = name,
            emoji = emoji,
            isSystem = isSystem,
            sortOrder = sortOrder,
            isActive = isActive,
            createdAt = DateUtils.toEpochMillis(createdAt)
        )
    }
}
