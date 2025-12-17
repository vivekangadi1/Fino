package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.MerchantMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantMappingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: MerchantMappingEntity): Long

    @Update
    suspend fun update(mapping: MerchantMappingEntity)

    @Delete
    suspend fun delete(mapping: MerchantMappingEntity)

    @Query("SELECT * FROM merchant_mappings WHERE id = :id")
    suspend fun getById(id: Long): MerchantMappingEntity?

    @Query("SELECT * FROM merchant_mappings WHERE rawMerchantName = :rawName LIMIT 1")
    suspend fun findByRawName(rawName: String): MerchantMappingEntity?

    @Query("SELECT * FROM merchant_mappings ORDER BY lastUsedAt DESC")
    fun getAllFlow(): Flow<List<MerchantMappingEntity>>

    @Query("SELECT * FROM merchant_mappings ORDER BY lastUsedAt DESC")
    suspend fun getAll(): List<MerchantMappingEntity>

    @Query("SELECT * FROM merchant_mappings WHERE categoryId = :categoryId")
    suspend fun getByCategory(categoryId: Long): List<MerchantMappingEntity>

    @Query("SELECT * FROM merchant_mappings WHERE confidence >= :minConfidence ORDER BY confidence DESC")
    suspend fun getHighConfidenceMappings(minConfidence: Float): List<MerchantMappingEntity>

    @Query("UPDATE merchant_mappings SET matchCount = matchCount + 1, confidence = MIN(1.0, confidence + 0.05), lastUsedAt = :timestamp WHERE id = :id")
    suspend fun incrementMatchCount(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM merchant_mappings")
    suspend fun getMappingCount(): Int
}
