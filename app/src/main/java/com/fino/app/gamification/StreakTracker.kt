package com.fino.app.gamification

import com.fino.app.data.local.dao.UserStatsDao
import com.fino.app.util.DateUtils
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks user streaks for consecutive days of activity.
 */
@Singleton
class StreakTracker @Inject constructor(
    private val userStatsDao: UserStatsDao
) {

    /**
     * Record activity for a given date and update streak.
     * Returns the new streak count.
     */
    suspend fun recordActivity(date: LocalDate): Int {
        val stats = userStatsDao.getUserStats() ?: return 1

        val lastActiveDate = stats.lastActiveDate?.let { DateUtils.toLocalDate(it) }

        val newStreak = when {
            // First ever activity
            lastActiveDate == null -> 1

            // Same day - no change
            lastActiveDate == date -> stats.currentStreak

            // Consecutive day - increase streak
            lastActiveDate == date.minusDays(1) -> stats.currentStreak + 1

            // Missed days - reset streak
            else -> 1
        }

        val newLongestStreak = maxOf(newStreak, stats.longestStreak)

        userStatsDao.updateStreak(
            streak = newStreak,
            longestStreak = newLongestStreak,
            lastActiveDate = DateUtils.toEpochMillis(date)
        )

        return newStreak
    }

    /**
     * Get the current streak.
     */
    suspend fun getCurrentStreak(): Int {
        return userStatsDao.getUserStats()?.currentStreak ?: 0
    }

    /**
     * Get the longest streak ever.
     */
    suspend fun getLongestStreak(): Int {
        return userStatsDao.getUserStats()?.longestStreak ?: 0
    }

    /**
     * Check if the user has an active streak today.
     */
    suspend fun hasActiveStreakToday(): Boolean {
        val stats = userStatsDao.getUserStats() ?: return false
        val lastActiveDate = stats.lastActiveDate?.let { DateUtils.toLocalDate(it) }
        return lastActiveDate == LocalDate.now()
    }

    /**
     * Check if the streak is at risk (no activity yesterday).
     */
    suspend fun isStreakAtRisk(): Boolean {
        val stats = userStatsDao.getUserStats() ?: return false
        if (stats.currentStreak == 0) return false

        val lastActiveDate = stats.lastActiveDate?.let { DateUtils.toLocalDate(it) }
        val today = LocalDate.now()

        return lastActiveDate != null &&
               lastActiveDate != today &&
               lastActiveDate == today.minusDays(1)
    }
}
