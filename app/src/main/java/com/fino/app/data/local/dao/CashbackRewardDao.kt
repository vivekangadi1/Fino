package com.fino.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fino.app.data.local.entity.CashbackRewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashbackRewardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reward: CashbackRewardEntity): Long

    @Update
    suspend fun update(reward: CashbackRewardEntity)

    @Delete
    suspend fun delete(reward: CashbackRewardEntity)

    @Query("SELECT * FROM cashback_rewards WHERE id = :id")
    suspend fun getById(id: Long): CashbackRewardEntity?

    @Query("SELECT * FROM cashback_rewards ORDER BY creditedAt DESC")
    fun getAllFlow(): Flow<List<CashbackRewardEntity>>

    @Query("SELECT * FROM cashback_rewards WHERE period = :period ORDER BY creditedAt DESC")
    fun getByPeriodFlow(period: String): Flow<List<CashbackRewardEntity>>

    @Query("SELECT * FROM cashback_rewards WHERE period = :period ORDER BY creditedAt DESC")
    suspend fun getByPeriod(period: String): List<CashbackRewardEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cashback_rewards WHERE period = :period")
    suspend fun getTotalForPeriod(period: String): Double
}
