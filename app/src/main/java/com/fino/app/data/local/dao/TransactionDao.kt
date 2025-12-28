package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.TransactionEntity
import com.fino.app.domain.model.PaymentStatus
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

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY transactionDate DESC")
    fun getByCategoryFlow(categoryId: Long): Flow<List<TransactionEntity>>

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

    // Event sub-category queries
    @Query("SELECT * FROM transactions WHERE eventSubCategoryId = :subCategoryId ORDER BY transactionDate DESC")
    suspend fun getByEventSubCategory(subCategoryId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE eventSubCategoryId = :subCategoryId ORDER BY transactionDate DESC")
    fun getByEventSubCategoryFlow(subCategoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventSubCategoryId = :subCategoryId")
    suspend fun getTotalSpendingForSubCategory(subCategoryId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventSubCategoryId = :subCategoryId AND paymentStatus = 'PAID'")
    suspend fun getPaidAmountForSubCategory(subCategoryId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventSubCategoryId = :subCategoryId AND paymentStatus != 'PAID'")
    suspend fun getPendingAmountForSubCategory(subCategoryId: Long): Double?

    @Query("SELECT COUNT(*) FROM transactions WHERE eventSubCategoryId = :subCategoryId")
    suspend fun getCountBySubCategory(subCategoryId: Long): Int

    // Event vendor queries
    @Query("SELECT * FROM transactions WHERE eventVendorId = :vendorId ORDER BY transactionDate DESC")
    suspend fun getByEventVendor(vendorId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE eventVendorId = :vendorId ORDER BY transactionDate DESC")
    fun getByEventVendorFlow(vendorId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventVendorId = :vendorId")
    suspend fun getTotalSpendingForVendor(vendorId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventVendorId = :vendorId AND paymentStatus = 'PAID'")
    suspend fun getPaidAmountForVendor(vendorId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventVendorId = :vendorId AND paymentStatus != 'PAID'")
    suspend fun getPendingAmountForVendor(vendorId: Long): Double?

    @Query("SELECT COUNT(*) FROM transactions WHERE eventVendorId = :vendorId")
    suspend fun getCountByVendor(vendorId: Long): Int

    // Payment status queries
    @Query("SELECT * FROM transactions WHERE eventId = :eventId AND paymentStatus = :status ORDER BY transactionDate DESC")
    suspend fun getByEventAndStatus(eventId: Long, status: PaymentStatus): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE eventId = :eventId AND paymentStatus != 'PAID' ORDER BY dueDate ASC, transactionDate DESC")
    suspend fun getPendingPaymentsForEvent(eventId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE eventId = :eventId AND paymentStatus != 'PAID' ORDER BY dueDate ASC")
    fun getPendingPaymentsForEventFlow(eventId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventId = :eventId AND paymentStatus = 'PAID'")
    suspend fun getPaidAmountForEvent(eventId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventId = :eventId AND paymentStatus != 'PAID'")
    suspend fun getPendingAmountForEvent(eventId: Long): Double?

    @Query("UPDATE transactions SET paymentStatus = :status WHERE id = :transactionId")
    suspend fun updatePaymentStatus(transactionId: Long, status: PaymentStatus)

    @Query("UPDATE transactions SET eventSubCategoryId = :subCategoryId WHERE id = :transactionId")
    suspend fun updateEventSubCategory(transactionId: Long, subCategoryId: Long?)

    @Query("UPDATE transactions SET eventVendorId = :vendorId WHERE id = :transactionId")
    suspend fun updateEventVendor(transactionId: Long, vendorId: Long?)

    // Payer queries
    @Query("SELECT * FROM transactions WHERE paidBy = :payer ORDER BY transactionDate DESC")
    suspend fun getByPayer(payer: String): List<TransactionEntity>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND paidBy = :payer")
    suspend fun getTotalSpendingByPayer(payer: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT' AND eventId = :eventId AND paidBy = :payer")
    suspend fun getEventSpendingByPayer(eventId: Long, payer: String): Double?

    // Bulk categorize by merchant name
    @Query("UPDATE transactions SET categoryId = :categoryId, needsReview = 0 WHERE LOWER(merchantName) = LOWER(:merchantName)")
    suspend fun categorizeByMerchant(merchantName: String, categoryId: Long): Int
}
