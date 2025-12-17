package com.fino.app.domain.model

import java.time.LocalDateTime

/**
 * Achievement definition and unlock status.
 *
 * @property id Unique string identifier (e.g., "streak_7", "txn_100")
 * @property name Display name of the achievement
 * @property description How to earn this achievement
 * @property emoji Emoji icon for the achievement
 * @property xpReward XP awarded when unlocked
 * @property requirement Numeric threshold to unlock (e.g., 7 for 7-day streak)
 * @property type Category of achievement
 * @property unlockedAt When the user earned this (null if locked)
 * @property progress Current progress toward the requirement
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val xpReward: Int,
    val requirement: Int,
    val type: AchievementType,
    val unlockedAt: LocalDateTime? = null,
    val progress: Int = 0
)

/**
 * Level definition for gamification.
 */
data class Level(
    val level: Int,
    val name: String,
    val minXp: Int,
    val maxXp: Int?
)

/**
 * Progress toward next level.
 */
data class LevelProgress(
    val currentLevel: Int,
    val nextLevel: Int?,
    val currentXp: Int,
    val xpForNextLevel: Int,
    val progressPercent: Float
)

/**
 * Weekly challenge definition.
 */
data class Challenge(
    val id: String,
    val description: String,
    val categoryId: Long? = null,
    val targetReduction: Float? = null,
    val targetAmount: Double? = null,
    val xpReward: Int
)
