package com.fino.app.data.repository

import com.fino.app.data.local.dao.EMIDao
import com.fino.app.data.local.entity.EMIEntity
import com.fino.app.domain.model.EMI
import com.fino.app.domain.model.EMIStatus
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EMIRepository @Inject constructor(
    private val dao: EMIDao
) {

    fun getActiveEMIsFlow(): Flow<List<EMI>> {
        return dao.getActiveEMIsFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getActiveEMIs(): List<EMI> {
        return dao.getActiveEMIs().map { it.toDomain() }
    }

    fun getAllEMIsFlow(): Flow<List<EMI>> {
        return dao.getAllEMIsFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getById(id: Long): EMI? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun getEMIsByCard(cardId: Long): List<EMI> {
        return dao.getEMIsByCard(cardId).map { it.toDomain() }
    }

    fun getActiveEMIsByCardFlow(cardId: Long): Flow<List<EMI>> {
        return dao.getActiveEMIsByCardFlow(cardId).map { list -> list.map { it.toDomain() } }
    }

    fun getCompletedEMIsFlow(): Flow<List<EMI>> {
        return dao.getCompletedEMIsFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun insert(emi: EMI): Long {
        return dao.insert(emi.toEntity())
    }

    suspend fun update(emi: EMI) {
        dao.update(emi.toEntity())
    }

    suspend fun delete(emi: EMI) {
        dao.delete(emi.toEntity())
    }

    suspend fun getTotalMonthlyEMI(): Double {
        return dao.getTotalMonthlyEMI() ?: 0.0
    }

    suspend fun getTotalMonthlyEMIByCard(cardId: Long): Double {
        return dao.getTotalMonthlyEMIByCard(cardId) ?: 0.0
    }

    suspend fun getActiveEMICount(): Int {
        return dao.getActiveEMICount()
    }

    suspend fun recordPayment(id: Long, nextDueDate: LocalDate) {
        dao.recordPayment(id, DateUtils.toEpochMillis(nextDueDate))
    }

    suspend fun markCompleted(id: Long) {
        dao.updateStatus(id, EMIStatus.COMPLETED)
    }

    suspend fun getUpcomingEMIs(startDate: LocalDate, endDate: LocalDate): List<EMI> {
        return dao.getUpcomingEMIs(
            DateUtils.toEpochMillis(startDate),
            DateUtils.toEpochMillis(endDate)
        ).map { it.toDomain() }
    }

    private fun EMIEntity.toDomain(): EMI {
        return EMI(
            id = id,
            creditCardId = creditCardId,
            description = description,
            merchantName = merchantName,
            originalAmount = originalAmount,
            monthlyAmount = monthlyAmount,
            tenure = tenure,
            paidCount = paidCount,
            startDate = DateUtils.toLocalDate(startDate),
            endDate = DateUtils.toLocalDate(endDate),
            nextDueDate = DateUtils.toLocalDate(nextDueDate),
            interestRate = interestRate,
            processingFee = processingFee,
            status = status,
            notes = notes,
            createdAt = DateUtils.fromEpochMillis(createdAt)
        )
    }

    private fun EMI.toEntity(): EMIEntity {
        return EMIEntity(
            id = id,
            creditCardId = creditCardId,
            description = description,
            merchantName = merchantName,
            originalAmount = originalAmount,
            monthlyAmount = monthlyAmount,
            tenure = tenure,
            paidCount = paidCount,
            startDate = DateUtils.toEpochMillis(startDate),
            endDate = DateUtils.toEpochMillis(endDate),
            nextDueDate = DateUtils.toEpochMillis(nextDueDate),
            interestRate = interestRate,
            processingFee = processingFee,
            status = status,
            notes = notes,
            createdAt = DateUtils.toEpochMillis(createdAt)
        )
    }
}
