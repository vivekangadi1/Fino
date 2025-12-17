package com.fino.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * User statistics for gamification tracking.
 * This is a single-row table (always id=1).
 *
 * @property id Always 1 (singleton)
 * @property currentStreak Current consecutive days with categorization activity
 * @property longestStreak Best streak ever achieved
 * @property totalTransactionsLogged Lifetime count of transactions logged
 * @property totalXp Total experience points earned
 * @property currentLevel Current level (1-8)
 * @property lastActiveDate Last date the user categorized a transaction
 * @property createdAt When the user started using the app
 */
data class UserStats(
    val id: Long = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalTransactionsLogged: Int = 0,
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val lastActiveDate: LocalDate? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
