package com.fino.app.data.repository

import com.fino.app.data.local.dao.TransactionDao
import com.fino.app.data.local.entity.TransactionEntity
import com.fino.app.domain.model.PaymentStatus
import com.fino.app.domain.model.Transaction
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao
) {

    fun getAllTransactionsFlow(): Flow<List<Transaction>> {
        return dao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getAllTransactions(): List<Transaction> {
        return dao.getAll().map { it.toDomain() }
    }

    fun getUncategorizedFlow(): Flow<List<Transaction>> {
        return dao.getUncategorizedFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getUncategorized(): List<Transaction> {
        return dao.getUncategorized().map { it.toDomain() }
    }

    suspend fun getByCategory(categoryId: Long): List<Transaction> {
        return dao.getByCategory(categoryId).map { it.toDomain() }
    }

    fun getByCategoryFlow(categoryId: Long): Flow<List<Transaction>> {
        return dao.getByCategoryFlow(categoryId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getById(id: Long): Transaction? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun insert(transaction: Transaction): Long {
        return dao.insert(transaction.toEntity())
    }

    suspend fun update(transaction: Transaction) {
        dao.update(transaction.toEntity())
    }

    suspend fun delete(transaction: Transaction) {
        dao.delete(transaction.toEntity())
    }

    suspend fun getTransactionsForMonth(yearMonth: YearMonth): List<Transaction> {
        val startDate = DateUtils.getMonthStart(yearMonth)
        val endDate = DateUtils.getMonthEnd(yearMonth)
        return dao.getTransactionsForPeriod(startDate, endDate).map { it.toDomain() }
    }

    suspend fun getMonthlySpending(yearMonth: YearMonth = YearMonth.now()): Double {
        val startDate = DateUtils.getMonthStart(yearMonth)
        val endDate = DateUtils.getMonthEnd(yearMonth)
        return dao.getTotalSpendingForPeriod(startDate, endDate) ?: 0.0
    }

    suspend fun getSpendingForCategory(categoryId: Long, yearMonth: YearMonth): Double {
        val startDate = DateUtils.getMonthStart(yearMonth)
        val endDate = DateUtils.getMonthEnd(yearMonth)
        return dao.getSpendingForCategory(categoryId, startDate, endDate) ?: 0.0
    }

    suspend fun categorize(transactionId: Long, categoryId: Long, subcategoryId: Long? = null) {
        dao.categorize(transactionId, categoryId, subcategoryId)
    }

    suspend fun getTransactionCount(): Int {
        return dao.getTransactionCount()
    }

    suspend fun getByReference(reference: String): Transaction? {
        return dao.getByReference(reference)?.toDomain()
    }

    suspend fun existsByRawSmsBody(rawSmsBody: String): Boolean {
        return dao.existsByRawSmsBody(rawSmsBody)
    }

    suspend fun getTransactionsForEvent(eventId: Long): List<Transaction> {
        return dao.getTransactionsForEvent(eventId).map { it.toDomain() }
    }

    fun getTransactionsForEventFlow(eventId: Long): Flow<List<Transaction>> {
        return dao.getTransactionsForEventFlow(eventId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun assignToEvent(transactionId: Long, eventId: Long?) {
        dao.assignToEvent(transactionId, eventId)
    }

    suspend fun unlinkTransactionsFromEvent(eventId: Long) {
        dao.unlinkTransactionsFromEvent(eventId)
    }

    suspend fun getTotalSpendingForEvent(eventId: Long): Double {
        return dao.getTotalSpendingForEvent(eventId) ?: 0.0
    }

    // Event sub-category methods
    suspend fun getByEventSubCategory(subCategoryId: Long): List<Transaction> {
        return dao.getByEventSubCategory(subCategoryId).map { it.toDomain() }
    }

    fun getByEventSubCategoryFlow(subCategoryId: Long): Flow<List<Transaction>> {
        return dao.getByEventSubCategoryFlow(subCategoryId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getTotalSpendingForSubCategory(subCategoryId: Long): Double {
        return dao.getTotalSpendingForSubCategory(subCategoryId) ?: 0.0
    }

    suspend fun getPaidAmountForSubCategory(subCategoryId: Long): Double {
        return dao.getPaidAmountForSubCategory(subCategoryId) ?: 0.0
    }

    suspend fun getPendingAmountForSubCategory(subCategoryId: Long): Double {
        return dao.getPendingAmountForSubCategory(subCategoryId) ?: 0.0
    }

    // Event vendor methods
    suspend fun getByEventVendor(vendorId: Long): List<Transaction> {
        return dao.getByEventVendor(vendorId).map { it.toDomain() }
    }

    fun getByEventVendorFlow(vendorId: Long): Flow<List<Transaction>> {
        return dao.getByEventVendorFlow(vendorId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getTotalSpendingForVendor(vendorId: Long): Double {
        return dao.getTotalSpendingForVendor(vendorId) ?: 0.0
    }

    suspend fun getPaidAmountForVendor(vendorId: Long): Double {
        return dao.getPaidAmountForVendor(vendorId) ?: 0.0
    }

    suspend fun getPendingAmountForVendor(vendorId: Long): Double {
        return dao.getPendingAmountForVendor(vendorId) ?: 0.0
    }

    // Payment status methods
    suspend fun getByEventAndStatus(eventId: Long, status: PaymentStatus): List<Transaction> {
        return dao.getByEventAndStatus(eventId, status).map { it.toDomain() }
    }

    suspend fun getPendingPaymentsForEvent(eventId: Long): List<Transaction> {
        return dao.getPendingPaymentsForEvent(eventId).map { it.toDomain() }
    }

    fun getPendingPaymentsForEventFlow(eventId: Long): Flow<List<Transaction>> {
        return dao.getPendingPaymentsForEventFlow(eventId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getPaidAmountForEvent(eventId: Long): Double {
        return dao.getPaidAmountForEvent(eventId) ?: 0.0
    }

    suspend fun getPendingAmountForEvent(eventId: Long): Double {
        return dao.getPendingAmountForEvent(eventId) ?: 0.0
    }

    suspend fun updatePaymentStatus(transactionId: Long, status: PaymentStatus) {
        dao.updatePaymentStatus(transactionId, status)
    }

    suspend fun updateEventSubCategory(transactionId: Long, subCategoryId: Long?) {
        dao.updateEventSubCategory(transactionId, subCategoryId)
    }

    suspend fun updateEventVendor(transactionId: Long, vendorId: Long?) {
        dao.updateEventVendor(transactionId, vendorId)
    }

    // Payer methods
    suspend fun getByPayer(payer: String): List<Transaction> {
        return dao.getByPayer(payer).map { it.toDomain() }
    }

    suspend fun getTotalSpendingByPayer(payer: String): Double {
        return dao.getTotalSpendingByPayer(payer) ?: 0.0
    }

    suspend fun getEventSpendingByPayer(eventId: Long, payer: String): Double {
        return dao.getEventSpendingByPayer(eventId, payer) ?: 0.0
    }

    suspend fun getByMerchant(merchantName: String): List<Transaction> {
        return dao.getByMerchant("%$merchantName%").map { it.toDomain() }
    }

    /**
     * Categorize all transactions from a given merchant and return count updated
     */
    suspend fun categorizeByMerchant(merchantName: String, categoryId: Long): Int {
        return dao.categorizeByMerchant(merchantName, categoryId)
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            type = type,
            merchantName = merchantName,
            merchantNormalized = merchantNormalized,
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            creditCardId = creditCardId,
            isRecurring = isRecurring,
            recurringRuleId = recurringRuleId,
            rawSmsBody = rawSmsBody,
            smsSender = smsSender,
            parsedConfidence = parsedConfidence,
            needsReview = needsReview,
            transactionDate = DateUtils.fromEpochMillis(transactionDate),
            createdAt = DateUtils.fromEpochMillis(createdAt),
            source = source,
            reference = reference,
            bankName = bankName,
            paymentMethod = paymentMethod,
            cardLastFour = cardLastFour,
            eventId = eventId,
            eventSubCategoryId = eventSubCategoryId,
            eventVendorId = eventVendorId,
            paidBy = paidBy,
            isAdvancePayment = isAdvancePayment,
            dueDate = dueDate?.let { DateUtils.toLocalDate(it) },
            expenseNotes = expenseNotes,
            paymentStatus = paymentStatus
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            type = type,
            merchantName = merchantName,
            merchantNormalized = merchantNormalized,
            categoryId = categoryId,
            subcategoryId = subcategoryId,
            creditCardId = creditCardId,
            isRecurring = isRecurring,
            recurringRuleId = recurringRuleId,
            rawSmsBody = rawSmsBody,
            smsSender = smsSender,
            parsedConfidence = parsedConfidence,
            needsReview = needsReview,
            transactionDate = DateUtils.toEpochMillis(transactionDate),
            createdAt = DateUtils.toEpochMillis(createdAt),
            source = source,
            reference = reference,
            bankName = bankName,
            paymentMethod = paymentMethod,
            cardLastFour = cardLastFour,
            eventId = eventId,
            eventSubCategoryId = eventSubCategoryId,
            eventVendorId = eventVendorId,
            paidBy = paidBy,
            isAdvancePayment = isAdvancePayment,
            dueDate = dueDate?.let { DateUtils.toEpochMillis(it) },
            expenseNotes = expenseNotes,
            paymentStatus = paymentStatus
        )
    }
}
