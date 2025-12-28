package com.fino.app.data.repository

import com.fino.app.data.local.dao.LoanDao
import com.fino.app.data.local.entity.LoanEntity
import com.fino.app.domain.model.Loan
import com.fino.app.domain.model.LoanStatus
import com.fino.app.domain.model.LoanType
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val dao: LoanDao
) {

    fun getActiveLoansFlow(): Flow<List<Loan>> {
        return dao.getActiveLoansFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getActiveLoans(): List<Loan> {
        return dao.getActiveLoans().map { it.toDomain() }
    }

    fun getAllLoansFlow(): Flow<List<Loan>> {
        return dao.getAllLoansFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getById(id: Long): Loan? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun getLoansByType(type: LoanType): List<Loan> {
        return dao.getLoansByType(type).map { it.toDomain() }
    }

    fun getCompletedLoansFlow(): Flow<List<Loan>> {
        return dao.getCompletedLoansFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun insert(loan: Loan): Long {
        return dao.insert(loan.toEntity())
    }

    suspend fun update(loan: Loan) {
        dao.update(loan.toEntity())
    }

    suspend fun delete(loan: Loan) {
        dao.delete(loan.toEntity())
    }

    suspend fun getTotalMonthlyLoanEMI(): Double {
        return dao.getTotalMonthlyLoanEMI() ?: 0.0
    }

    suspend fun getActiveLoanCount(): Int {
        return dao.getActiveLoanCount()
    }

    suspend fun recordPayment(id: Long, nextDueDate: LocalDate, outstandingPrincipal: Double?) {
        dao.recordPayment(id, DateUtils.toEpochMillis(nextDueDate), outstandingPrincipal)
    }

    suspend fun markCompleted(id: Long) {
        dao.updateStatus(id, LoanStatus.COMPLETED)
    }

    suspend fun closeLoan(id: Long) {
        dao.updateStatus(id, LoanStatus.CLOSED)
    }

    suspend fun getUpcomingLoanPayments(startDate: LocalDate, endDate: LocalDate): List<Loan> {
        return dao.getUpcomingLoanPayments(
            DateUtils.toEpochMillis(startDate),
            DateUtils.toEpochMillis(endDate)
        ).map { it.toDomain() }
    }

    private fun LoanEntity.toDomain(): Loan {
        return Loan(
            id = id,
            type = type,
            bankName = bankName,
            accountNumber = accountNumber,
            description = description,
            principalAmount = principalAmount,
            interestRate = interestRate,
            monthlyEMI = monthlyEMI,
            tenure = tenure,
            paidCount = paidCount,
            startDate = DateUtils.toLocalDate(startDate),
            endDate = DateUtils.toLocalDate(endDate),
            nextDueDate = DateUtils.toLocalDate(nextDueDate),
            outstandingPrincipal = outstandingPrincipal,
            status = status,
            notes = notes,
            createdAt = DateUtils.fromEpochMillis(createdAt)
        )
    }

    private fun Loan.toEntity(): LoanEntity {
        return LoanEntity(
            id = id,
            type = type,
            bankName = bankName,
            accountNumber = accountNumber,
            description = description,
            principalAmount = principalAmount,
            interestRate = interestRate,
            monthlyEMI = monthlyEMI,
            tenure = tenure,
            paidCount = paidCount,
            startDate = DateUtils.toEpochMillis(startDate),
            endDate = DateUtils.toEpochMillis(endDate),
            nextDueDate = DateUtils.toEpochMillis(nextDueDate),
            outstandingPrincipal = outstandingPrincipal,
            status = status,
            notes = notes,
            createdAt = DateUtils.toEpochMillis(createdAt)
        )
    }
}
