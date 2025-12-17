package com.fino.app.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

/**
 * Time period for analytics
 */
enum class AnalyticsPeriod {
    WEEK,
    MONTH,
    YEAR
}

/**
 * Category spending data for display
 */
data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val emoji: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

/**
 * UI state for Analytics screen
 */
data class AnalyticsUiState(
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.MONTH,
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val transactionCount: Int = 0,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    // Predefined colors for categories
    private val categoryColors = listOf(
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFFFFE66D), // Yellow
        Color(0xFF95E1D3), // Mint
        Color(0xFFF38181), // Coral
        Color(0xFFAA96DA), // Purple
        Color(0xFFFCBF49), // Orange
        Color(0xFF2EC4B6), // Cyan
        Color(0xFFE84A5F), // Pink
        Color(0xFF5C7AEA)  // Blue
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { transactions, categories ->
                Pair(transactions, categories)
            }.collect { (transactions, categories) ->
                val period = _uiState.value.selectedPeriod
                val filteredTransactions = filterByPeriod(transactions, period)

                // Calculate totals from DEBIT transactions only
                val debitTransactions = filteredTransactions.filter { it.type == TransactionType.DEBIT }
                val totalSpent = debitTransactions.sumOf { it.amount }
                val totalIncome = filteredTransactions
                    .filter { it.type == TransactionType.CREDIT }
                    .sumOf { it.amount }

                // Group by category and calculate breakdown
                val categoryMap = categories.associateBy { it.id }
                val categoryBreakdown = debitTransactions
                    .groupBy { it.categoryId }
                    .map { (categoryId, txns) ->
                        val category = categoryId?.let { categoryMap[it] }
                        val amount = txns.sumOf { it.amount }
                        val percentage = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f

                        CategorySpending(
                            categoryId = categoryId ?: 0L,
                            categoryName = category?.name ?: "Uncategorized",
                            emoji = category?.emoji ?: "📦",
                            amount = amount,
                            percentage = percentage,
                            color = categoryColors[(categoryId?.toInt() ?: 0) % categoryColors.size]
                        )
                    }
                    .sortedByDescending { it.amount }

                _uiState.update {
                    it.copy(
                        totalSpent = totalSpent,
                        totalIncome = totalIncome,
                        transactionCount = debitTransactions.size,
                        categoryBreakdown = categoryBreakdown,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun filterByPeriod(transactions: List<Transaction>, period: AnalyticsPeriod): List<Transaction> {
        val now = LocalDateTime.now()
        return transactions.filter { txn ->
            when (period) {
                AnalyticsPeriod.WEEK -> {
                    val weekFields = WeekFields.of(Locale.getDefault())
                    val currentWeek = now.get(weekFields.weekOfWeekBasedYear())
                    val txnWeek = txn.transactionDate.get(weekFields.weekOfWeekBasedYear())
                    val currentYear = now.year
                    val txnYear = txn.transactionDate.year
                    currentWeek == txnWeek && currentYear == txnYear
                }
                AnalyticsPeriod.MONTH -> {
                    val currentMonth = YearMonth.from(now)
                    val txnMonth = YearMonth.from(txn.transactionDate)
                    currentMonth == txnMonth
                }
                AnalyticsPeriod.YEAR -> {
                    now.year == txn.transactionDate.year
                }
            }
        }
    }

    fun setPeriod(period: AnalyticsPeriod) {
        _uiState.update { it.copy(selectedPeriod = period, isLoading = true) }
        loadData()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }
}
