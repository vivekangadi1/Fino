package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE needsReview = 1 ORDER BY transactionDate DESC")
    fun getUncategorizedFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE needsReview = 1 ORDER BY transactionDate DESC")
    suspend fun getUncategorized(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE transactionDate BETWEEN :startDate AND :endDate ORDER BY transactionDate DESC")
    suspend fun getTransactionsForPeriod(startDate: Long, endDate: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY transactionDate DESC")
    suspend fun getByCategory(categoryId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE creditCardId = :creditCardId ORDER BY transactionDate DESC")
    suspend fun getByCreditCard(creditCardId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE merchantName LIKE :merchantPattern ORDER BY transactionDate DESC")
    suspend fun getByMerchant(merchantPattern: String): List<TransactionEntity>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND transactionDate BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpendingForPeriod(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND categoryId = :categoryId AND transactionDate BETWEEN :startDate AND :endDate")
    suspend fun getSpendingForCategory(categoryId: Long, startDate: Long, endDate: Long): Double?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun getCountByCategory(categoryId: Long): Int

    @Query("UPDATE transactions SET categoryId = :categoryId, subcategoryId = :subcategoryId, needsReview = 0 WHERE id = :transactionId")
    suspend fun categorize(transactionId: Long, categoryId: Long, subcategoryId: Long?)

    @Query("SELECT * FROM transactions WHERE reference = :reference LIMIT 1")
    suspend fun getByReference(reference: String): TransactionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE rawSmsBody = :rawSmsBody LIMIT 1)")
    suspend fun existsByRawSmsBody(rawSmsBody: String): Boolean

    @Query("SELECT * FROM transactions WHERE eventId = :eventId ORDER BY transactionDate DESC")
    suspend fun getTransactionsForEvent(eventId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE eventId = :eventId ORDER BY transactionDate DESC")
    fun getTransactionsForEventFlow(eventId: Long): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET eventId = :eventId WHERE id = :transactionId")
    suspend fun assignToEvent(transactionId: Long, eventId: Long?)

    @Query("UPDATE transactions SET eventId = NULL WHERE eventId = :eventId")
    suspend fun unlinkTransactionsFromEvent(eventId: Long)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventId = :eventId")
    suspend fun getTotalSpendingForEvent(eventId: Long): Double?
}
