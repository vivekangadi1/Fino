package com.fino.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Credit card entity for tracking card transactions and bills.
 *
 * @property id Unique identifier
 * @property bankName Bank name (e.g., "HDFC", "ICICI", "SBI")
 * @property cardName Optional card variant name (e.g., "Regalia", "Amazon Pay")
 * @property lastFourDigits Last 4 digits of the card number for identification
 * @property creditLimit Optional credit limit of the card
 * @property billingCycleDay Day of month when bill is generated (1-31)
 * @property dueDateDay Day of month when payment is due (1-31)
 * @property currentUnbilled Current unbilled amount (transactions since last bill)
 * @property previousDue Amount due from the last statement
 * @property previousDueDate Due date for the previous bill payment
 * @property minimumDue Minimum payment due from last statement
 * @property isActive Whether this card is actively being tracked
 * @property createdAt When this card was added to the app
 */
data class CreditCard(
    val id: Long = 0,
    val bankName: String,
    val cardName: String? = null,
    val lastFourDigits: String,
    val creditLimit: Double? = null,
    val billingCycleDay: Int? = null,
    val dueDateDay: Int? = null,
    val currentUnbilled: Double = 0.0,
    val previousDue: Double = 0.0,
    val previousDueDate: LocalDate? = null,
    val minimumDue: Double? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Credit card bill information parsed from SMS.
 */
data class CreditCardBill(
    val cardLastFour: String,
    val bankName: String? = null,
    val totalDue: Double,
    val minimumDue: Double? = null,
    val dueDate: LocalDate
)
