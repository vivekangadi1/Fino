package com.fino.app.data.repository

import com.fino.app.data.local.dao.CashbackRewardDao
import com.fino.app.domain.model.CashbackReward
import com.fino.app.domain.model.toDomain
import com.fino.app.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashbackRewardRepository @Inject constructor(
    private val dao: CashbackRewardDao
) {

    fun getAllFlow(): Flow<List<CashbackReward>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    fun getByPeriodFlow(period: String): Flow<List<CashbackReward>> =
        dao.getByPeriodFlow(period).map { list -> list.map { it.toDomain() } }

    suspend fun getByPeriod(period: String): List<CashbackReward> =
        dao.getByPeriod(period).map { it.toDomain() }

    suspend fun getTotalForPeriod(period: String): Double = dao.getTotalForPeriod(period)

    suspend fun insert(reward: CashbackReward): Long = dao.insert(reward.toEntity())

    suspend fun update(reward: CashbackReward) = dao.update(reward.toEntity())

    suspend fun delete(reward: CashbackReward) = dao.delete(reward.toEntity())
}
