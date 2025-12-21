package com.fino.app.data.repository

import com.fino.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for event expense analytics and reporting.
 * Aggregates data from multiple repositories to generate summaries and reports.
 */
@Singleton
class EventAnalyticsRepository @Inject constructor(
    private val eventRepository: EventRepository,
    private val eventTypeRepository: EventTypeRepository,
    private val eventSubCategoryRepository: EventSubCategoryRepository,
    private val eventVendorRepository: EventVendorRepository,
    private val transactionRepository: TransactionRepository
) {

    /**
     * Get sub-category summaries for an event
     */
    suspend fun getSubCategorySummaries(eventId: Long): List<EventSubCategorySummary> {
        val subCategories = eventSubCategoryRepository.getByEventId(eventId)

        return subCategories.map { subCategory ->
            val paidAmount = transactionRepository.getPaidAmountForSubCategory(subCategory.id)
            val pendingAmount = transactionRepository.getPendingAmountForSubCategory(subCategory.id)
            val transactionCount = transactionRepository.getByEventSubCategory(subCategory.id).size
            val vendorCount = eventVendorRepository.getCountBySubCategoryId(subCategory.id)
            val quotedAmount = eventVendorRepository.getTotalQuotedBySubCategoryId(subCategory.id)

            EventSubCategorySummary(
                subCategory = subCategory,
                budgetAmount = subCategory.budgetAmount ?: 0.0,
                quotedAmount = quotedAmount,
                paidAmount = paidAmount,
                pendingAmount = pendingAmount,
                transactionCount = transactionCount,
                vendorCount = vendorCount
            )
        }
    }

    /**
     * Get vendor summaries for an event
     */
    suspend fun getVendorSummaries(eventId: Long): List<EventVendorSummary> {
        val vendors = eventVendorRepository.getByEventId(eventId)
        val subCategories = eventSubCategoryRepository.getByEventId(eventId)
        val subCategoryMap = subCategories.associateBy { it.id }

        return vendors.map { vendor ->
            val payments = transactionRepository.getByEventVendor(vendor.id)
            val paidAmount = transactionRepository.getPaidAmountForVendor(vendor.id)
            val pendingAmount = transactionRepository.getPendingAmountForVendor(vendor.id)
            val subCategoryName = vendor.subCategoryId?.let { subCategoryMap[it]?.name }

            EventVendorSummary(
                vendor = vendor,
                subCategoryName = subCategoryName,
                quotedAmount = vendor.quotedAmount ?: 0.0,
                paidAmount = paidAmount,
                pendingAmount = pendingAmount,
                paymentCount = payments.size,
                payments = payments
            )
        }
    }

    /**
     * Get a comprehensive expense report for an event
     */
    suspend fun getEventExpenseReport(eventId: Long): EventExpenseReport? {
        val event = eventRepository.getById(eventId) ?: return null
        val eventType = eventTypeRepository.getById(event.eventTypeId)
        val eventTypeName = eventType?.name ?: "Unknown"

        val subCategorySummaries = getSubCategorySummaries(eventId)
        val vendorSummaries = getVendorSummaries(eventId)

        val totalBudget = event.budgetAmount ?: 0.0
        val totalQuoted = eventVendorRepository.getTotalQuotedByEventId(eventId)
        val totalPaid = transactionRepository.getPaidAmountForEvent(eventId)
        val totalPending = transactionRepository.getPendingAmountForEvent(eventId)

        val pendingPayments = transactionRepository.getPendingPaymentsForEvent(eventId)
        val recentTransactions = transactionRepository.getTransactionsForEvent(eventId).take(10)

        return EventExpenseReport(
            event = event,
            eventTypeName = eventTypeName,
            totalBudget = totalBudget,
            totalQuoted = totalQuoted,
            totalPaid = totalPaid,
            totalPending = totalPending,
            subCategorySummaries = subCategorySummaries,
            vendorSummaries = vendorSummaries,
            pendingPayments = pendingPayments,
            recentTransactions = recentTransactions
        )
    }

    /**
     * Get spending breakdown by payer for an event
     */
    suspend fun getPayerBreakdown(eventId: Long): Map<String, Double> {
        val transactions = transactionRepository.getTransactionsForEvent(eventId)
        return transactions
            .filter { it.paidBy != null }
            .groupBy { it.paidBy!! }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }

    /**
     * Get sub-categories over budget for an event
     */
    suspend fun getOverBudgetSubCategories(eventId: Long): List<EventSubCategorySummary> {
        return getSubCategorySummaries(eventId).filter { it.isOverBudget }
    }

    /**
     * Get vendors with pending payments for an event
     */
    suspend fun getVendorsWithPendingPayments(eventId: Long): List<EventVendorSummary> {
        return getVendorSummaries(eventId).filter { it.paymentStatus != PaymentStatus.PAID }
    }

    /**
     * Flow-based sub-category summaries for reactive UI
     */
    fun getSubCategorySummariesFlow(eventId: Long): Flow<List<EventSubCategorySummary>> {
        return combine(
            eventSubCategoryRepository.getByEventIdFlow(eventId),
            transactionRepository.getTransactionsForEventFlow(eventId),
            eventVendorRepository.getByEventIdFlow(eventId)
        ) { subCategories, transactions, vendors ->
            subCategories.map { subCategory ->
                val subCatTransactions = transactions.filter { it.eventSubCategoryId == subCategory.id }
                val paidAmount = subCatTransactions
                    .filter { it.paymentStatus == PaymentStatus.PAID }
                    .sumOf { it.amount }
                val pendingAmount = subCatTransactions
                    .filter { it.paymentStatus != PaymentStatus.PAID }
                    .sumOf { it.amount }
                val vendorCount = vendors.count { it.subCategoryId == subCategory.id }
                val quotedAmount = vendors
                    .filter { it.subCategoryId == subCategory.id }
                    .sumOf { it.quotedAmount ?: 0.0 }

                EventSubCategorySummary(
                    subCategory = subCategory,
                    budgetAmount = subCategory.budgetAmount ?: 0.0,
                    quotedAmount = quotedAmount,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount,
                    transactionCount = subCatTransactions.size,
                    vendorCount = vendorCount
                )
            }
        }
    }

    /**
     * Flow-based vendor summaries for reactive UI
     */
    fun getVendorSummariesFlow(eventId: Long): Flow<List<EventVendorSummary>> {
        return combine(
            eventVendorRepository.getByEventIdFlow(eventId),
            transactionRepository.getTransactionsForEventFlow(eventId),
            eventSubCategoryRepository.getByEventIdFlow(eventId)
        ) { vendors, transactions, subCategories ->
            val subCategoryMap = subCategories.associateBy { it.id }

            vendors.map { vendor ->
                val vendorTransactions = transactions.filter { it.eventVendorId == vendor.id }
                val paidAmount = vendorTransactions
                    .filter { it.paymentStatus == PaymentStatus.PAID }
                    .sumOf { it.amount }
                val pendingAmount = vendorTransactions
                    .filter { it.paymentStatus != PaymentStatus.PAID }
                    .sumOf { it.amount }
                val subCategoryName = vendor.subCategoryId?.let { subCategoryMap[it]?.name }

                EventVendorSummary(
                    vendor = vendor,
                    subCategoryName = subCategoryName,
                    quotedAmount = vendor.quotedAmount ?: 0.0,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount,
                    paymentCount = vendorTransactions.size,
                    payments = vendorTransactions
                )
            }
        }
    }
}
