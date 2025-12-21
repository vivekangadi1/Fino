package com.fino.app.domain.model

/**
 * Comprehensive expense report for an event.
 * Contains all summary data needed for the report screen.
 *
 * @property event The event this report is for
 * @property eventTypeName Display name of the event type
 * @property totalBudget Total budget for the event
 * @property totalQuoted Sum of all vendor quotes
 * @property totalPaid Total amount paid
 * @property totalPending Total amount pending
 * @property subCategorySummaries Breakdown by sub-category
 * @property vendorSummaries Breakdown by vendor
 * @property pendingPayments List of pending payment transactions
 * @property recentTransactions Most recent transactions for the event
 */
data class EventExpenseReport(
    val event: Event,
    val eventTypeName: String,

    // Budget Overview
    val totalBudget: Double,
    val totalQuoted: Double,
    val totalPaid: Double,
    val totalPending: Double,

    // Breakdowns
    val subCategorySummaries: List<EventSubCategorySummary>,
    val vendorSummaries: List<EventVendorSummary>,
    val pendingPayments: List<Transaction>,
    val recentTransactions: List<Transaction>
) {
    /**
     * Total amount (paid + pending)
     */
    val totalAmount: Double
        get() = totalPaid + totalPending

    /**
     * Budget deviation percentage
     * Positive = over budget, Negative = under budget
     */
    val budgetDeviation: Float
        get() = if (totalBudget > 0) (((totalPaid - totalBudget) / totalBudget) * 100).toFloat() else 0f

    /**
     * Whether the event is over budget
     */
    val isOverBudget: Boolean
        get() = totalBudget > 0 && totalPaid > totalBudget

    /**
     * Remaining budget
     */
    val remainingBudget: Double
        get() = totalBudget - totalPaid

    /**
     * Days elapsed since event started
     */
    val daysElapsed: Int
        get() = event.daysElapsed.toInt()

    /**
     * Days remaining until event ends (null if no end date)
     */
    val daysRemaining: Int?
        get() = event.daysRemaining?.toInt()

    /**
     * Daily average spending
     */
    val dailyAverage: Double
        get() = if (daysElapsed > 0) totalPaid / daysElapsed else totalPaid

    /**
     * Projected total based on current spending rate
     * Only meaningful if event has an end date
     */
    val projectedTotal: Double?
        get() {
            val remaining = daysRemaining ?: return null
            val totalDays = daysElapsed + remaining
            return if (totalDays > 0) dailyAverage * totalDays else null
        }

    /**
     * Number of sub-categories over budget
     */
    val overBudgetCategoryCount: Int
        get() = subCategorySummaries.count { it.isOverBudget }

    /**
     * Number of vendors with pending payments
     */
    val vendorsWithPendingPayments: Int
        get() = vendorSummaries.count { it.paymentStatus != PaymentStatus.PAID }

    /**
     * Total number of transactions
     */
    val totalTransactionCount: Int
        get() = subCategorySummaries.sumOf { it.transactionCount }

    /**
     * Total number of vendors
     */
    val totalVendorCount: Int
        get() = vendorSummaries.size
}
