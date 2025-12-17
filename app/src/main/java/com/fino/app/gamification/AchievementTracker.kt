package com.fino.app.gamification

import com.fino.app.data.local.dao.AchievementDao
import com.fino.app.data.local.dao.UserStatsDao
import com.fino.app.domain.model.Achievement
import com.fino.app.domain.model.AchievementType
import com.fino.app.util.DateUtils
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks and unlocks achievements based on user activity.
 */
@Singleton
class AchievementTracker @Inject constructor(
    private val achievementDao: AchievementDao,
    private val userStatsDao: UserStatsDao
) {

    companion object {
        val ALL_ACHIEVEMENTS = listOf(
            // Streak Achievements
            Achievement("streak_3", "Getting Started", "3-day categorization streak", "🔥", 25, 3, AchievementType.STREAK),
            Achievement("streak_7", "Week Warrior", "7-day categorization streak", "🔥", 50, 7, AchievementType.STREAK),
            Achievement("streak_14", "Fortnight Fighter", "14-day categorization streak", "🔥", 100, 14, AchievementType.STREAK),
            Achievement("streak_30", "Monthly Master", "30-day categorization streak", "🔥", 200, 30, AchievementType.STREAK),
            Achievement("streak_100", "Century Club", "100-day categorization streak", "💯", 500, 100, AchievementType.STREAK),
            Achievement("streak_365", "Year of Discipline", "365-day categorization streak", "🏆", 1000, 365, AchievementType.STREAK),

            // Transaction Count Achievements
            Achievement("txn_10", "First Steps", "Log 10 transactions", "📝", 20, 10, AchievementType.TRANSACTION_COUNT),
            Achievement("txn_50", "Getting Serious", "Log 50 transactions", "📝", 50, 50, AchievementType.TRANSACTION_COUNT),
            Achievement("txn_100", "Century Tracker", "Log 100 transactions", "💯", 100, 100, AchievementType.TRANSACTION_COUNT),
            Achievement("txn_500", "Expense Expert", "Log 500 transactions", "🏆", 300, 500, AchievementType.TRANSACTION_COUNT),
            Achievement("txn_1000", "Fino Veteran", "Log 1000 transactions", "⭐", 500, 1000, AchievementType.TRANSACTION_COUNT),

            // Budget Achievements
            Achievement("budget_first", "Budget Beginner", "Set your first budget", "💰", 30, 1, AchievementType.BUDGET),
            Achievement("budget_5", "Budget Planner", "Set 5 category budgets", "💰", 75, 5, AchievementType.BUDGET),
            Achievement("under_budget_1", "Under Control", "Stay under budget for 1 month", "✅", 100, 1, AchievementType.UNDER_BUDGET),
            Achievement("under_budget_3", "Budget Boss", "Stay under budget for 3 months", "👑", 300, 3, AchievementType.UNDER_BUDGET),

            // Credit Card Achievements
            Achievement("cc_first", "Card Keeper", "Add your first credit card", "💳", 50, 1, AchievementType.CREDIT_CARD),
            Achievement("cc_3", "Card Collector", "Track 3 credit cards", "💳", 100, 3, AchievementType.CREDIT_CARD),

            // Recurring Achievements
            Achievement("recurring_3", "Pattern Spotter", "Identify 3 recurring expenses", "🔄", 50, 3, AchievementType.RECURRING),
            Achievement("recurring_5", "Subscription Tracker", "Identify 5 recurring expenses", "🔄", 75, 5, AchievementType.RECURRING),
            Achievement("recurring_10", "Subscription Master", "Identify 10 recurring expenses", "🔄", 150, 10, AchievementType.RECURRING)
        )
    }

    /**
     * Check for newly unlocked achievements and unlock them.
     * Returns list of newly unlocked achievements.
     */
    suspend fun checkAndUnlock(): List<Achievement> {
        val stats = userStatsDao.getUserStats() ?: return emptyList()
        val unlockedAchievements = mutableListOf<Achievement>()

        // Check streak achievements
        for (achievement in ALL_ACHIEVEMENTS.filter { it.type == AchievementType.STREAK }) {
            if (!achievementDao.isUnlocked(achievement.id) && stats.currentStreak >= achievement.requirement) {
                unlockAchievement(achievement)
                unlockedAchievements.add(achievement)
            }
        }

        // Check transaction count achievements
        for (achievement in ALL_ACHIEVEMENTS.filter { it.type == AchievementType.TRANSACTION_COUNT }) {
            if (!achievementDao.isUnlocked(achievement.id) && stats.totalTransactionsLogged >= achievement.requirement) {
                unlockAchievement(achievement)
                unlockedAchievements.add(achievement)
            }
        }

        return unlockedAchievements
    }

    /**
     * Unlock a specific achievement.
     */
    private suspend fun unlockAchievement(achievement: Achievement) {
        val timestamp = DateUtils.toEpochMillis(LocalDateTime.now())
        achievementDao.unlock(achievement.id, timestamp)

        // Award XP
        val currentXp = userStatsDao.getTotalXp() ?: 0
        userStatsDao.addXp(achievement.xpReward)

        // Check for level up
        val levelCalculator = LevelCalculator()
        val newXp = currentXp + achievement.xpReward
        if (levelCalculator.didLevelUp(currentXp, newXp)) {
            userStatsDao.updateLevel(levelCalculator.calculateLevel(newXp))
        }
    }

    /**
     * Update progress for an achievement.
     */
    suspend fun updateProgress(achievementId: String, progress: Int) {
        achievementDao.updateProgress(achievementId, progress)
    }

    /**
     * Get all achievements with their current status.
     */
    suspend fun getAllAchievements(): List<Achievement> {
        return achievementDao.getAll().map { entity ->
            Achievement(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                emoji = entity.emoji,
                xpReward = entity.xpReward,
                requirement = entity.requirement,
                type = entity.type,
                unlockedAt = entity.unlockedAt?.let { DateUtils.fromEpochMillis(it) },
                progress = entity.progress
            )
        }
    }

    /**
     * Get unlocked achievement count.
     */
    suspend fun getUnlockedCount(): Int {
        return achievementDao.getUnlockedCount()
    }
}
