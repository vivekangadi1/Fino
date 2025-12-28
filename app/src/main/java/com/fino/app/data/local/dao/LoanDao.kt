package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.LoanEntity
import com.fino.app.domain.model.LoanStatus
import com.fino.app.domain.model.LoanType
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: LoanEntity): Long

    @Update
    suspend fun update(loan: LoanEntity)

    @Delete
    suspend fun delete(loan: LoanEntity)

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getById(id: Long): LoanEntity?

    @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY nextDueDate ASC")
    fun getActiveLoansFlow(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY nextDueDate ASC")
    suspend fun getActiveLoans(): List<LoanEntity>

    @Query("SELECT * FROM loans ORDER BY createdAt DESC")
    fun getAllLoansFlow(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE type = :type ORDER BY nextDueDate ASC")
    suspend fun getLoansByType(type: LoanType): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE status = 'COMPLETED' OR status = 'CLOSED' ORDER BY endDate DESC")
    fun getCompletedLoansFlow(): Flow<List<LoanEntity>>

    @Query("SELECT SUM(monthlyEMI) FROM loans WHERE status = 'ACTIVE'")
    suspend fun getTotalMonthlyLoanEMI(): Double?

    @Query("SELECT COUNT(*) FROM loans WHERE status = 'ACTIVE'")
    suspend fun getActiveLoanCount(): Int

    @Query("UPDATE loans SET paidCount = paidCount + 1, nextDueDate = :nextDueDate, outstandingPrincipal = :outstandingPrincipal WHERE id = :id")
    suspend fun recordPayment(id: Long, nextDueDate: Long, outstandingPrincipal: Double?)

    @Query("UPDATE loans SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: LoanStatus)

    @Query("SELECT * FROM loans WHERE nextDueDate BETWEEN :startDate AND :endDate AND status = 'ACTIVE'")
    suspend fun getUpcomingLoanPayments(startDate: Long, endDate: Long): List<LoanEntity>
}
