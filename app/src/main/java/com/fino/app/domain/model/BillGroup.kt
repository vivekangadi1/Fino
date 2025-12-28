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
                BillStatus.PAID -> LATER_THIS_MONTH // Paid bills shown with later bills (usually filtered)
            }
        }
    }
}

/**
 * Bill category types for grouping by expense type
 */
enum class BillCategoryType(val displayLabel: String, val emoji: String, val sortOrder: Int) {
    SUBSCRIPTION("Subscriptions", "📱", 0),
    UTILITIES("Utilities", "💡", 1),
    INSURANCE("Insurance", "🛡️", 2),
    EMI("EMIs & Loans", "💳", 3),
    CREDIT_CARD("Credit Cards", "💳", 4),
    OTHER("Other Bills", "📄", 5);

    companion object {
        /**
         * Determine bill category based on merchant name patterns
         */
        fun fromMerchantName(merchantName: String): BillCategoryType {
            val name = merchantName.lowercase()
            return when {
                // Subscriptions
                listOf("netflix", "prime", "hotstar", "spotify", "youtube", "disney",
                    "zee5", "sony liv", "jio", "airtel xstream", "apple music",
                    "amazon prime", "hbo", "hulu").any { name.contains(it) } -> SUBSCRIPTION

                // Utilities
                listOf("electricity", "electric", "power", "water", "gas", "broadband",
                    "internet", "wifi", "phone bill", "mobile", "postpaid", "dth",
                    "tata sky", "dish tv", "airtel", "jio fiber", "act fibernet",
                    "bescom", "bses", "torrent power", "municipal", "sewage").any { name.contains(it) } -> UTILITIES

                // Insurance
                listOf("insurance", "lic", "icici prudential", "hdfc life", "max life",
                    "sbi life", "policy", "premium", "health insurance").any { name.contains(it) } -> INSURANCE

                // EMI
                listOf("emi", "loan", "bajaj finserv", "home credit", "zest money",
                    "simpl", "lazypay", "slice").any { name.contains(it) } -> EMI

                // Credit Card (detected by source)
                name.contains("credit card") -> CREDIT_CARD

                else -> OTHER
            }
        }
    }
}

/**
 * A bill group organized by category type within a time period
 */
data class CategoryBillGroup(
    val category: BillCategoryType,
    val bills: List<UpcomingBill>
) {
    val totalAmount: Double get() = bills.sumOf { it.amount }
    val billCount: Int get() = bills.size
}

/**
 * Enhanced bill group with category subgroups
 */
data class EnhancedBillGroup(
    val type: BillGroupType,
    val label: String,
    val categoryGroups: List<CategoryBillGroup>,
    val isExpanded: Boolean = true
) {
    val totalAmount: Double get() = categoryGroups.sumOf { it.totalAmount }
    val billCount: Int get() = categoryGroups.sumOf { it.billCount }
    val allBills: List<UpcomingBill> get() = categoryGroups.flatMap { it.bills }

    companion object {
        fun fromBillGroup(group: BillGroup, isExpanded: Boolean = true): EnhancedBillGroup {
            val categoryGroups = group.bills
                .groupBy { bill ->
                    if (bill.source == BillSource.CREDIT_CARD) {
                        BillCategoryType.CREDIT_CARD
                    } else {
                        BillCategoryType.fromMerchantName(bill.merchantName)
                    }
                }
                .map { (category, bills) ->
                    CategoryBillGroup(category, bills.sortedBy { it.dueDate })
                }
                .sortedBy { it.category.sortOrder }

            return EnhancedBillGroup(
                type = group.type,
                label = group.label,
                categoryGroups = categoryGroups,
                isExpanded = isExpanded
            )
        }
    }
}
