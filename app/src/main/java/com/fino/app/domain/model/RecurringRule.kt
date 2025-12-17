package com.fino.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Rule for detecting and tracking recurring expenses (subscriptions, EMIs, etc.)
 *
 * @property id Unique identifier
 * @property merchantPattern Merchant name pattern to match (may contain wildcards)
 * @property categoryId Foreign key to the category
 * @property expectedAmount Expected transaction amount
 * @property amountVariance Acceptable variance as a fraction (0.1 = 10% variance allowed)
 * @property frequency How often this expense recurs (WEEKLY, MONTHLY, YEARLY)
 * @property dayOfPeriod Expected day (1-31 for monthly, 1-7 for weekly, 1-365 for yearly)
 * @property lastOccurrence Date of the last detected occurrence
 * @property nextExpected Predicted date for next occurrence
 * @property occurrenceCount How many times this recurring expense has occurred
 * @property isActive Whether this rule is still active
 * @property isUserConfirmed Whether the user has manually confirmed this rule
 * @property createdAt When this rule was created
 */
data class RecurringRule(
    val id: Long = 0,
    val merchantPattern: String,
    val categoryId: Long,
    val expectedAmount: Double,
    val amountVariance: Float = 0.1f,
    val frequency: RecurringFrequency,
    val dayOfPeriod: Int? = null,
    val lastOccurrence: LocalDate? = null,
    val nextExpected: LocalDate? = null,
    val occurrenceCount: Int = 0,
    val isActive: Boolean = true,
    val isUserConfirmed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
