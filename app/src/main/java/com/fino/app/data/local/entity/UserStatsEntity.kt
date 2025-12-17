package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Long = 1,  // Singleton - always ID 1
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalTransactionsLogged: Int = 0,
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val lastActiveDate: Long? = null,  // Epoch millis (LocalDate)
    val createdAt: Long  // Epoch millis
)
