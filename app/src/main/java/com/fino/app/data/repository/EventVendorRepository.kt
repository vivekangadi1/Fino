package com.fino.app.data.repository

import com.fino.app.data.local.dao.EventVendorDao
import com.fino.app.data.local.entity.EventVendorEntity
import com.fino.app.domain.model.EventVendor
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventVendorRepository @Inject constructor(
    private val dao: EventVendorDao
) {

    fun getByEventIdFlow(eventId: Long): Flow<List<EventVendor>> {
        return dao.getByEventIdFlow(eventId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getByEventId(eventId: Long): List<EventVendor> {
        return dao.getByEventId(eventId).map { it.toDomain() }
    }

    fun getBySubCategoryIdFlow(subCategoryId: Long): Flow<List<EventVendor>> {
        return dao.getBySubCategoryIdFlow(subCategoryId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getBySubCategoryId(subCategoryId: Long): List<EventVendor> {
        return dao.getBySubCategoryId(subCategoryId).map { it.toDomain() }
    }

    suspend fun getById(id: Long): EventVendor? {
        return dao.getById(id)?.toDomain()
    }

    fun getByIdFlow(id: Long): Flow<EventVendor?> {
        return dao.getByIdFlow(id).map { it?.toDomain() }
    }

    suspend fun insert(vendor: EventVendor): Long {
        return dao.insert(vendor.toEntity())
    }

    suspend fun insertAll(vendors: List<EventVendor>): List<Long> {
        return dao.insertAll(vendors.map { it.toEntity() })
    }

    suspend fun update(vendor: EventVendor) {
        val updated = vendor.copy(updatedAt = LocalDateTime.now())
        dao.update(updated.toEntity())
    }

    suspend fun delete(vendor: EventVendor) {
        dao.delete(vendor.toEntity())
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

    suspend fun getCountBySubCategoryId(subCategoryId: Long): Int {
        return dao.getCountBySubCategoryId(subCategoryId)
    }

    suspend fun getTotalQuotedByEventId(eventId: Long): Double {
        return dao.getTotalQuotedByEventId(eventId) ?: 0.0
    }

    suspend fun getTotalQuotedBySubCategoryId(subCategoryId: Long): Double {
        return dao.getTotalQuotedBySubCategoryId(subCategoryId) ?: 0.0
    }

    suspend fun search(eventId: Long, query: String): List<EventVendor> {
        return dao.searchByEventId(eventId, query).map { it.toDomain() }
    }

    private fun EventVendorEntity.toDomain(): EventVendor {
        return EventVendor(
            id = id,
            eventId = eventId,
            subCategoryId = subCategoryId,
            name = name,
            description = description,
            phone = phone,
            email = email,
            quotedAmount = quotedAmount,
            notes = notes,
            createdAt = DateUtils.fromEpochMillis(createdAt),
            updatedAt = DateUtils.fromEpochMillis(updatedAt)
        )
    }

    private fun EventVendor.toEntity(): EventVendorEntity {
        return EventVendorEntity(
            id = id,
            eventId = eventId,
            subCategoryId = subCategoryId,
            name = name,
            description = description,
            phone = phone,
            email = email,
            quotedAmount = quotedAmount,
            notes = notes,
            createdAt = DateUtils.toEpochMillis(createdAt),
            updatedAt = DateUtils.toEpochMillis(updatedAt)
        )
    }
}
