package com.fino.app.data.repository

import com.fino.app.data.local.dao.TransactionDao
import com.fino.app.data.local.entity.TransactionEntity
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
            cardLastFour = cardLastFour
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
            cardLastFour = cardLastFour
        )
    }
}
