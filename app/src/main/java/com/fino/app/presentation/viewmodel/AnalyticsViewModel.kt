package com.fino.app.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.BudgetRepository
import com.fino.app.data.repository.CashbackRewardRepository
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.data.repository.NoticesRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Notice
import com.fino.app.service.notices.NoticesComputer
import com.fino.app.domain.model.CreditCard
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
import com.fino.app.service.forecast.BudgetForecast
import com.fino.app.service.forecast.ForecastService
import com.fino.app.presentation.components.SpendingInsight
import com.fino.app.presentation.components.generateSpendingInsights
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    THREE_MONTHS,
    YEAR,
    CUSTOM
}

/**
 * Headline metric: SPEND (debit totals) vs NET (income − outgoing).
 */
enum class AnalyticsMetric { SPEND, NET }

/**
 * Route a Noticed card taps into. Maps 1:1 to Phase A detail screens.
 */
sealed class InsightRoute {
    data class Merchant(val merchantKey: String) : InsightRoute()
    data class Bill(val creditCardId: Long) : InsightRoute()
    data class SubCategory(val categoryId: Long, val categoryName: String) : InsightRoute()
    data class Day(val epochDay: Long) : InsightRoute()
    object Subscriptions : InsightRoute()
    object NewMerchants : InsightRoute()
    object Weekend : InsightRoute()
    object Compare : InsightRoute()
}

data class TrendBars(
    val values: List<Float>,
    val todayIndex: Int,
    val leftLabel: String,
    val midLabel: String,
    val rightLabel: String,
    val epochDays: List<Long?> = emptyList()
)

