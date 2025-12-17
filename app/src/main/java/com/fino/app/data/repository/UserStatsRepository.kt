package com.fino.app.data.repository

import com.fino.app.data.local.dao.UserStatsDao
import com.fino.app.data.local.entity.UserStatsEntity
import com.fino.app.domain.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStatsRepository @Inject constructor(
    private val userStatsDao: UserStatsDao
) {
    /**
     * Get user stats (singleton row)
     */
    suspend fun getUserStats(): UserStats? {
        return userStatsDao.getUserStats()?.toDomain()
    }

    /**
     * Get user stats as Flow for reactive updates
     */
    fun getUserStatsFlow(): Flow<UserStats?> {
        return userStatsDao.getUserStatsFlow().map { it?.toDomain() }
    }

    /**
     * Initialize user stats with default values (called on first app launch)
     */
    suspend fun initializeUserStats() {
        val entity = UserStatsEntity(
            id = 1,
            currentStreak = 0,
            longestStreak = 0,
            totalTransactionsLogged = 0,
            totalXp = 0,
            currentLevel = 1,
            lastActiveDate = null,
            createdAt = System.currentTimeMillis()
        )
        userStatsDao.insert(entity)
    }

    /**
     * Add XP to user's total
     */
    suspend fun addXp(xp: Int) {
        userStatsDao.addXp(xp)
    }

    /**
     * Update streak values
     */
    suspend fun updateStreak(currentStreak: Int, longestStreak: Int, lastActiveDate: LocalDate) {
        val epochMillis = lastActiveDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        userStatsDao.updateStreak(currentStreak, longestStreak, epochMillis)
    }

    /**
     * Update current level
     */
    suspend fun updateLevel(level: Int) {
        userStatsDao.updateLevel(level)
    }

    /**
     * Increment transaction count by 1
     */
    suspend fun incrementTransactionCount() {
        userStatsDao.incrementTransactionCount()
    }

    /**
     * Get total XP
     */
    suspend fun getTotalXp(): Int {
        return userStatsDao.getTotalXp() ?: 0
    }

    // Mapping functions

    private fun UserStatsEntity.toDomain(): UserStats {
        return UserStats(
            id = id,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalTransactionsLogged = totalTransactionsLogged,
            totalXp = totalXp,
            currentLevel = currentLevel,
            lastActiveDate = lastActiveDate?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            },
            createdAt = Instant.ofEpochMilli(createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        )
    }
}
