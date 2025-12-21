package com.fino.app.domain.model

/**
 * Summary of payments for an event vendor.
 * Tracks quoted vs paid with payment status.
 *
 * @property vendor The vendor this summary is for
 * @property subCategoryName Name of the sub-category (if assigned)
 * @property quotedAmount Amount quoted by the vendor
 * @property paidAmount Total amount paid to this vendor
 * @property pendingAmount Amount still pending (advance payments)
 * @property paymentCount Number of payments made
 * @property payments List of individual payment transactions
 */
data class EventVendorSummary(
    val vendor: EventVendor,
    val subCategoryName: String? = null,
    val quotedAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val paymentCount: Int,
    val payments: List<Transaction> = emptyList()
) {
    /**
     * Total amount (paid + pending)
     */
    val totalAmount: Double
        get() = paidAmount + pendingAmount

    /**
     * Whether the vendor is fully paid
     */
    val isPaidInFull: Boolean
        get() = quotedAmount > 0 && paidAmount >= quotedAmount

    /**
     * Balance remaining (quoted - paid)
     */
    val balanceRemaining: Double
        get() = if (quotedAmount > 0) quotedAmount - paidAmount else 0.0

    /**
     * Percentage of quote paid
     */
    val percentPaid: Float
        get() = if (quotedAmount > 0) ((paidAmount / quotedAmount) * 100).toFloat() else 100f

    /**
     * Payment status based on amounts
     */
    val paymentStatus: PaymentStatus
        get() = when {
            isPaidInFull -> PaymentStatus.PAID
            paidAmount > 0 -> PaymentStatus.PARTIAL
            else -> PaymentStatus.PENDING
        }
}
