package com.fino.app.domain.model

import java.time.YearMonth

data class MonthlySpending(
    val yearMonth: YearMonth,
    val totalSpent: Double,
    val transactionCount: Int
)

data class SpendingTrend(
    val periods: List<MonthlySpending>,
    val trendDirection: TrendDirection,
    val averageSpending: Double,
    val percentageChange: Float // vs previous period
)

enum class TrendDirection {
    INCREASING,
    DECREASING,
    STABLE
}

fun calculatePercentageChange(current: Double, previous: Double): Float {
    if (previous == 0.0) return 0f
    return ((current - previous) / previous * 100).toFloat()
}

fun determineTrendDirection(percentageChange: Float): TrendDirection {
    return when {
        percentageChange > 5f -> TrendDirection.INCREASING
        percentageChange < -5f -> TrendDirection.DECREASING
        else -> TrendDirection.STABLE
    }
}