data class InsightItem(
    val title: String,
    val body: String,
    val isWarn: Boolean = false,
    val chartData: List<Float>? = null,
    val route: InsightRoute? = null
)

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
    val customStart: LocalDate? = null,
    val customEnd: LocalDate? = null,
    val metric: AnalyticsMetric = AnalyticsMetric.SPEND,
    val headlineAmount: Double = 0.0,
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
    val budgetForecast: BudgetForecast? = null,
    val insights: List<SpendingInsight> = emptyList(),
    val trendBars: TrendBars? = null,
    val trendPercent: Float? = null,
    val previousPeriodLabel: String = "",
    val insightItems: List<InsightItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val creditCardRepository: CreditCardRepository,
    private val cashbackRewardRepository: CashbackRewardRepository,
    private val noticesRepository: NoticesRepository,
    private val noticesComputer: NoticesComputer,
    private val exportService: ExportService,
    private val forecastService: ForecastService
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
        // Load heavy analytics data lazily on demand rather than all at once
        // to prevent blocking the UI thread on startup
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive(),
                creditCardRepository.getActiveCardsFlow()
            ) { transactions, categories, creditCards ->
                Triple(transactions, categories, creditCards)
            }.collect { (transactions, categories, creditCards) ->
                // Cache all transactions for export
                allTransactions = transactions

                val period = _uiState.value.selectedPeriod
                val selectedDate = _uiState.value.selectedDate
                val customStart = _uiState.value.customStart
                val customEnd = _uiState.value.customEnd
                val metric = _uiState.value.metric
                val filteredTransactions = filterByPeriod(transactions, period, selectedDate, customStart, customEnd)

                val prevCustomStart: LocalDate?
                val prevCustomEnd: LocalDate?
                val previousDate: LocalDate
                if (period == AnalyticsPeriod.CUSTOM && customStart != null && customEnd != null) {
                    val duration = java.time.temporal.ChronoUnit.DAYS.between(customStart, customEnd) + 1
                    prevCustomEnd = customStart.minusDays(1)
                    prevCustomStart = customStart.minusDays(duration)
                    previousDate = prevCustomStart
                } else {
                    prevCustomStart = null
                    prevCustomEnd = null
                    previousDate = previousPeriodReferenceDate(period, selectedDate)
                }
                val previousTransactions = filterByPeriod(transactions, period, previousDate, prevCustomStart, prevCustomEnd)

                // Calculate period label
                val periodLabel = formatPeriodLabel(period, selectedDate, customStart, customEnd)
                val previousPeriodLabel = formatPeriodLabel(period, previousDate, prevCustomStart, prevCustomEnd)

                // Calculate totals from DEBIT transactions only
                val debitTransactions = filteredTransactions.filter { it.type == TransactionType.DEBIT }
                val totalSpent = debitTransactions.sumOf { it.amount }
                val totalIncome = filteredTransactions
                    .filter { it.type == TransactionType.CREDIT }
                    .sumOf { it.amount }
                val headlineAmount = when (metric) {
                    AnalyticsMetric.SPEND -> totalSpent
                    AnalyticsMetric.NET -> totalIncome - totalSpent
                }

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

                val categoryNameMap = categoryMap.mapValues { it.value.name }
                val trendBars = buildTrendBars(filteredTransactions, period, selectedDate, metric, customStart, customEnd)
                val prevDebitTotal = previousTransactions
                    .filter { it.type == TransactionType.DEBIT }
                    .sumOf { it.amount }
                val prevCreditTotal = previousTransactions
                    .filter { it.type == TransactionType.CREDIT }
                    .sumOf { it.amount }
                val prevHeadline = when (metric) {
                    AnalyticsMetric.SPEND -> prevDebitTotal
                    AnalyticsMetric.NET -> prevCreditTotal - prevDebitTotal
                }
                val trendPercent = if (kotlin.math.abs(prevHeadline) > 0.01) {
                    (((headlineAmount - prevHeadline) / kotlin.math.abs(prevHeadline)) * 100).toFloat()
                } else null
                val cashbackPeriodKey = if (period == AnalyticsPeriod.CUSTOM && customStart != null && customEnd != null) {
                    "custom-${customStart}-$customEnd"
                } else {
                    YearMonth.from(selectedDate).format(DateTimeFormatter.ofPattern("yyyy-MM"))
                }
                val cashbackTotal = cashbackRewardRepository.getTotalForPeriod(
                    if (period == AnalyticsPeriod.CUSTOM) {
                        YearMonth.from(selectedDate).format(DateTimeFormatter.ofPattern("yyyy-MM"))
                    } else cashbackPeriodKey
                )

                val insightItems = loadOrComputeNotices(
                    periodKey = cashbackPeriodKey,
                    current = debitTransactions,
                    previous = previousTransactions.filter { it.type == TransactionType.DEBIT },
                    categoryNames = categoryNameMap,
                    period = period,
                    selectedDate = selectedDate,
                    creditCards = creditCards,
                    allTransactions = transactions,
                    cashbackTotal = cashbackTotal,
                    customStart = customStart,
                    customEnd = customEnd
                ).map { it.toInsightItem() }

                _uiState.update {
                    it.copy(
                        periodLabel = periodLabel,
                        previousPeriodLabel = previousPeriodLabel,
                        totalSpent = totalSpent,
                        totalIncome = totalIncome,
                        headlineAmount = headlineAmount,
                        transactionCount = debitTransactions.size,
                        categoryBreakdown = categoryBreakdown,
                        paymentMethodBreakdown = paymentMethodBreakdown,
                        budgetProgress = budgetProgress,
                        showBudgetAlert = budgetAlert.first,
                        budgetAlertForCategory = budgetAlert.second,
                        trendBars = trendBars,
                        trendPercent = trendPercent,
                        insightItems = insightItems,
                        isLoading = false
                    )
                }

                // Update navigation buttons after data is loaded
                updateNavigationButtons()
            }
        }
    }

    private fun filterByPeriod(
        transactions: List<Transaction>,
        period: AnalyticsPeriod,
        referenceDate: LocalDate,
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null
    ): List<Transaction> {
        if (period == AnalyticsPeriod.CUSTOM) {
            val start = customStart ?: return emptyList()
            val end = customEnd ?: return emptyList()
            return transactions.filter { txn ->
                val date = txn.transactionDate.toLocalDate()
                !date.isBefore(start) && !date.isAfter(end)
            }
        }
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
                AnalyticsPeriod.THREE_MONTHS -> {
                    val endMonth = YearMonth.from(referenceDate)
                    val startMonth = endMonth.minusMonths(2)
                    val txnMonth = YearMonth.from(txn.transactionDate)
                    !txnMonth.isBefore(startMonth) && !txnMonth.isAfter(endMonth)
                }
                AnalyticsPeriod.YEAR -> {
                    referenceDate.year == txn.transactionDate.year
                }
                AnalyticsPeriod.CUSTOM -> false
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

    /**
     * Switch between SPEND and NET headline metric.
     * Re-runs loadData so trend/percent/headline all rebase.
     */
    fun setMetric(metric: AnalyticsMetric) {
        if (_uiState.value.metric == metric) return
        _uiState.update {
            it.copy(metric = metric, isLoading = true)
        }
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
        val state = _uiState.value
        val currentDate = state.selectedDate
        if (state.selectedPeriod == AnalyticsPeriod.CUSTOM &&
            state.customStart != null && state.customEnd != null
        ) {
            val duration = java.time.temporal.ChronoUnit.DAYS.between(state.customStart, state.customEnd) + 1
            val newStart = state.customStart.minusDays(duration)
            val newEnd = state.customStart.minusDays(1)
            _uiState.update {
                it.copy(
                    customStart = newStart,
                    customEnd = newEnd,
                    selectedDate = newStart,
                    isLoading = true
                )
            }
            loadData()
            return
        }
        val newDate = when (state.selectedPeriod) {
            AnalyticsPeriod.WEEK -> currentDate.minusWeeks(1)
            AnalyticsPeriod.MONTH -> currentDate.minusMonths(1)
            AnalyticsPeriod.THREE_MONTHS -> currentDate.minusMonths(3)
            AnalyticsPeriod.YEAR -> currentDate.minusYears(1)
            AnalyticsPeriod.CUSTOM -> currentDate
        }
        updateSelectedDate(newDate)
    }

    /**
     * Navigate to the next period (next week/month/year)
     * Only allowed if not in future
     */
    fun navigateToNextPeriod() {
        val state = _uiState.value
        val currentDate = state.selectedDate
        if (state.selectedPeriod == AnalyticsPeriod.CUSTOM &&
            state.customStart != null && state.customEnd != null
        ) {
            val duration = java.time.temporal.ChronoUnit.DAYS.between(state.customStart, state.customEnd) + 1
            val newStart = state.customEnd.plusDays(1)
            val newEnd = state.customEnd.plusDays(duration)
            if (!newEnd.isAfter(LocalDate.now())) {
                _uiState.update {
                    it.copy(
                        customStart = newStart,
                        customEnd = newEnd,
                        selectedDate = newStart,
                        isLoading = true
                    )
                }
                loadData()
            }
            return
        }
        val newDate = when (state.selectedPeriod) {
            AnalyticsPeriod.WEEK -> currentDate.plusWeeks(1)
            AnalyticsPeriod.MONTH -> currentDate.plusMonths(1)
            AnalyticsPeriod.THREE_MONTHS -> currentDate.plusMonths(3)
            AnalyticsPeriod.YEAR -> currentDate.plusYears(1)
            AnalyticsPeriod.CUSTOM -> currentDate
        }

        // Only navigate if the new period is not in the future
        if (!isPeriodInFuture(newDate, state.selectedPeriod)) {
            updateSelectedDate(newDate)
        }
    }

    /**
     * Switch to CUSTOM period with an explicit date range.
     */
    fun setCustomRange(start: LocalDate, end: LocalDate) {
        val (normStart, normEnd) = if (start.isAfter(end)) end to start else start to end
        _uiState.update {
            it.copy(
                selectedPeriod = AnalyticsPeriod.CUSTOM,
                customStart = normStart,
                customEnd = normEnd,
                selectedDate = normStart,
                isLoading = true
            )
        }
        updateNavigationButtons()
        loadData()
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
        // Navigation buttons will update after data loads
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
            AnalyticsPeriod.THREE_MONTHS -> {
                YearMonth.from(date) > YearMonth.from(now)
            }
            AnalyticsPeriod.YEAR -> {
                date.year > now.year
            }
            AnalyticsPeriod.CUSTOM -> {
                val end = _uiState.value.customEnd
                end != null && end.isAfter(now)
            }
        }
    }

    /**
     * Format period label for display
     */
    private fun formatPeriodLabel(
        period: AnalyticsPeriod,
        date: LocalDate,
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null
    ): String {
        return when (period) {
            AnalyticsPeriod.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val week = date.get(weekFields.weekOfWeekBasedYear())
                "Week $week, ${date.year}"
            }
            AnalyticsPeriod.MONTH -> {
                date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            }
            AnalyticsPeriod.THREE_MONTHS -> {
                val endMonth = YearMonth.from(date)
                val startMonth = endMonth.minusMonths(2)
                val monthFmt = DateTimeFormatter.ofPattern("MMM")
                if (startMonth.year == endMonth.year) {
                    "${startMonth.format(monthFmt)} – ${endMonth.format(monthFmt)} ${endMonth.year}"
                } else {
                    "${startMonth.format(monthFmt)} ${startMonth.year} – ${endMonth.format(monthFmt)} ${endMonth.year}"
                }
            }
            AnalyticsPeriod.YEAR -> {
                date.year.toString()
            }
            AnalyticsPeriod.CUSTOM -> {
                if (customStart == null || customEnd == null) "Custom range"
                else {
                    val dayFmt = DateTimeFormatter.ofPattern("MMM d")
                    if (customStart.year == customEnd.year) {
                        "${customStart.format(dayFmt)} – ${customEnd.format(dayFmt)}, ${customEnd.year}"
                    } else {
                        "${customStart.format(dayFmt)}, ${customStart.year} – ${customEnd.format(dayFmt)}, ${customEnd.year}"
                    }
                }
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
            AnalyticsPeriod.THREE_MONTHS -> {
                val endMonth = YearMonth.from(selectedDate)
                val startMonth = endMonth.minusMonths(2)
                Pair(startMonth.atDay(1), endMonth.atEndOfMonth())
            }
            AnalyticsPeriod.YEAR -> {
                val year = selectedDate.year
                val firstDay = LocalDate.of(year, 1, 1)
                val lastDay = LocalDate.of(year, 12, 31)
                Pair(firstDay, lastDay)
            }
            AnalyticsPeriod.CUSTOM -> {
                val start = _uiState.value.customStart ?: selectedDate
                val end = _uiState.value.customEnd ?: selectedDate
                Pair(start, end)
            }
        }

        // Filter transactions for the period
        val customStart = _uiState.value.customStart
        val customEnd = _uiState.value.customEnd
        val filteredTransactions = filterByPeriod(allTransactions, selectedPeriod, selectedDate, customStart, customEnd)

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
     * Load budget forecast data
     */
    suspend fun loadBudgetForecast() {
        val categoryNamesMap = mutableMapOf<Long, Pair<String, String>>()
        val categories = categoryRepository.getAllActive().first()
        categories.forEach { categoryNamesMap[it.id] = Pair(it.name, it.emoji) }

        val forecast = forecastService.calculateForecast(
            transactions = allTransactions,
            categoryNames = categoryNamesMap
        )

        _uiState.update {
            it.copy(budgetForecast = forecast)
        }
    }

    private fun previousPeriodReferenceDate(period: AnalyticsPeriod, date: LocalDate): LocalDate {
        return when (period) {
            AnalyticsPeriod.WEEK -> date.minusWeeks(1)
            AnalyticsPeriod.MONTH -> date.minusMonths(1)
            AnalyticsPeriod.THREE_MONTHS -> date.minusMonths(3)
            AnalyticsPeriod.YEAR -> date.minusYears(1)
            AnalyticsPeriod.CUSTOM -> date
        }
    }

    private fun buildTrendBars(
        transactions: List<Transaction>,
        period: AnalyticsPeriod,
        referenceDate: LocalDate,
        metric: AnalyticsMetric = AnalyticsMetric.SPEND,
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null
    ): TrendBars {
        val debit = transactions.filter { it.type == TransactionType.DEBIT }
        val credit = transactions.filter { it.type == TransactionType.CREDIT }
        val today = LocalDate.now()
        // dayBucket(day) = SPEND: sum of debits on day; NET: credits − debits on day
        fun dayBucket(day: LocalDate): Float {
            val out = debit.filter { it.transactionDate.toLocalDate() == day }.sumOf { it.amount }
            return when (metric) {
                AnalyticsMetric.SPEND -> out.toFloat()
                AnalyticsMetric.NET -> {
                    val inc = credit.filter { it.transactionDate.toLocalDate() == day }.sumOf { it.amount }
                    (inc - out).toFloat()
                }
            }
        }
        return when (period) {
            AnalyticsPeriod.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val startOfWeek = referenceDate.with(weekFields.dayOfWeek(), 1)
                val values = List(7) { i ->
                    val day = startOfWeek.plusDays(i.toLong())
                    dayBucket(day)
                }
                val epochDays: List<Long?> = List(7) { i -> startOfWeek.plusDays(i.toLong()).toEpochDay() }
                val todayIdx = if (today.isBefore(startOfWeek) || today.isAfter(startOfWeek.plusDays(6))) -1
                else java.time.temporal.ChronoUnit.DAYS.between(startOfWeek, today).toInt()
                TrendBars(
                    values = values,
                    todayIndex = todayIdx,
                    leftLabel = "Mon",
                    midLabel = "Thu",
                    rightLabel = "Sun",
                    epochDays = epochDays
                )
            }
            AnalyticsPeriod.MONTH -> {
                val ym = YearMonth.from(referenceDate)
                val days = ym.lengthOfMonth()
                val values = List(days) { i ->
                    val day = ym.atDay(i + 1)
                    dayBucket(day)
                }
                val epochDays: List<Long?> = List(days) { i -> ym.atDay(i + 1).toEpochDay() }
                val todayIdx = if (YearMonth.from(today) != ym) -1 else today.dayOfMonth - 1
                val monthFmt = DateTimeFormatter.ofPattern("MMM")
                val monthStr = ym.atDay(1).format(monthFmt)
                TrendBars(
                    values = values,
                    todayIndex = todayIdx,
                    leftLabel = "$monthStr 1",
                    midLabel = "$monthStr 15",
                    rightLabel = "$monthStr $days",
                    epochDays = epochDays
                )
            }
            AnalyticsPeriod.THREE_MONTHS -> {
                val endMonth = YearMonth.from(referenceDate)
                val startMonth = endMonth.minusMonths(2)
                val startDate = startMonth.atDay(1)
                val totalDays = java.time.temporal.ChronoUnit.DAYS
                    .between(startDate, endMonth.atEndOfMonth()).toInt() + 1
                val bucketCount = 12
                val daysPerBucket = totalDays.toFloat() / bucketCount
                val values = MutableList(bucketCount) { 0f }
                fun accum(txns: List<Transaction>, sign: Float) {
                    txns.forEach { txn ->
                        val txDate = txn.transactionDate.toLocalDate()
                        if (txDate.isBefore(startDate) || txDate.isAfter(endMonth.atEndOfMonth())) return@forEach
                        val daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, txDate).toInt()
                        val idx = (daysFromStart / daysPerBucket).toInt().coerceIn(0, bucketCount - 1)
                        values[idx] = values[idx] + sign * txn.amount.toFloat()
                    }
                }
                accum(debit, if (metric == AnalyticsMetric.SPEND) 1f else -1f)
                if (metric == AnalyticsMetric.NET) accum(credit, 1f)
                val todayIdx = if (YearMonth.from(today).isBefore(startMonth) ||
                    YearMonth.from(today).isAfter(endMonth)) -1
                else {
                    val d = java.time.temporal.ChronoUnit.DAYS.between(startDate, today).toInt()
                    (d / daysPerBucket).toInt().coerceIn(0, bucketCount - 1)
                }
                val monthFmt = DateTimeFormatter.ofPattern("MMM")
                TrendBars(
                    values = values,
                    todayIndex = todayIdx,
                    leftLabel = startMonth.atDay(1).format(monthFmt),
                    midLabel = startMonth.plusMonths(1).atDay(1).format(monthFmt),
                    rightLabel = endMonth.atDay(1).format(monthFmt)
                )
            }
            AnalyticsPeriod.YEAR -> {
                val year = referenceDate.year
                val values = List(12) { i ->
                    val ym = YearMonth.of(year, i + 1)
                    val out = debit.filter { YearMonth.from(it.transactionDate) == ym }.sumOf { it.amount }
                    when (metric) {
                        AnalyticsMetric.SPEND -> out.toFloat()
                        AnalyticsMetric.NET -> {
                            val inc = credit.filter { YearMonth.from(it.transactionDate) == ym }.sumOf { it.amount }
                            (inc - out).toFloat()
                        }
                    }
                }
                val todayIdx = if (today.year != year) -1 else today.monthValue - 1
                TrendBars(
                    values = values,
                    todayIndex = todayIdx,
                    leftLabel = "Jan",
                    midLabel = "Jul",
                    rightLabel = "Dec"
                )
            }
            AnalyticsPeriod.CUSTOM -> {
                if (customStart == null || customEnd == null) {
                    TrendBars(
                        values = emptyList(),
                        todayIndex = -1,
                        leftLabel = "",
                        midLabel = "",
                        rightLabel = ""
                    )
                } else {
                    val totalDays = (java.time.temporal.ChronoUnit.DAYS.between(customStart, customEnd).toInt() + 1)
                        .coerceAtLeast(1)
                    val dayFmt = DateTimeFormatter.ofPattern("MMM d")
                    if (totalDays <= 62) {
                        // Daily bars
                        val values = List(totalDays) { i -> dayBucket(customStart.plusDays(i.toLong())) }
                        val epochDays: List<Long?> = List(totalDays) { i -> customStart.plusDays(i.toLong()).toEpochDay() }
                        val todayIdx = if (today.isBefore(customStart) || today.isAfter(customEnd)) -1
                        else java.time.temporal.ChronoUnit.DAYS.between(customStart, today).toInt()
                        val midDate = customStart.plusDays(totalDays / 2L)
                        TrendBars(
                            values = values,
                            todayIndex = todayIdx,
                            leftLabel = customStart.format(dayFmt),
                            midLabel = midDate.format(dayFmt),
                            rightLabel = customEnd.format(dayFmt),
                            epochDays = epochDays
                        )
                    } else {
                        // Weekly bucket bars when range exceeds ~2 months
                        val bucketCount = 20.coerceAtMost(totalDays)
                        val daysPerBucket = totalDays.toFloat() / bucketCount
                        val values = MutableList(bucketCount) { 0f }
                        fun accum(txns: List<Transaction>, sign: Float) {
                            txns.forEach { txn ->
                                val txDate = txn.transactionDate.toLocalDate()
                                if (txDate.isBefore(customStart) || txDate.isAfter(customEnd)) return@forEach
                                val daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(customStart, txDate).toInt()
                                val idx = (daysFromStart / daysPerBucket).toInt().coerceIn(0, bucketCount - 1)
                                values[idx] = values[idx] + sign * txn.amount.toFloat()
                            }
                        }
                        accum(debit, if (metric == AnalyticsMetric.SPEND) 1f else -1f)
                        if (metric == AnalyticsMetric.NET) accum(credit, 1f)
                        val todayIdx = if (today.isBefore(customStart) || today.isAfter(customEnd)) -1
                        else {
                            val d = java.time.temporal.ChronoUnit.DAYS.between(customStart, today).toInt()
                            (d / daysPerBucket).toInt().coerceIn(0, bucketCount - 1)
                        }
                        TrendBars(
                            values = values,
                            todayIndex = todayIdx,
                            leftLabel = customStart.format(dayFmt),
                            midLabel = customStart.plusDays(totalDays / 2L).format(dayFmt),
                            rightLabel = customEnd.format(dayFmt)
                        )
                    }
                }
            }
        }
    }

    /**
     * Loads notices for the given period from cache. If the cache is empty or stale
     * (>24h), recomputes inline via NoticesComputer, overwrites the cache, and returns
     * the fresh result. This keeps Insights snappy while also handling first-install
     * and long-idle scenarios gracefully.
     */
    private suspend fun loadOrComputeNotices(
        periodKey: String,
        current: List<Transaction>,
        previous: List<Transaction>,
        categoryNames: Map<Long, String>,
        period: AnalyticsPeriod,
        selectedDate: LocalDate,
        creditCards: List<CreditCard>,
        allTransactions: List<Transaction>,
        cashbackTotal: Double,
        customStart: LocalDate? = null,
        customEnd: LocalDate? = null
    ): List<Notice> {
        // Don't cache CUSTOM ranges — they're ad-hoc. Always recompute.
        if (period != AnalyticsPeriod.CUSTOM) {
            val cached = noticesRepository.getForPeriod(periodKey)
            val staleMillis = 24L * 60 * 60 * 1000
            val isStale = cached.isEmpty() || (System.currentTimeMillis() - cached.first().computedAt) > staleMillis
            if (!isStale) return cached
        }

        val computerPeriod = when (period) {
            AnalyticsPeriod.WEEK -> NoticesComputer.Period.WEEK
            AnalyticsPeriod.MONTH -> NoticesComputer.Period.MONTH
            AnalyticsPeriod.THREE_MONTHS -> NoticesComputer.Period.THREE_MONTHS
            AnalyticsPeriod.YEAR -> NoticesComputer.Period.YEAR
            AnalyticsPeriod.CUSTOM -> NoticesComputer.Period.CUSTOM
        }
        val customBounds = if (period == AnalyticsPeriod.CUSTOM && customStart != null && customEnd != null) {
            customStart to customEnd
        } else null
        val fresh = noticesComputer.compute(
            current = current,
            previous = previous,
            categoryNames = categoryNames,
            period = computerPeriod,
            selectedDate = selectedDate,
            creditCards = creditCards,
            allTransactions = allTransactions,
            cashbackTotal = cashbackTotal,
            periodKey = periodKey,
            customBounds = customBounds
        )
        if (period != AnalyticsPeriod.CUSTOM) {
            noticesRepository.replaceForPeriod(periodKey, fresh)
        }
        return fresh
    }

    private fun Notice.toInsightItem(): InsightItem {
        val route: InsightRoute? = routeJson?.let { raw ->
            val parts = raw.split("|")
            when (parts[0]) {
                "MERCHANT" -> parts.getOrNull(1)?.let { InsightRoute.Merchant(it) }
                "BILL" -> parts.getOrNull(1)?.toLongOrNull()?.let { InsightRoute.Bill(it) }
                "SUBCATEGORY" -> {
                    val catId = parts.getOrNull(1)?.toLongOrNull()
                    val catName = parts.getOrNull(2)
                    if (catId != null && catName != null) InsightRoute.SubCategory(catId, catName) else null
                }
                "DAY" -> parts.getOrNull(1)?.toLongOrNull()?.let { InsightRoute.Day(it) }
                "SUBSCRIPTIONS" -> InsightRoute.Subscriptions
                "NEW_MERCHANTS" -> InsightRoute.NewMerchants
                "WEEKEND" -> InsightRoute.Weekend
                "COMPARE" -> InsightRoute.Compare
                else -> null
            }
        }
        val chart = chartDataJson
            ?.split(",")
            ?.mapNotNull { it.trim().toFloatOrNull() }
            ?.takeIf { it.isNotEmpty() }
        return InsightItem(
            title = title,
            body = body,
            isWarn = isWarn,
            chartData = chart,
            route = route
        )
    }

    // Insight computation moved to NoticesComputer; Notice→InsightItem mapping handled above.

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
