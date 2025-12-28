package com.fino.app.data.repository

import com.fino.app.data.local.dao.MerchantMappingDao
import com.fino.app.data.local.entity.MerchantMappingEntity
import com.fino.app.domain.model.MerchantMapping
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantMappingRepository @Inject constructor(
    private val dao: MerchantMappingDao
) {

    suspend fun findByRawName(rawName: String): MerchantMapping? {
        return dao.findByRawName(rawName)?.toDomain()
    }

    suspend fun findAllMappings(): List<MerchantMapping> {
        return dao.getAll().map { it.toDomain() }
    }

    fun getAllMappingsFlow(): Flow<List<MerchantMapping>> {
        return dao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun insertMapping(mapping: MerchantMapping): Long {
        return dao.insert(mapping.toEntity())
    }

    suspend fun updateMapping(mapping: MerchantMapping) {
        dao.update(mapping.toEntity())
    }

    suspend fun incrementMatchCount(id: Long) {
        dao.incrementMatchCount(id, System.currentTimeMillis())
    }

    suspend fun getHighConfidenceMappings(minConfidence: Float = 0.8f): List<MerchantMapping> {
        return dao.getHighConfidenceMappings(minConfidence).map { it.toDomain() }
    }

    suspend fun deleteMapping(mapping: MerchantMapping) {
        dao.delete(mapping.toEntity())
    }

    suspend fun deleteById(id: Long) {
        dao.getById(id)?.let { dao.delete(it) }
    }

    private fun MerchantMappingEntity.toDomain(): MerchantMapping {
        return MerchantMapping(
            id = id,
            rawMerchantName = rawMerchantName,
            normalizedName = normalizedName,
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            confidence = confidence,
            matchCount = matchCount,
            isFuzzyMatch = isFuzzyMatch,
            createdAt = DateUtils.fromEpochMillis(createdAt),
            lastUsedAt = DateUtils.fromEpochMillis(lastUsedAt)
        )
    }

    private fun MerchantMapping.toEntity(): MerchantMappingEntity {
        return MerchantMappingEntity(
            id = id,
            rawMerchantName = rawMerchantName,
            normalizedName = normalizedName,
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            confidence = confidence,
            matchCount = matchCount,
            isFuzzyMatch = isFuzzyMatch,
            createdAt = DateUtils.toEpochMillis(createdAt),
            lastUsedAt = DateUtils.toEpochMillis(lastUsedAt)
        )
    }
}
