package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType
import java.time.LocalDateTime

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
    val confidence: Float = 0.5f
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
