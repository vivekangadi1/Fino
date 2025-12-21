package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.EventEntity
import com.fino.app.domain.model.EventStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE isActive = 1 ORDER BY startDate DESC")
    fun getAllActiveFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE isActive = 1 ORDER BY startDate DESC")
    suspend fun getAllActive(): List<EventEntity>

    @Query("SELECT * FROM events WHERE status = :status ORDER BY startDate DESC")
    fun getByStatusFlow(status: EventStatus): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE status = 'ACTIVE' ORDER BY updatedAt DESC LIMIT 1")
    fun getMostRecentActiveFlow(): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE status = 'ACTIVE' AND isActive = 1 ORDER BY startDate DESC")
    fun getActiveEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE startDate <= :date AND (endDate IS NULL OR endDate >= :date) ORDER BY startDate DESC")
    suspend fun getEventsForDate(date: Long): List<EventEntity>

    @Query("UPDATE events SET status = :status, updatedAt = :updatedAt WHERE id = :eventId")
    suspend fun updateStatus(eventId: Long, status: EventStatus, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM events WHERE status = 'ACTIVE' AND isActive = 1")
    suspend fun getActiveEventCount(): Int

    @Query("SELECT * FROM events WHERE eventTypeId = :typeId ORDER BY startDate DESC")
    suspend fun getByEventType(typeId: Long): List<EventEntity>
}
