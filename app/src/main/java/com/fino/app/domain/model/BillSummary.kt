package com.fino.app.domain.model

import java.time.YearMonth

/**
 * Summary of upcoming bills for display on home screen
 */
data class BillSummary(
    val thisMonth: MonthSummary,
    val nextMonth: MonthSummary,
    val overdueCount: Int,
    val dueTodayCount: Int
) {
    /**
     * Total amount due across both months
     */
    val totalAmount: Double
        get() = thisMonth.totalAmount + nextMonth.totalAmount

    /**
     * Total number of bills across both months
     */
    val totalBillCount: Int
        get() = thisMonth.billCount + nextMonth.billCount

    /**
     * Whether there are any urgent bills (overdue or due today)
     */
    val hasUrgentBills: Boolean
        get() = overdueCount > 0 || dueTodayCount > 0

    companion object {
        /**
         * Create an empty bill summary
         */
        fun empty(): BillSummary {
            val now = YearMonth.now()
            return BillSummary(
                thisMonth = MonthSummary(0.0, 0, now),
                nextMonth = MonthSummary(0.0, 0, now.plusMonths(1)),
                overdueCount = 0,
                dueTodayCount = 0
            )
        }
    }
}

/**
 * Summary for a specific month
 */
data class MonthSummary(
    val totalAmount: Double,
    val billCount: Int,
    val month: YearMonth
) {
    /**
     * Average bill amount for this month
     */
    val averageAmount: Double
        get() = if (billCount > 0) totalAmount / billCount else 0.0

    /**
     * Display string for the month (e.g., "December 2024")
     */
    val displayMonth: String
        get() = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}"
}
