package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.AchievementEntity
import com.fino.app.domain.model.AchievementType
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: String): AchievementEntity?

    @Query("SELECT * FROM achievements ORDER BY type, requirement")
    fun getAllFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY type, requirement")
    suspend fun getAll(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    suspend fun getUnlocked(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NULL ORDER BY progress DESC")
    suspend fun getLocked(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE type = :type ORDER BY requirement")
    suspend fun getByType(type: AchievementType): List<AchievementEntity>

    @Query("SELECT unlockedAt IS NOT NULL FROM achievements WHERE id = :id")
    suspend fun isUnlocked(id: String): Boolean

    @Query("UPDATE achievements SET unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlock(id: String, timestamp: Long)

    @Query("UPDATE achievements SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedCount(): Int
}
