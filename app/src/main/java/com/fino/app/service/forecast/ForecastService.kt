package com.fino.app.service.forecast

import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Confidence level for budget forecasts
 */
enum class ForecastConfidence(val displayText: String, val description: String) {
    HIGH("High", "Based on 3+ months of consistent data"),
    MEDIUM("Medium", "Based on 2-3 months of data"),
    LOW("Low", "Limited historical data available"),
    INSUFFICIENT("Not enough data", "Need more transaction history")
}

/**
 * Category-level forecast data
 */
data class CategoryForecast(
    val categoryId: Long,
    val categoryName: String,
    val emoji: String,
    val forecastAmount: Double,
    val averageAmount: Double,
    val isRecurring: Boolean,
    val monthsOfData: Int
)

/**
 * Monthly budget forecast
 */
data class BudgetForecast(
    val month: YearMonth,
    val totalForecast: Double,
    val recurringTotal: Double,
    val variableTotal: Double,
    val categoryForecasts: List<CategoryForecast>,
    val confidence: ForecastConfidence,
    val currentMonthSpent: Double,
    val percentageOfForecast: Float
)

/**
 * Service for calculating budget forecasts based on historical spending patterns
 */
@Singleton
class ForecastService @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val recurringRuleRepository: RecurringRuleRepository
) {
    /**
     * Calculate forecast for the next month based on:
     * - Active recurring rules (bills, subscriptions)
     * - Average variable spending from last N months
     */
    suspend fun calculateForecast(
        transactions: List<Transaction>,
        categoryNames: Map<Long, Pair<String, String>>,
        monthsOfHistory: Int = 3
    ): BudgetForecast {
        val currentMonth = YearMonth.now()
        val nextMonth = currentMonth.plusMonths(1)

        // Get active recurring rules total
        val recurringRules = recurringRuleRepository.getActiveRulesWithNextExpected(nextMonth)
        val recurringTotal = recurringRules.sumOf { it.expectedAmount }

        // Calculate average variable spending by category
        val historicalData = mutableMapOf<Long, MutableList<Double>>()
        val debitTransactions = transactions.filter { it.type == TransactionType.DEBIT }

        // Group transactions by month and category
        for (monthOffset in 1..monthsOfHistory) {
            val targetMonth = currentMonth.minusMonths(monthOffset.toLong())
            val monthTransactions = debitTransactions.filter {
                YearMonth.from(it.transactionDate) == targetMonth
            }

            monthTransactions.groupBy { it.categoryId ?: 0L }.forEach { (categoryId, txns) ->
                val amount = txns.sumOf { it.amount }
                historicalData.getOrPut(categoryId) { mutableListOf() }.add(amount)
            }
        }

        // Calculate category forecasts
        val categoryForecasts = mutableListOf<CategoryForecast>()

        // Add recurring items
        val recurringByCategoryId = recurringRules.groupBy { it.categoryId }
        recurringByCategoryId.forEach { (categoryId, rules) ->
            val (name, emoji) = categoryNames[categoryId] ?: Pair("Recurring Bills", "📅")
            val totalForCategory = rules.sumOf { it.expectedAmount }
            categoryForecasts.add(
                CategoryForecast(
                    categoryId = categoryId,
                    categoryName = name,
                    emoji = emoji,
                    forecastAmount = totalForCategory,
                    averageAmount = totalForCategory,
                    isRecurring = true,
                    monthsOfData = monthsOfHistory
                )
            )
        }

        // Add variable spending forecasts
        val recurringCategoryIds = recurringByCategoryId.keys
        historicalData.filter { it.key !in recurringCategoryIds }.forEach { (categoryId, amounts) ->
            val (name, emoji) = categoryNames[categoryId] ?: Pair("Other", "📦")
            val average = amounts.average()
            categoryForecasts.add(
                CategoryForecast(
                    categoryId = categoryId,
                    categoryName = name,
                    emoji = emoji,
                    forecastAmount = average,
                    averageAmount = average,
                    isRecurring = false,
                    monthsOfData = amounts.size
                )
            )
        }

        // Sort by forecast amount descending
        categoryForecasts.sortByDescending { it.forecastAmount }

        // Calculate totals
        val variableTotal = categoryForecasts.filter { !it.isRecurring }.sumOf { it.forecastAmount }
        val totalForecast = recurringTotal + variableTotal

        // Calculate confidence
        val monthsWithData = historicalData.values.maxOfOrNull { it.size } ?: 0
        val confidence = when {
            monthsWithData >= 3 -> ForecastConfidence.HIGH
            monthsWithData >= 2 -> ForecastConfidence.MEDIUM
            monthsWithData >= 1 -> ForecastConfidence.LOW
            else -> ForecastConfidence.INSUFFICIENT
        }

        // Current month spending
        val currentMonthSpent = debitTransactions
            .filter { YearMonth.from(it.transactionDate) == currentMonth }
            .sumOf { it.amount }

        // Calculate percentage progress
        val percentageOfForecast = if (totalForecast > 0) {
            (currentMonthSpent / totalForecast).toFloat().coerceAtMost(1.5f)
        } else {
            0f
        }

        return BudgetForecast(
            month = nextMonth,
            totalForecast = totalForecast,
            recurringTotal = recurringTotal,
            variableTotal = variableTotal,
            categoryForecasts = categoryForecasts,
            confidence = confidence,
            currentMonthSpent = currentMonthSpent,
            percentageOfForecast = percentageOfForecast
        )
    }
}
