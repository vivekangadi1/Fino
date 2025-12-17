package com.fino.app.domain.model

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
    val reference: String? = null
)
