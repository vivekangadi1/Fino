package com.fino.app.domain.model

import java.time.Month

/**
 * Year-over-year spending data for a specific month across multiple years
 */
data class YearlySpendingData(
    val year: Int,
    val month: Month,
    val totalSpent: Double,
    val totalIncome: Double,
    val transactionCount: Int
)

/**
 * Year-over-year comparison for a specific month across years
 */
data class YearOverYearComparison(
    val month: Month,
    val monthName: String,
    val yearlyData: List<YearlySpendingData>, // Sorted by year descending
    val averageSpending: Double,
    val highestYear: YearlySpendingData?,
    val lowestYear: YearlySpendingData?,
    val yearOverYearChanges: List<YearOverYearChange>
)

/**
 * Change between consecutive years for the same month
 */
data class YearOverYearChange(
    val fromYear: Int,
    val toYear: Int,
    val spendingChange: Double,
    val spendingChangePercentage: Float,
    val incomeChange: Double,
    val incomeChangePercentage: Float,
    val trend: TrendDirection
)

/**
 * Calculate year-over-year comparison from yearly data
 */
fun calculateYearOverYearComparison(
    month: Month,
    yearlyData: List<YearlySpendingData>
): YearOverYearComparison {
    val sortedData = yearlyData.sortedByDescending { it.year }

    // Calculate average spending
    val averageSpending = if (sortedData.isNotEmpty()) {
        sortedData.map { it.totalSpent }.average()
    } else {
        0.0
    }

    // Find highest and lowest spending years
    val highestYear = sortedData.maxByOrNull { it.totalSpent }
    val lowestYear = sortedData.minByOrNull { it.totalSpent }

    // Calculate year-over-year changes
    val yearOverYearChanges = mutableListOf<YearOverYearChange>()
    for (i in 0 until sortedData.size - 1) {
        val currentYear = sortedData[i]
        val previousYear = sortedData[i + 1]

        val spendingChange = currentYear.totalSpent - previousYear.totalSpent
        val spendingChangePercentage = calculatePercentageChange(
            currentYear.totalSpent,
            previousYear.totalSpent
        )

        val incomeChange = currentYear.totalIncome - previousYear.totalIncome
        val incomeChangePercentage = calculatePercentageChange(
            currentYear.totalIncome,
            previousYear.totalIncome
        )

        val trend = determineTrendDirection(spendingChangePercentage)

        yearOverYearChanges.add(
            YearOverYearChange(
                fromYear = previousYear.year,
                toYear = currentYear.year,
                spendingChange = spendingChange,
                spendingChangePercentage = spendingChangePercentage,
                incomeChange = incomeChange,
                incomeChangePercentage = incomeChangePercentage,
                trend = trend
            )
        )
    }

    return YearOverYearComparison(
        month = month,
        monthName = month.name.lowercase().replaceFirstChar { it.uppercase() },
        yearlyData = sortedData,
        averageSpending = averageSpending,
        highestYear = highestYear,
        lowestYear = lowestYear,
        yearOverYearChanges = yearOverYearChanges
    )
}
