package com.fino.app.data.repository

import com.fino.app.data.local.dao.EventSubCategoryDao
import com.fino.app.data.local.entity.EventSubCategoryEntity
import com.fino.app.domain.model.EventSubCategory
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventSubCategoryRepository @Inject constructor(
    private val dao: EventSubCategoryDao
) {

    fun getByEventIdFlow(eventId: Long): Flow<List<EventSubCategory>> {
        return dao.getByEventIdFlow(eventId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getByEventId(eventId: Long): List<EventSubCategory> {
        return dao.getByEventId(eventId).map { it.toDomain() }
    }

    suspend fun getById(id: Long): EventSubCategory? {
        return dao.getById(id)?.toDomain()
    }

    fun getByIdFlow(id: Long): Flow<EventSubCategory?> {
        return dao.getByIdFlow(id).map { it?.toDomain() }
    }

    suspend fun insert(subCategory: EventSubCategory): Long {
        return dao.insert(subCategory.toEntity())
    }

    suspend fun insertAll(subCategories: List<EventSubCategory>): List<Long> {
        return dao.insertAll(subCategories.map { it.toEntity() })
    }

    suspend fun update(subCategory: EventSubCategory) {
        dao.update(subCategory.toEntity())
    }

    suspend fun delete(subCategory: EventSubCategory) {
        dao.delete(subCategory.toEntity())
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun deleteAllByEventId(eventId: Long) {
        dao.deleteAllByEventId(eventId)
    }

    suspend fun getCountByEventId(eventId: Long): Int {
        return dao.getCountByEventId(eventId)
    }

    suspend fun getTotalBudgetByEventId(eventId: Long): Double {
        return dao.getTotalBudgetByEventId(eventId) ?: 0.0
    }

    suspend fun search(eventId: Long, query: String): List<EventSubCategory> {
        return dao.searchByEventId(eventId, query).map { it.toDomain() }
    }

    suspend fun updateSortOrder(id: Long, sortOrder: Int) {
        dao.updateSortOrder(id, sortOrder)
    }

    /**
     * Create default sub-categories for an event based on its type
     */
    suspend fun createDefaultsForEvent(eventId: Long, eventTypeName: String): List<Long> {
        val defaults = EventSubCategory.getDefaultsForEventType(eventTypeName)
        val subCategories = defaults.mapIndexed { index, (name, emoji) ->
            EventSubCategory(
                eventId = eventId,
                name = name,
                emoji = emoji,
                sortOrder = index,
                createdAt = LocalDateTime.now()
            )
        }
        return insertAll(subCategories)
    }

    private fun EventSubCategoryEntity.toDomain(): EventSubCategory {
        return EventSubCategory(
            id = id,
            eventId = eventId,
            name = name,
            emoji = emoji,
            budgetAmount = budgetAmount,
            sortOrder = sortOrder,
            createdAt = DateUtils.fromEpochMillis(createdAt)
        )
    }

    private fun EventSubCategory.toEntity(): EventSubCategoryEntity {
        return EventSubCategoryEntity(
            id = id,
            eventId = eventId,
            name = name,
            emoji = emoji,
            budgetAmount = budgetAmount,
            sortOrder = sortOrder,
            createdAt = DateUtils.toEpochMillis(createdAt)
        )
    }
}
