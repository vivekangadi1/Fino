package com.fino.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Summary view of an event with spending totals
 */
data class EventSummary(
    val event: Event,
    val eventTypeName: String,
    val totalSpent: Double,
    val transactionCount: Int,
    val budgetStatus: EventBudgetStatus?
) {
    /**
     * Budget utilization percentage (0-100+)
     */
    val budgetPercentage: Float?
        get() = if (event.hasBudget && event.budgetAmount != null) {
            ((totalSpent / event.budgetAmount) * 100).toFloat()
        } else null

    /**
     * Remaining budget amount
     */
    val remainingBudget: Double?
        get() = event.budgetAmount?.let { it - totalSpent }
}

/**
 * Budget status for an event with projection support
 */
data class EventBudgetStatus(
    val budgetAmount: Double,
    val spent: Double,
    val remaining: Double,
    val percentageUsed: Float,
    val projectedTotal: Double,
    val projectedOverBudget: Boolean,
    val daysElapsed: Int,
    val daysRemaining: Int?,
    val dailyAverage: Double,
    val alertLevel: BudgetAlertLevel
) {
    companion object {
        /**
         * Calculate budget status from event and spending data
         */
        fun calculate(
            event: Event,
            totalSpent: Double
        ): EventBudgetStatus? {
            val budget = event.budgetAmount ?: return null

            val today = LocalDate.now()
            val daysElapsed = ChronoUnit.DAYS.between(event.startDate, today).toInt().coerceAtLeast(1)
            val daysRemaining = event.endDate?.let {
                ChronoUnit.DAYS.between(today, it).toInt().coerceAtLeast(0)
            }

            val dailyAverage = totalSpent / daysElapsed
            val projectedTotal = if (daysRemaining != null) {
                totalSpent + (dailyAverage * daysRemaining)
            } else {
                totalSpent
            }

            val percentageUsed = ((totalSpent / budget) * 100).toFloat()
            val alertLevel = when {
                percentageUsed >= 100 -> BudgetAlertLevel.EXCEEDED
                percentageUsed >= 75 -> BudgetAlertLevel.WARNING
                else -> BudgetAlertLevel.NORMAL
            }

            return EventBudgetStatus(
                budgetAmount = budget,
                spent = totalSpent,
                remaining = budget - totalSpent,
                percentageUsed = percentageUsed,
                projectedTotal = projectedTotal,
                projectedOverBudget = projectedTotal > budget,
                daysElapsed = daysElapsed,
                daysRemaining = daysRemaining,
                dailyAverage = dailyAverage,
                alertLevel = alertLevel
            )
        }
    }
}
