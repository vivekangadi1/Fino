package com.fino.app.domain.model

import com.fino.app.data.local.entity.CashbackRewardEntity

data class CashbackReward(
    val id: Long = 0,
    val accountId: Long? = null,
    val amount: Double,
    val period: String,
    val creditedAt: Long,
    val source: String,
    val description: String? = null
)

fun CashbackRewardEntity.toDomain(): CashbackReward = CashbackReward(
    id = id,
    accountId = accountId,
    amount = amount,
    period = period,
    creditedAt = creditedAt,
    source = source,
    description = description
)

fun CashbackReward.toEntity(): CashbackRewardEntity = CashbackRewardEntity(
    id = id,
    accountId = accountId,
    amount = amount,
    period = period,
    creditedAt = creditedAt,
    source = source,
    description = description
)
