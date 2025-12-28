package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType
import java.time.LocalDateTime

/**
 * Payment channel/method used for the transaction.
 */
enum class PaymentChannel {
    UPI,
    CREDIT_CARD,
    DEBIT_CARD,
    PREPAID_CARD,
    FASTAG,
    AUTOPAY,
    STANDING_INSTRUCTION,
    NEFT,
    IMPS,
    INSURANCE,
    BANK_CHARGE,
    INVESTMENT,
    EMI,
    UNKNOWN
}

/**
 * Result of parsing an SMS message.
 * Contains all extracted transaction details.
 */
data class ParsedTransaction(
    val amount: Double,
    val type: TransactionType,
    val merchantName: String,
    val transactionDate: LocalDateTime,
    val reference: String? = null,
    val cardLastFour: String? = null,
    val bankName: String? = null,
    val accountLastFour: String? = null,
    val isLikelySubscription: Boolean = false,
    val confidence: Float = 0.5f,
    // New fields for enhanced parsing
    val currency: String = "INR",
    val paymentChannel: PaymentChannel = PaymentChannel.UNKNOWN,
    val senderName: String? = null,  // For CREDIT transactions
    val tollName: String? = null,    // For FASTag transactions
    val vehicleNumber: String? = null, // For FASTag transactions
    val isMandateRevocation: Boolean = false  // For AutoPay mandate revocations
)

/**
 * Result of parsing a credit card bill SMS.
 */
data class ParsedBill(
    val cardLastFour: String,
    val bankName: String? = null,
    val totalDue: Double,
    val minimumDue: Double? = null,
    val dueDate: java.time.LocalDate
)

/**
 * Pattern definition for SMS parsing.
 */
data class ParsePattern(
    val name: String,
    val regex: Regex,
    val confidence: Float = 0.9f,
    val isSubscription: Boolean = false
)
