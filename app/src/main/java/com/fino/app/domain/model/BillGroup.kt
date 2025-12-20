package com.fino.app.domain.model

/**
 * A group of bills organized by time period for display
 */
data class BillGroup(
    val type: BillGroupType,
    val label: String,
    val bills: List<UpcomingBill>
) {
    /**
     * Total amount for all bills in this group
     */
    val totalAmount: Double
        get() = bills.sumOf { it.amount }

    /**
     * Number of bills in this group
     */
    val billCount: Int
        get() = bills.size

    /**
     * Whether this group is empty
     */
    val isEmpty: Boolean
        get() = bills.isEmpty()

    /**
     * Whether this group has any overdue bills
     */
    val hasOverdueBills: Boolean
        get() = bills.any { it.status == BillStatus.OVERDUE }

    companion object {
        /**
         * Create a BillGroup from type with auto-generated label
         */
        fun fromType(type: BillGroupType, bills: List<UpcomingBill>): BillGroup {
            return BillGroup(
                type = type,
                label = type.displayLabel,
                bills = bills
            )
        }
    }
}

/**
 * Types of bill groupings by time period
 */
enum class BillGroupType(val displayLabel: String, val sortOrder: Int) {
    TODAY("Today", 0),
    TOMORROW("Tomorrow", 1),
    THIS_WEEK("This Week", 2),
    LATER_THIS_MONTH("Later This Month", 3),
    NEXT_MONTH("Next Month", 4);

    companion object {
        /**
         * Get the appropriate group type for a bill based on its due date
         */
        fun fromBillStatus(status: BillStatus): BillGroupType {
            return when (status) {
                BillStatus.OVERDUE -> TODAY // Show overdue with today for urgency
                BillStatus.DUE_TODAY -> TODAY
                BillStatus.DUE_TOMORROW -> TOMORROW
                BillStatus.DUE_THIS_WEEK -> THIS_WEEK
                BillStatus.UPCOMING -> LATER_THIS_MONTH // Default, actual grouping done by date
            }
        }
    }
}
