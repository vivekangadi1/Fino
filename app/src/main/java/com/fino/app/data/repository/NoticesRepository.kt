package com.fino.app.data.repository

import com.fino.app.data.local.dao.NoticesDao
import com.fino.app.domain.model.Notice
import com.fino.app.domain.model.toDomain
import com.fino.app.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticesRepository @Inject constructor(
    private val dao: NoticesDao
) {

    fun getForPeriodFlow(period: String): Flow<List<Notice>> =
        dao.getByPeriodFlow(period).map { list -> list.map { it.toDomain() } }

    suspend fun getForPeriod(period: String): List<Notice> =
        dao.getByPeriod(period).map { it.toDomain() }

    suspend fun replaceForPeriod(period: String, notices: List<Notice>) {
        dao.replaceForPeriod(period, notices.map { it.toEntity() })
    }
}
