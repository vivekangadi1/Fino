package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.EventSubCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventSubCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subCategory: EventSubCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subCategories: List<EventSubCategoryEntity>): List<Long>

    @Update
    suspend fun update(subCategory: EventSubCategoryEntity)

    @Delete
    suspend fun delete(subCategory: EventSubCategoryEntity)

    @Query("DELETE FROM event_sub_categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM event_sub_categories WHERE id = :id")
    suspend fun getById(id: Long): EventSubCategoryEntity?

    @Query("SELECT * FROM event_sub_categories WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<EventSubCategoryEntity?>

    @Query("SELECT * FROM event_sub_categories WHERE eventId = :eventId ORDER BY sortOrder ASC, name ASC")
    fun getByEventIdFlow(eventId: Long): Flow<List<EventSubCategoryEntity>>

    @Query("SELECT * FROM event_sub_categories WHERE eventId = :eventId ORDER BY sortOrder ASC, name ASC")
    suspend fun getByEventId(eventId: Long): List<EventSubCategoryEntity>

    @Query("SELECT COUNT(*) FROM event_sub_categories WHERE eventId = :eventId")
    suspend fun getCountByEventId(eventId: Long): Int

    @Query("SELECT SUM(budgetAmount) FROM event_sub_categories WHERE eventId = :eventId AND budgetAmount IS NOT NULL")
    suspend fun getTotalBudgetByEventId(eventId: Long): Double?

    @Query("SELECT * FROM event_sub_categories WHERE eventId = :eventId AND name LIKE '%' || :query || '%' ORDER BY sortOrder ASC")
    suspend fun searchByEventId(eventId: Long, query: String): List<EventSubCategoryEntity>

    @Query("UPDATE event_sub_categories SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("DELETE FROM event_sub_categories WHERE eventId = :eventId")
    suspend fun deleteAllByEventId(eventId: Long)
}
