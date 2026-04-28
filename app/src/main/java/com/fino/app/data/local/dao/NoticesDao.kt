package com.fino.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.fino.app.data.local.entity.NoticesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notice: NoticesEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notices: List<NoticesEntity>): List<Long>

    @Query("DELETE FROM notices WHERE period = :period")
    suspend fun deleteByPeriod(period: String)

    @Query("SELECT * FROM notices WHERE period = :period ORDER BY rankOrder ASC")
    fun getByPeriodFlow(period: String): Flow<List<NoticesEntity>>

    @Query("SELECT * FROM notices WHERE period = :period ORDER BY rankOrder ASC")
    suspend fun getByPeriod(period: String): List<NoticesEntity>

    @Transaction
    suspend fun replaceForPeriod(period: String, notices: List<NoticesEntity>) {
        deleteByPeriod(period)
        if (notices.isNotEmpty()) insertAll(notices)
    }
}
