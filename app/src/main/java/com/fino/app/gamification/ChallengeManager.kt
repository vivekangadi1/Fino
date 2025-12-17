package com.fino.app.gamification

import com.fino.app.domain.model.Challenge
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages weekly challenges for users.
 */
@Singleton
class ChallengeManager @Inject constructor() {

    companion object {
        val AVAILABLE_CHALLENGES = listOf(
            Challenge("spend_less_food", "Spend 10% less on Food this week", categoryId = 1, targetReduction = 0.10f, xpReward = 100),
            Challenge("categorize_all", "Categorize all transactions within 24 hours", xpReward = 75),
            Challenge("no_food_delivery", "No food delivery for a week", categoryId = 5, targetAmount = 0.0, xpReward = 100),
            Challenge("track_every_day", "Log at least 1 transaction every day this week", xpReward = 50),
            Challenge("under_budget_week", "Stay under budget in all categories this week", xpReward = 100),
            Challenge("reduce_subscriptions", "Review and cancel unused subscriptions", xpReward = 75)
        )
    }

    /**
     * Get a random challenge for the current week.
     */
    fun getWeeklyChallenge(): Challenge {
        val weekNumber = LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfYear())
        val index = weekNumber % AVAILABLE_CHALLENGES.size
        return AVAILABLE_CHALLENGES[index]
    }

    /**
     * Get the start of the current week.
     */
    fun getWeekStart(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    /**
     * Get the end of the current week.
     */
    fun getWeekEnd(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }

    /**
     * Check if challenge is completed.
     * Implementation depends on challenge type.
     */
    fun isChallengeCompleted(challenge: Challenge): Boolean {
        // This would need to check actual data
        // Placeholder implementation
        return false
    }
}
