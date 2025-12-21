package com.fino.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Core transaction record representing a financial transaction.
 *
 * @property id Unique identifier
 * @property amount Transaction amount in INR
 * @property type Whether money was spent (DEBIT) or received (CREDIT)
 * @property merchantName Raw merchant name as extracted from SMS
 * @property merchantNormalized Cleaned/learned display name for the merchant
 * @property categoryId Foreign key to Category
 * @property subcategoryId Foreign key to subcategory (Category with parent)
 * @property creditCardId Foreign key to CreditCard if this was a CC transaction
 * @property isRecurring Whether this is flagged as a recurring transaction
 * @property recurringRuleId Foreign key to RecurringRule if applicable
 * @property rawSmsBody Original SMS text for reference
 * @property smsSender SMS sender address (e.g., "HDFCBK", "ICICIB")
 * @property parsedConfidence Parser confidence score 0.0-1.0
 * @property needsReview Whether this transaction needs user categorization
 * @property transactionDate When the transaction actually occurred
 * @property createdAt When this record was created in the database
 * @property source How this transaction was recorded
 * @property reference Transaction reference number (UPI ref, etc.)
 * @property bankName Bank name extracted from SMS (e.g., "HDFC", "ICICI", "SBI", "AXIS")
 * @property paymentMethod Payment method type ("UPI", "CREDIT_CARD")
 * @property cardLastFour Last 4 digits of card number for credit card transactions
 * @property eventId Foreign key to Event if this transaction is associated with an event
 * @property eventSubCategoryId Foreign key to EventSubCategory for event expense tracking
 * @property eventVendorId Foreign key to EventVendor for event expense tracking
 * @property paidBy Who paid for this expense (e.g., "Self", "Father")
 * @property isAdvancePayment Whether this is an advance/partial payment
 * @property dueDate Due date for pending payments
 * @property expenseNotes Additional notes about the expense
 * @property paymentStatus Payment status (PAID, PENDING, PARTIAL, OVERDUE)
 */
data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val merchantName: String,
    val merchantNormalized: String? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val creditCardId: Long? = null,
    val isRecurring: Boolean = false,
    val recurringRuleId: Long? = null,
    val rawSmsBody: String? = null,
    val smsSender: String? = null,
    val parsedConfidence: Float = 0f,
    val needsReview: Boolean = true,
    val transactionDate: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val source: TransactionSource = TransactionSource.SMS,
    val reference: String? = null,
    val bankName: String? = null,
    val paymentMethod: String? = null,
    val cardLastFour: String? = null,
    val eventId: Long? = null,

    // Event expense tracking fields
    val eventSubCategoryId: Long? = null,
    val eventVendorId: Long? = null,
    val paidBy: String? = null,
    val isAdvancePayment: Boolean = false,
    val dueDate: LocalDate? = null,
    val expenseNotes: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID
) {
    /**
     * Whether this transaction is associated with an event
     */
    val isEventExpense: Boolean
        get() = eventId != null

    /**
     * Whether this transaction is fully paid
     */
    val isFullyPaid: Boolean
        get() = paymentStatus == PaymentStatus.PAID

    /**
     * Whether this transaction has a pending balance
     */
    val hasPendingBalance: Boolean
        get() = paymentStatus == PaymentStatus.PENDING || paymentStatus == PaymentStatus.PARTIAL

    /**
     * Whether this transaction is overdue
     */
    val isOverdue: Boolean
        get() = paymentStatus == PaymentStatus.OVERDUE ||
                (hasPendingBalance && dueDate != null && dueDate.isBefore(LocalDate.now()))
}
