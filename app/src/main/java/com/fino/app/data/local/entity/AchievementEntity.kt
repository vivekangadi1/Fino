package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fino.app.domain.model.AchievementType

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,  // e.g., "streak_7", "txn_100"
    val name: String,
    val description: String,
    val emoji: String,
    val xpReward: Int,
    val requirement: Int,
    val type: AchievementType,
    val unlockedAt: Long? = null,  // Epoch millis (null if locked)
    val progress: Int = 0
)
