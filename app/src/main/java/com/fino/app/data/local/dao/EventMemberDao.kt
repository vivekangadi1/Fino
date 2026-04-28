package com.fino.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fino.app.data.local.entity.EventMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventMemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: EventMemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<EventMemberEntity>): List<Long>

    @Update
    suspend fun update(member: EventMemberEntity)

    @Delete
    suspend fun delete(member: EventMemberEntity)

    @Query("SELECT * FROM event_members WHERE id = :id")
    suspend fun getById(id: Long): EventMemberEntity?

    @Query("SELECT * FROM event_members WHERE eventId = :eventId ORDER BY createdAt ASC")
    fun getByEventFlow(eventId: Long): Flow<List<EventMemberEntity>>

    @Query("SELECT * FROM event_members WHERE eventId = :eventId ORDER BY createdAt ASC")
    suspend fun getByEvent(eventId: Long): List<EventMemberEntity>

    @Query("DELETE FROM event_members WHERE eventId = :eventId")
    suspend fun deleteByEvent(eventId: Long)

    @Query("UPDATE event_members SET isPayer = 0 WHERE eventId = :eventId")
    suspend fun clearPayerForEvent(eventId: Long)

    @Query("UPDATE event_members SET isPayer = 1 WHERE id = :memberId")
    suspend fun setPayer(memberId: Long)
}
