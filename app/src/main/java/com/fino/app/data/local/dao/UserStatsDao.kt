package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: UserStatsEntity)

    @Update
    suspend fun update(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("UPDATE user_stats SET currentStreak = :streak, longestStreak = :longestStreak, lastActiveDate = :lastActiveDate WHERE id = 1")
    suspend fun updateStreak(streak: Int, longestStreak: Int, lastActiveDate: Long)

    @Query("UPDATE user_stats SET totalXp = totalXp + :xp WHERE id = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE user_stats SET currentLevel = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)

    @Query("UPDATE user_stats SET totalTransactionsLogged = totalTransactionsLogged + 1 WHERE id = 1")
    suspend fun incrementTransactionCount()

    @Query("SELECT totalXp FROM user_stats WHERE id = 1")
    suspend fun getTotalXp(): Int?
}
