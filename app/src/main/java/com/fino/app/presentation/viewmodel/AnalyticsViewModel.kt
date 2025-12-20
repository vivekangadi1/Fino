package com.fino.app.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.BudgetRepository
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.BudgetProgress
import com.fino.app.domain.model.BudgetStatus
import com.fino.app.domain.model.ExportFormat
import com.fino.app.domain.model.ExportRequest
import com.fino.app.domain.model.ExportResult
import com.fino.app.domain.model.MonthlySpending
import com.fino.app.domain.model.PaymentMethodBreakdown
import com.fino.app.domain.model.PaymentMethodSpending
import com.fino.app.domain.model.PaymentMethodTrend
import com.fino.app.domain.model.SpendingTrend
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.domain.model.YearOverYearComparison
import com.fino.app.domain.model.YearlySpendingData
import com.fino.app.domain.model.calculatePaymentMethodTrend
import com.fino.app.domain.model.calculatePercentageChange
import com.fino.app.domain.model.calculateYearOverYearComparison
import com.fino.app.domain.model.determineTrendDirection
import com.fino.app.service.export.ExportService
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
    val spendingTrend: SpendingTrend? = null,
    val trendPeriodCount: Int = 6, // 6 months by default
    val budgetProgress: List<BudgetProgress> = emptyList(),
    val showBudgetAlert: Boolean = false,
    val budgetAlertForCategory: Long? = null,
    val yearOverYearComparison: YearOverYearComparison? = null,
    val paymentMethodTrend: PaymentMethodTrend? = null,
    val spendingHeatmapData: List<com.fino.app.presentation.components.MonthSpendingData> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val exportService: ExportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    // Cache all transactions for export
    private var allTransactions: List<Transaction> = emptyList()

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
        viewModelScope.launch {
            loadTrendData()
            loadYearOverYearData()
            loadPaymentMethodTrend()
            loadSpendingHeatmapData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { transactions, categories ->
                Pair(transactions, categories)
            }.collect { (transactions, categories) ->
                // Cache all transactions for export
                allTransactions = transactions

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

                // Load budget progress
                val budgetProgress = loadBudgetProgress(
                    filteredTransactions = filteredTransactions,
                    categories = categories,
                    selectedDate = selectedDate,
                    selectedPeriod = period
                )

                // Check for budget alerts
                val budgetAlert = checkBudgetAlert(budgetProgress)

                _uiState.update {
                    it.copy(
                        periodLabel = periodLabel,
                        totalSpent = totalSpent,
                        totalIncome = totalIncome,
                        transactionCount = debitTransactions.size,
                        categoryBreakdown = categoryBreakdown,
                        paymentMethodBreakdown = paymentMethodBreakdown,
                        budgetProgress = budgetProgress,
                        showBudgetAlert = budgetAlert.first,
                        budgetAlertForCategory = budgetAlert.second,
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

    /**
     * Load budget progress for the current period
     */
    private suspend fun loadBudgetProgress(
        filteredTransactions: List<Transaction>,
        categories: List<com.fino.app.domain.model.Category>,
        selectedDate: LocalDate,
        selectedPeriod: AnalyticsPeriod
    ): List<BudgetProgress> {
        // Only load budgets for MONTH period
        if (selectedPeriod != AnalyticsPeriod.MONTH) {
            return emptyList()
        }

        val yearMonth = YearMonth.from(selectedDate)
        val budgets = budgetRepository.getBudgetsForMonth(yearMonth)

        if (budgets.isEmpty()) {
            return emptyList()
        }

        // Calculate spending per category from filtered transactions
        val categorySpending = filteredTransactions
            .filter { it.type == TransactionType.DEBIT }
            .groupBy { it.categoryId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val categoryMap = categories.associateBy { it.id }

        return budgets.map { budget ->
            val spent = categorySpending[budget.categoryId] ?: 0.0
            val remaining = budget.monthlyLimit - spent
            val percentage = if (budget.monthlyLimit > 0) {
                (spent / budget.monthlyLimit).toFloat()
            } else {
                0f
            }

            val status = when {
                percentage >= 1.0f -> BudgetStatus.OVER_BUDGET
                percentage >= 0.75f -> BudgetStatus.APPROACHING_LIMIT
                else -> BudgetStatus.UNDER_BUDGET
            }

            val category = categoryMap[budget.categoryId]

            BudgetProgress(
                budget = budget,
                categoryName = category?.name ?: "Unknown",
                spent = spent,
                remaining = remaining,
                percentage = percentage,
                status = status
            )
        }
    }

    /**
     * Check if any budget has crossed alert thresholds
     * Returns Pair(showAlert, categoryId)
     */
    private fun checkBudgetAlert(budgetProgress: List<BudgetProgress>): Pair<Boolean, Long?> {
        // Find first budget that crossed threshold
        val alertBudget = budgetProgress.firstOrNull { progress ->
            val budget = progress.budget
            val percentage = progress.percentage

            (budget.alertAt100 && percentage >= 1.0f) ||
            (budget.alertAt75 && percentage >= 0.75f && percentage < 1.0f)
        }

        return if (alertBudget != null) {
            Pair(true, alertBudget.budget.categoryId)
        } else {
            Pair(false, null)
        }
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
     * Dismiss the budget alert dialog
     */
    fun dismissBudgetAlert() {
        _uiState.update {
            it.copy(
                showBudgetAlert = false,
                budgetAlertForCategory = null
            )
        }
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
     * Jump to last month (1 month ago)
     */
    fun jumpToLastMonth() {
        updateSelectedDate(LocalDate.now().minusMonths(1))
    }

    /**
     * Jump to 3 months ago
     */
    fun jumpTo3MonthsAgo() {
        updateSelectedDate(LocalDate.now().minusMonths(3))
    }

    /**
     * Jump to last year (same month, 1 year ago)
     */
    fun jumpToLastYear() {
        updateSelectedDate(LocalDate.now().minusYears(1))
    }

    /**
     * Jump to same month last year
     */
    fun jumpToSameMonthLastYear() {
        val now = LocalDate.now()
        updateSelectedDate(now.minusYears(1))
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

    /**
     * Load trend data for the specified number of periods (months)
     */
    suspend fun loadTrendData(periodCount: Int = 6) {
        val periods = mutableListOf<MonthlySpending>()
        val currentMonth = YearMonth.now()

        // Load historical data for specified period count
        for (i in periodCount - 1 downTo 0) {
            val month = currentMonth.minusMonths(i.toLong())
            val transactions = transactionRepository.getTransactionsForMonth(month)
            val spending = transactions.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
            periods.add(MonthlySpending(month, spending, transactions.size))
        }

        // Calculate average spending
        val average = if (periods.isNotEmpty()) {
            periods.map { it.totalSpent }.average()
        } else 0.0

        // Calculate percentage change (current vs previous)
        val percentageChange = if (periods.size >= 2) {
            calculatePercentageChange(
                periods.last().totalSpent,
                periods[periods.size - 2].totalSpent
            )
        } else 0f

        // Determine trend direction
        val direction = determineTrendDirection(percentageChange)

        val trend = SpendingTrend(
            periods = periods,
            trendDirection = direction,
            averageSpending = average,
            percentageChange = percentageChange
        )

        _uiState.update {
            it.copy(spendingTrend = trend)
        }
    }

    /**
     * Export transactions for the current period to specified format
     */
    suspend fun exportCurrentPeriod(format: ExportFormat): ExportResult {
        val selectedDate = _uiState.value.selectedDate
        val selectedPeriod = _uiState.value.selectedPeriod

        // Determine start and end dates based on period
        val (startDate, endDate) = when (selectedPeriod) {
            AnalyticsPeriod.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val firstDayOfWeek = selectedDate.with(weekFields.dayOfWeek(), 1)
                val lastDayOfWeek = firstDayOfWeek.plusDays(6)
                Pair(firstDayOfWeek, lastDayOfWeek)
            }
            AnalyticsPeriod.MONTH -> {
                val yearMonth = YearMonth.from(selectedDate)
                val firstDay = yearMonth.atDay(1)
                val lastDay = yearMonth.atEndOfMonth()
                Pair(firstDay, lastDay)
            }
            AnalyticsPeriod.YEAR -> {
                val year = selectedDate.year
                val firstDay = LocalDate.of(year, 1, 1)
                val lastDay = LocalDate.of(year, 12, 31)
                Pair(firstDay, lastDay)
            }
        }

        // Filter transactions for the period
        val filteredTransactions = filterByPeriod(allTransactions, selectedPeriod, selectedDate)

        // Create export request
        val request = ExportRequest(
            format = format,
            startDate = startDate,
            endDate = endDate,
            transactions = filteredTransactions
        )

        return exportService.exportTransactions(request)
    }

    /**
     * Load year-over-year comparison data for the current month
     */
    suspend fun loadYearOverYearData() {
        val currentDate = _uiState.value.selectedDate
        val currentMonth = currentDate.month
        val currentYear = currentDate.year

        // Collect data for the same month across the last 3-5 years
        val yearlyData = mutableListOf<YearlySpendingData>()

        // Go back 3 years from current year
        for (yearsAgo in 0..2) {
            val year = currentYear - yearsAgo
            val targetMonth = YearMonth.of(year, currentMonth)

            // Get transactions for that month
            val transactions = allTransactions.filter { transaction ->
                val txMonth = YearMonth.from(transaction.transactionDate)
                txMonth == targetMonth
            }

            if (transactions.isNotEmpty() || yearsAgo == 0) {
                // Include current year even if empty
                val totalSpent = transactions
                    .filter { it.type == TransactionType.DEBIT }
                    .sumOf { it.amount }

                val totalIncome = transactions
                    .filter { it.type == TransactionType.CREDIT }
                    .sumOf { it.amount }

                yearlyData.add(
                    YearlySpendingData(
                        year = year,
                        month = currentMonth,
                        totalSpent = totalSpent,
                        totalIncome = totalIncome,
                        transactionCount = transactions.size
                    )
                )
            }
        }

        // Calculate year-over-year comparison
        if (yearlyData.isNotEmpty()) {
            val yoyComparison = calculateYearOverYearComparison(
                month = currentMonth,
                yearlyData = yearlyData
            )

            _uiState.update {
                it.copy(yearOverYearComparison = yoyComparison)
            }
        }
    }

    /**
     * Load payment method trend data for the last 6 months
     */
    suspend fun loadPaymentMethodTrend(periodCount: Int = 6) {
        val paymentMethodTrend = calculatePaymentMethodTrend(
            transactions = allTransactions,
            periodCount = periodCount
        )

        _uiState.update {
            it.copy(paymentMethodTrend = paymentMethodTrend)
        }
    }

    /**
     * Load spending heatmap data for the last 24 months
     */
    suspend fun loadSpendingHeatmapData() {
        val currentMonth = YearMonth.now()
        val heatmapData = mutableListOf<com.fino.app.presentation.components.MonthSpendingData>()

        // Load data for last 24 months
        val spendingByMonth = mutableListOf<Pair<YearMonth, Double>>()
        for (i in 23 downTo 0) {
            val targetMonth = currentMonth.minusMonths(i.toLong())
            val monthTransactions = allTransactions.filter { transaction ->
                val txMonth = YearMonth.from(transaction.transactionDate)
                txMonth == targetMonth && transaction.type == TransactionType.DEBIT
            }
            val totalSpent = monthTransactions.sumOf { it.amount }
            spendingByMonth.add(targetMonth to totalSpent)
        }

        // Find max spending for normalization
        val maxSpending = spendingByMonth.maxOfOrNull { it.second } ?: 0.0

        // Calculate intensity for each month
        spendingByMonth.forEach { (yearMonth, spending) ->
            val intensity = if (maxSpending > 0) {
                (spending / maxSpending).toFloat()
            } else {
                0f
            }

            heatmapData.add(
                com.fino.app.presentation.components.MonthSpendingData(
                    yearMonth = yearMonth,
                    totalSpent = spending,
                    intensity = intensity
                )
            )
        }

        _uiState.update {
            it.copy(spendingHeatmapData = heatmapData)
        }
    }
}
