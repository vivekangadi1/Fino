package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cashback_rewards",
    indices = [
        Index("period"),
        Index("accountId")
    ]
)
data class CashbackRewardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long? = null,
    val amount: Double,
    val period: String,
    val creditedAt: Long,
    val source: String,
    val description: String? = null
)
