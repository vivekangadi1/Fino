package com.fino.app.domain.model

import java.time.YearMonth

/**
 * Data for a single period in comparison
 */
data class PeriodData(
    val yearMonth: YearMonth,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netBalance: Double,
    val transactionCount: Int,
    val categoryBreakdown: List<CategorySpending>
)

/**
 * Category spending data for comparison
 */
data class CategorySpending(
    val categoryName: String,
    val categoryId: Long?,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

/**
 * Comparison between two periods
 */
data class PeriodComparison(
    val currentPeriod: PeriodData,
    val previousPeriod: PeriodData,
    val incomeChange: Double,
    val incomeChangePercentage: Float,
    val expensesChange: Double,
    val expensesChangePercentage: Float,
    val netBalanceChange: Double,
    val categoryComparisons: List<CategoryComparison>
)

/**
 * Comparison for a specific category
 */
data class CategoryComparison(
    val categoryName: String,
    val categoryId: Long?,
    val currentAmount: Double,
    val previousAmount: Double,
    val change: Double,
    val changePercentage: Float,
    val trend: TrendDirection
)

/**
 * Calculate period comparison from two period data
 */
fun calculatePeriodComparison(
    current: PeriodData,
    previous: PeriodData
): PeriodComparison {
    val incomeChange = current.totalIncome - previous.totalIncome
    val incomeChangePercentage = calculatePercentageChange(
        current.totalIncome,
        previous.totalIncome
    )

    val expensesChange = current.totalExpenses - previous.totalExpenses
    val expensesChangePercentage = calculatePercentageChange(
        current.totalExpenses,
        previous.totalExpenses
    )

    val netBalanceChange = current.netBalance - previous.netBalance

    // Compare categories
    val categoryComparisons = mutableListOf<CategoryComparison>()
    val allCategories = (current.categoryBreakdown + previous.categoryBreakdown)
        .distinctBy { it.categoryId }

    allCategories.forEach { category ->
        val currentCat = current.categoryBreakdown.find { it.categoryId == category.categoryId }
        val previousCat = previous.categoryBreakdown.find { it.categoryId == category.categoryId }

        val currentAmount = currentCat?.amount ?: 0.0
        val previousAmount = previousCat?.amount ?: 0.0

        if (currentAmount > 0 || previousAmount > 0) {
            val change = currentAmount - previousAmount
            val changePercentage = calculatePercentageChange(currentAmount, previousAmount)
            val trend = determineTrendDirection(changePercentage)

            categoryComparisons.add(
                CategoryComparison(
                    categoryName = category.categoryName,
                    categoryId = category.categoryId,
                    currentAmount = currentAmount,
                    previousAmount = previousAmount,
                    change = change,
                    changePercentage = changePercentage,
                    trend = trend
                )
            )
        }
    }

    return PeriodComparison(
        currentPeriod = current,
        previousPeriod = previous,
        incomeChange = incomeChange,
        incomeChangePercentage = incomeChangePercentage,
        expensesChange = expensesChange,
        expensesChangePercentage = expensesChangePercentage,
        netBalanceChange = netBalanceChange,
        categoryComparisons = categoryComparisons.sortedByDescending { kotlin.math.abs(it.change) }
    )
}
