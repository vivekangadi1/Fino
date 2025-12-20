package com.fino.app.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.PaymentMethodBreakdown
import com.fino.app.domain.model.PaymentMethodSpending
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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
    val selectedDate: LocalDate = LocalDate.now(),
    val periodLabel: String = "",
    val canNavigateBackward: Boolean = true,
    val canNavigateForward: Boolean = false,
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val transactionCount: Int = 0,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val paymentMethodBreakdown: PaymentMethodBreakdown? = null,
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
                val selectedDate = _uiState.value.selectedDate
                val filteredTransactions = filterByPeriod(transactions, period, selectedDate)

                // Calculate period label
                val periodLabel = formatPeriodLabel(period, selectedDate)

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

                // Calculate payment method breakdown
                val paymentMethodBreakdown = calculatePaymentMethodBreakdown(debitTransactions, totalSpent)

                _uiState.update {
                    it.copy(
                        periodLabel = periodLabel,
                        totalSpent = totalSpent,
                        totalIncome = totalIncome,
                        transactionCount = debitTransactions.size,
                        categoryBreakdown = categoryBreakdown,
                        paymentMethodBreakdown = paymentMethodBreakdown,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun filterByPeriod(
        transactions: List<Transaction>,
        period: AnalyticsPeriod,
        referenceDate: LocalDate
    ): List<Transaction> {
        return transactions.filter { txn ->
            when (period) {
                AnalyticsPeriod.WEEK -> {
                    val weekFields = WeekFields.of(Locale.getDefault())
                    val refWeek = referenceDate.get(weekFields.weekOfWeekBasedYear())
                    val txnWeek = txn.transactionDate.toLocalDate().get(weekFields.weekOfWeekBasedYear())
                    val refYear = referenceDate.year
                    val txnYear = txn.transactionDate.year
                    refWeek == txnWeek && refYear == txnYear
                }
                AnalyticsPeriod.MONTH -> {
                    val refMonth = YearMonth.from(referenceDate)
                    val txnMonth = YearMonth.from(txn.transactionDate)
                    refMonth == txnMonth
                }
                AnalyticsPeriod.YEAR -> {
                    referenceDate.year == txn.transactionDate.year
                }
            }
        }
    }

    private fun calculatePaymentMethodBreakdown(
        transactions: List<Transaction>,
        totalSpent: Double
    ): PaymentMethodBreakdown {
        // Group UPI transactions by bank
        val upiTransactions = transactions
            .filter { it.paymentMethod == "UPI" }
            .groupBy { it.bankName ?: "Unknown" }
            .map { (bank, txns) ->
                val amount = txns.sumOf { it.amount }
                PaymentMethodSpending(
                    paymentMethod = "UPI",
                    bankName = bank,
                    cardLastFour = null,
                    displayName = "$bank UPI",
                    amount = amount,
                    transactionCount = txns.size,
                    percentage = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }

        // Group credit card transactions by bank + card last 4
        val creditCardTransactions = transactions
            .filter { it.paymentMethod == "CREDIT_CARD" }
            .groupBy {
                val bank = it.bankName ?: "Unknown"
                val lastFour = it.cardLastFour ?: "****"
                Pair(bank, lastFour)
            }
            .map { (bankCard, txns) ->
                val (bank, lastFour) = bankCard
                val amount = txns.sumOf { it.amount }
                PaymentMethodSpending(
                    paymentMethod = "CREDIT_CARD",
                    bankName = bank,
                    cardLastFour = lastFour,
                    displayName = "$bank ****$lastFour",
                    amount = amount,
                    transactionCount = txns.size,
                    percentage = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }

        // Group unknown payment methods
        val unknownTransactions = transactions
            .filter { it.paymentMethod == null }

        val unknownSpending = if (unknownTransactions.isNotEmpty()) {
            val amount = unknownTransactions.sumOf { it.amount }
            PaymentMethodSpending(
                paymentMethod = "UNKNOWN",
                bankName = "Unknown",
                cardLastFour = null,
                displayName = "Unknown Payment Method",
                amount = amount,
                transactionCount = unknownTransactions.size,
                percentage = if (totalSpent > 0) (amount / totalSpent).toFloat() else 0f
            )
        } else null

        return PaymentMethodBreakdown(
            upiTransactions = upiTransactions,
            creditCardTransactions = creditCardTransactions,
            unknownTransactions = unknownSpending,
            totalUpiSpend = upiTransactions.sumOf { it.amount },
            totalCreditCardSpend = creditCardTransactions.sumOf { it.amount },
            totalUnknownSpend = unknownSpending?.amount ?: 0.0
        )
    }

    fun setPeriod(period: AnalyticsPeriod) {
        _uiState.update {
            it.copy(
                selectedPeriod = period,
                isLoading = true
                // Keep selectedDate unchanged - stay in same timeframe
            )
        }
        updateNavigationButtons()
        loadData()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }

    /**
     * Navigate to the previous period (previous week/month/year)
     */
    fun navigateToPreviousPeriod() {
        val currentDate = _uiState.value.selectedDate
        val newDate = when (_uiState.value.selectedPeriod) {
            AnalyticsPeriod.WEEK -> currentDate.minusWeeks(1)
            AnalyticsPeriod.MONTH -> currentDate.minusMonths(1)
            AnalyticsPeriod.YEAR -> currentDate.minusYears(1)
        }
        updateSelectedDate(newDate)
    }

    /**
     * Navigate to the next period (next week/month/year)
     * Only allowed if not in future
     */
    fun navigateToNextPeriod() {
        val currentDate = _uiState.value.selectedDate
        val newDate = when (_uiState.value.selectedPeriod) {
            AnalyticsPeriod.WEEK -> currentDate.plusWeeks(1)
            AnalyticsPeriod.MONTH -> currentDate.plusMonths(1)
            AnalyticsPeriod.YEAR -> currentDate.plusYears(1)
        }

        // Only navigate if the new period is not in the future
        if (!isPeriodInFuture(newDate, _uiState.value.selectedPeriod)) {
            updateSelectedDate(newDate)
        }
    }

    /**
     * Jump to the current period (today)
     */
    fun navigateToCurrentPeriod() {
        updateSelectedDate(LocalDate.now())
    }

    /**
     * Update the selected date and recalculate analytics
     */
    internal fun updateSelectedDate(newDate: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = newDate,
                isLoading = true
            )
        }
        updateNavigationButtons()
        loadData()
    }

    /**
     * Update navigation button states based on current period
     */
    private fun updateNavigationButtons() {
        val selectedDate = _uiState.value.selectedDate
        val selectedPeriod = _uiState.value.selectedPeriod

        val canGoForward = !isPeriodInFuture(selectedDate, selectedPeriod)

        _uiState.update {
            it.copy(
                canNavigateBackward = true,  // Can always go back in history
                canNavigateForward = canGoForward
            )
        }
    }

    /**
     * Check if the given period is in the future
     */
    private fun isPeriodInFuture(date: LocalDate, period: AnalyticsPeriod): Boolean {
        val now = LocalDate.now()
        return when (period) {
            AnalyticsPeriod.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val dateWeek = date.get(weekFields.weekOfWeekBasedYear())
                val dateYear = date.year
                val nowWeek = now.get(weekFields.weekOfWeekBasedYear())
                val nowYear = now.year
                (dateYear > nowYear) || (dateYear == nowYear && dateWeek > nowWeek)
            }
            AnalyticsPeriod.MONTH -> {
                YearMonth.from(date) > YearMonth.from(now)
            }
            AnalyticsPeriod.YEAR -> {
                date.year > now.year
            }
        }
    }

    /**
     * Format period label for display
     */
    private fun formatPeriodLabel(period: AnalyticsPeriod, date: LocalDate): String {
        return when (period) {
            AnalyticsPeriod.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val week = date.get(weekFields.weekOfWeekBasedYear())
                "Week $week, ${date.year}"
            }
            AnalyticsPeriod.MONTH -> {
                date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            }
            AnalyticsPeriod.YEAR -> {
                date.year.toString()
            }
        }
    }
}
