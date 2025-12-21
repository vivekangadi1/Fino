package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.EventTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(eventType: EventTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(eventTypes: List<EventTypeEntity>)

    @Update
    suspend fun update(eventType: EventTypeEntity)

    @Delete
    suspend fun delete(eventType: EventTypeEntity)

    @Query("SELECT * FROM event_types WHERE id = :id")
    suspend fun getById(id: Long): EventTypeEntity?

    @Query("SELECT * FROM event_types WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getAllActiveFlow(): Flow<List<EventTypeEntity>>

    @Query("SELECT * FROM event_types WHERE isActive = 1 ORDER BY sortOrder ASC")
    suspend fun getAllActive(): List<EventTypeEntity>

    @Query("SELECT * FROM event_types WHERE isSystem = 1 AND isActive = 1 ORDER BY sortOrder ASC")
    suspend fun getSystemTypes(): List<EventTypeEntity>

    @Query("SELECT * FROM event_types WHERE isSystem = 0 AND isActive = 1 ORDER BY sortOrder ASC")
    suspend fun getCustomTypes(): List<EventTypeEntity>

    @Query("SELECT COUNT(*) FROM event_types WHERE isActive = 1")
    suspend fun getActiveTypeCount(): Int

    @Query("SELECT MAX(sortOrder) FROM event_types")
    suspend fun getMaxSortOrder(): Int?
}
