package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.EventVendorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventVendorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vendor: EventVendorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vendors: List<EventVendorEntity>): List<Long>

    @Update
    suspend fun update(vendor: EventVendorEntity)

    @Delete
    suspend fun delete(vendor: EventVendorEntity)

    @Query("DELETE FROM event_vendors WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM event_vendors WHERE id = :id")
    suspend fun getById(id: Long): EventVendorEntity?

    @Query("SELECT * FROM event_vendors WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<EventVendorEntity?>

    @Query("SELECT * FROM event_vendors WHERE eventId = :eventId ORDER BY name ASC")
    fun getByEventIdFlow(eventId: Long): Flow<List<EventVendorEntity>>

    @Query("SELECT * FROM event_vendors WHERE eventId = :eventId ORDER BY name ASC")
    suspend fun getByEventId(eventId: Long): List<EventVendorEntity>

    @Query("SELECT * FROM event_vendors WHERE subCategoryId = :subCategoryId ORDER BY name ASC")
    fun getBySubCategoryIdFlow(subCategoryId: Long): Flow<List<EventVendorEntity>>

    @Query("SELECT * FROM event_vendors WHERE subCategoryId = :subCategoryId ORDER BY name ASC")
    suspend fun getBySubCategoryId(subCategoryId: Long): List<EventVendorEntity>

    @Query("SELECT COUNT(*) FROM event_vendors WHERE eventId = :eventId")
    suspend fun getCountByEventId(eventId: Long): Int

    @Query("SELECT COUNT(*) FROM event_vendors WHERE subCategoryId = :subCategoryId")
    suspend fun getCountBySubCategoryId(subCategoryId: Long): Int

    @Query("SELECT SUM(quotedAmount) FROM event_vendors WHERE eventId = :eventId AND quotedAmount IS NOT NULL")
    suspend fun getTotalQuotedByEventId(eventId: Long): Double?

    @Query("SELECT SUM(quotedAmount) FROM event_vendors WHERE subCategoryId = :subCategoryId AND quotedAmount IS NOT NULL")
    suspend fun getTotalQuotedBySubCategoryId(subCategoryId: Long): Double?

    @Query("SELECT * FROM event_vendors WHERE eventId = :eventId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchByEventId(eventId: Long, query: String): List<EventVendorEntity>

    @Query("DELETE FROM event_vendors WHERE eventId = :eventId")
    suspend fun deleteAllByEventId(eventId: Long)

    @Query("UPDATE event_vendors SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTimestamp(id: Long, updatedAt: Long)
}
