package com.fino.app.domain.model

/**
 * Summary of spending for an event sub-category.
 * Used for displaying budget vs actual with deviation.
 *
 * @property subCategory The sub-category this summary is for
 * @property budgetAmount Budget allocated for this sub-category
 * @property quotedAmount Sum of vendor quotes in this sub-category
 * @property paidAmount Sum of PAID transactions
 * @property pendingAmount Sum of PENDING/PARTIAL transactions
 * @property transactionCount Number of transactions in this sub-category
 * @property vendorCount Number of vendors in this sub-category
 */
data class EventSubCategorySummary(
    val subCategory: EventSubCategory,
    val budgetAmount: Double,
    val quotedAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val transactionCount: Int,
    val vendorCount: Int
) {
    /**
     * Total amount (paid + pending)
     */
    val totalAmount: Double
        get() = paidAmount + pendingAmount

    /**
     * Percentage of budget used (based on paid amount)
     */
    val percentUsed: Float
        get() = if (budgetAmount > 0) ((paidAmount / budgetAmount) * 100).toFloat() else 0f

    /**
     * Percentage deviation from budget
     * Positive = over budget, Negative = under budget
     */
    val percentDeviation: Float
        get() = if (budgetAmount > 0) (((paidAmount - budgetAmount) / budgetAmount) * 100).toFloat() else 0f

    /**
     * Whether this sub-category is over budget
     */
    val isOverBudget: Boolean
        get() = budgetAmount > 0 && paidAmount > budgetAmount

    /**
     * Whether this sub-category is approaching budget (75%+)
     */
    val isApproachingBudget: Boolean
        get() = budgetAmount > 0 && percentUsed >= 75f && percentUsed < 100f

    /**
     * Remaining budget (negative if over)
     */
    val remainingBudget: Double
        get() = budgetAmount - paidAmount
}
