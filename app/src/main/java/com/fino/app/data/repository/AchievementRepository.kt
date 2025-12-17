package com.fino.app.data.repository

import com.fino.app.data.local.dao.AchievementDao
import com.fino.app.data.local.entity.AchievementEntity
import com.fino.app.domain.model.Achievement
import com.fino.app.domain.model.AchievementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {
    /**
     * Get all achievements
     */
    suspend fun getAll(): List<Achievement> {
        return achievementDao.getAll().map { it.toDomain() }
    }

    /**
     * Get all achievements as a Flow for reactive updates
     */
    fun getAllFlow(): Flow<List<Achievement>> {
        return achievementDao.getAllFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get achievement by ID
     */
    suspend fun getById(id: String): Achievement? {
        return achievementDao.getById(id)?.toDomain()
    }

    /**
     * Get only unlocked achievements
     */
    suspend fun getUnlocked(): List<Achievement> {
        return achievementDao.getUnlocked().map { it.toDomain() }
    }

    /**
     * Get only locked achievements
     */
    suspend fun getLocked(): List<Achievement> {
        return achievementDao.getLocked().map { it.toDomain() }
    }

    /**
     * Unlock an achievement
     */
    suspend fun unlock(id: String) {
        achievementDao.unlock(id, System.currentTimeMillis())
    }

    /**
     * Update achievement progress
     */
    suspend fun updateProgress(id: String, progress: Int) {
        achievementDao.updateProgress(id, progress)
    }

    /**
     * Get count of unlocked achievements
     */
    suspend fun getUnlockedCount(): Int {
        return achievementDao.getUnlockedCount()
    }

    /**
     * Seed default achievements (called on first app launch)
     */
    suspend fun seedDefaultAchievements() {
        val defaultAchievements = listOf(
            // Streak achievements
            AchievementEntity(
                id = "streak_3",
                name = "Getting Started",
                description = "Maintain a 3-day logging streak",
                emoji = "🔥",
                xpReward = 50,
                requirement = 3,
                type = AchievementType.STREAK
            ),
            AchievementEntity(
                id = "streak_7",
                name = "Week Warrior",
                description = "Maintain a 7-day logging streak",
                emoji = "⚡",
                xpReward = 100,
                requirement = 7,
                type = AchievementType.STREAK
            ),
            AchievementEntity(
                id = "streak_30",
                name = "Monthly Master",
                description = "Maintain a 30-day logging streak",
                emoji = "🏆",
                xpReward = 300,
                requirement = 30,
                type = AchievementType.STREAK
            ),

            // Transaction count achievements
            AchievementEntity(
                id = "txn_10",
                name = "First Steps",
                description = "Log your first 10 transactions",
                emoji = "📝",
                xpReward = 25,
                requirement = 10,
                type = AchievementType.TRANSACTION_COUNT
            ),
            AchievementEntity(
                id = "txn_50",
                name = "Getting Active",
                description = "Log 50 transactions",
                emoji = "📊",
                xpReward = 75,
                requirement = 50,
                type = AchievementType.TRANSACTION_COUNT
            ),
            AchievementEntity(
                id = "txn_100",
                name = "Century Tracker",
                description = "Log 100 transactions",
                emoji = "💯",
                xpReward = 200,
                requirement = 100,
                type = AchievementType.TRANSACTION_COUNT
            ),

            // Budget achievements
            AchievementEntity(
                id = "budget_1",
                name = "Budget Beginner",
                description = "Create your first budget",
                emoji = "💰",
                xpReward = 50,
                requirement = 1,
                type = AchievementType.BUDGET
            ),

            // Credit card achievements
            AchievementEntity(
                id = "card_1",
                name = "Card Keeper",
                description = "Add your first credit card",
                emoji = "💳",
                xpReward = 30,
                requirement = 1,
                type = AchievementType.CREDIT_CARD
            )
        )

        achievementDao.insertAll(defaultAchievements)
    }

    // Mapping functions

    private fun AchievementEntity.toDomain(): Achievement {
        return Achievement(
            id = id,
            name = name,
            description = description,
            emoji = emoji,
            xpReward = xpReward,
            requirement = requirement,
            type = type,
            unlockedAt = unlockedAt?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            },
            progress = progress
        )
    }
}
