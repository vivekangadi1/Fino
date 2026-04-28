package com.fino.app.data.repository

import com.fino.app.data.local.dao.BillDao
import com.fino.app.domain.model.Bill
import com.fino.app.domain.model.BillEntityStatus
import com.fino.app.domain.model.toDomain
import com.fino.app.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillRepository @Inject constructor(
    private val dao: BillDao
) {

    fun getAllFlow(): Flow<List<Bill>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    fun getUpcomingBillsFlow(withinDays: Int = 30): Flow<List<Bill>> {
        val now = System.currentTimeMillis()
        val cutoff = now + withinDays * 24L * 60L * 60L * 1000L
        return dao.getUpcomingFlow(now, cutoff).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getUpcomingBills(withinDays: Int = 30): List<Bill> {
        val now = System.currentTimeMillis()
        val cutoff = now + withinDays * 24L * 60L * 60L * 1000L
        return dao.getUpcoming(now, cutoff).map { it.toDomain() }
    }

    suspend fun getById(id: Long): Bill? = dao.getById(id)?.toDomain()

    suspend fun getByCreditCard(creditCardId: Long): List<Bill> =
        dao.getByCreditCard(creditCardId).map { it.toDomain() }

    suspend fun getByAccount(accountId: Long): List<Bill> =
        dao.getByAccount(accountId).map { it.toDomain() }

    suspend fun insert(bill: Bill): Long = dao.insert(bill.toEntity())

    suspend fun update(bill: Bill) = dao.update(bill.toEntity())

    suspend fun upsertForCycle(bill: Bill): Long {
        val existing = dao.findByAccountAndCycle(bill.accountId, bill.cycleEnd)
        return if (existing != null) {
            dao.update(bill.copy(id = existing.id).toEntity())
            existing.id
        } else {
            dao.insert(bill.toEntity())
        }
    }

    suspend fun markPaid(id: Long, paidAmount: Double, paidAt: Long = System.currentTimeMillis()) {
        val bill = dao.getById(id) ?: return
        val status = if (paidAmount >= bill.totalDue) BillEntityStatus.PAID else BillEntityStatus.PARTIAL
        dao.markPaid(id, paidAt, paidAmount, status, System.currentTimeMillis())
    }
}
