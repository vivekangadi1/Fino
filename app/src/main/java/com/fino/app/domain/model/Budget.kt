package com.fino.app.domain.model

import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Monthly budget limit for a category.
 *
 * @property id Unique identifier
 * @property categoryId Foreign key to the category this budget applies to
 * @property monthlyLimit Budget limit amount in INR
 * @property month The month and year this budget applies to
 * @property alertAt75 Whether to alert when 75% of budget is spent
 * @property alertAt100 Whether to alert when 100% of budget is spent
 * @property createdAt When this budget was created
 */
data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val month: YearMonth,
    val alertAt75: Boolean = true,
    val alertAt100: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Calculated budget progress for display.
 */
data class BudgetProgress(
    val budget: Budget,
    val categoryName: String,
    val spent: Double,
    val remaining: Double,
    val percentage: Float,
    val status: BudgetStatus
)

/**
 * Overall budget health calculation.
 */
data class BudgetHealth(
    val totalBudget: Double,
    val spent: Double,
    val upcomingBills: Double,
    val remaining: Double,
    val status: BudgetStatus
)
