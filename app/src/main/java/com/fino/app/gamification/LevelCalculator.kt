package com.fino.app.gamification

import com.fino.app.domain.model.Level
import com.fino.app.domain.model.LevelProgress

/**
 * Calculates user level based on XP.
 */
class LevelCalculator {

    companion object {
        val LEVELS = listOf(
            Level(level = 1, name = "Budding Saver", minXp = 0, maxXp = 99),
            Level(level = 2, name = "Money Tracker", minXp = 100, maxXp = 299),
            Level(level = 3, name = "Smart Spender", minXp = 300, maxXp = 599),
            Level(level = 4, name = "Budget Boss", minXp = 600, maxXp = 999),
            Level(level = 5, name = "Money Master", minXp = 1000, maxXp = 1499),
            Level(level = 6, name = "Finance Ninja", minXp = 1500, maxXp = 2199),
            Level(level = 7, name = "Wealth Wizard", minXp = 2200, maxXp = 2999),
            Level(level = 8, name = "Fino Legend", minXp = 3000, maxXp = null)  // Max level
        )

        const val MAX_LEVEL = 8
    }

    /**
     * Calculate level for given XP amount.
     */
    fun calculateLevel(xp: Int): Int {
        for (level in LEVELS.reversed()) {
            if (xp >= level.minXp) {
                return level.level
            }
        }
        return 1
    }

    /**
     * Get the level name for a given level number.
     */
    fun getLevelName(level: Int): String {
        return LEVELS.find { it.level == level }?.name ?: LEVELS.first().name
    }

    /**
     * Calculate progress toward the next level.
     */
    fun getProgressToNextLevel(xp: Int): LevelProgress {
        val currentLevel = calculateLevel(xp)
        val currentLevelDef = LEVELS.find { it.level == currentLevel }!!

        // At max level
        if (currentLevel == MAX_LEVEL) {
            return LevelProgress(
                currentLevel = currentLevel,
                nextLevel = null,
                currentXp = xp,
                xpForNextLevel = currentLevelDef.minXp,
                progressPercent = 1.0f
            )
        }

        val nextLevelDef = LEVELS.find { it.level == currentLevel + 1 }!!
        val xpInCurrentLevel = xp - currentLevelDef.minXp
        val xpNeededForNextLevel = nextLevelDef.minXp - currentLevelDef.minXp
        val progress = xpInCurrentLevel.toFloat() / xpNeededForNextLevel

        return LevelProgress(
            currentLevel = currentLevel,
            nextLevel = currentLevel + 1,
            currentXp = xp,
            xpForNextLevel = nextLevelDef.minXp,
            progressPercent = progress.coerceIn(0f, 1f)
        )
    }

    /**
     * Get XP required to reach a specific level.
     */
    fun getXpForLevel(level: Int): Int {
        return LEVELS.find { it.level == level }?.minXp ?: 0
    }

    /**
     * Check if leveling up occurred with the new XP.
     */
    fun didLevelUp(previousXp: Int, newXp: Int): Boolean {
        return calculateLevel(newXp) > calculateLevel(previousXp)
    }
}
