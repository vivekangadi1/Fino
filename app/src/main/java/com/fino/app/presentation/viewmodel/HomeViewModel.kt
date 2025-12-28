package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.EventRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.data.repository.UpcomingBillsRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.BillSummary
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.EventSummary
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.domain.model.UpcomingBill
import com.fino.app.gamification.LevelCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Spending period for filtering
 */
enum class SpendingPeriod {
    TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR
}

/**
 * Category spending breakdown
 */
data class CategorySpendingData(
    val categoryId: Long,
    val categoryName: String,
    val emoji: String,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

/**
 * UI state for Home screen
 */
data class HomeUiState(
    val totalBalance: Double = 0.0,
    val monthlySpent: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlySaved: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val currentStreak: Int = 0,
    val currentLevel: Int = 1,
    val levelName: String = "Budget Beginner",
    val totalXp: Int = 0,
    val xpProgress: Float = 0f,
    val upcomingBillsSummary: BillSummary? = null,
    val nextBills: List<UpcomingBill> = emptyList(),
    val activeEventSummary: EventSummary? = null,
    val hasActiveEvent: Boolean = false,
    val isLoading: Boolean = true,
    // Period-based spending
    val selectedSpendingPeriod: SpendingPeriod = SpendingPeriod.TODAY,
    val periodSpending: Double = 0.0,
    val periodCategoryBreakdown: List<CategorySpendingData> = emptyList(),
    val allTransactions: List<Transaction> = emptyList(),
    val categoryNames: Map<Long, Pair<String, String>> = emptyMap(), // categoryId -> (name, emoji)
    // Uncategorized transactions
    val uncategorizedCount: Int = 0,
    val uncategorizedTransactions: List<Transaction> = emptyList()
) {
    val hasUrgentBills: Boolean
        get() = (upcomingBillsSummary?.overdueCount ?: 0) > 0 ||
                (upcomingBillsSummary?.dueTodayCount ?: 0) > 0
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userStatsRepository: UserStatsRepository,
    private val upcomingBillsRepository: UpcomingBillsRepository,
    private val eventRepository: EventRepository,
    private val levelCalculator: LevelCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllActive().collect { categories ->
                val categoryNamesMap = categories.associate { it.id to Pair(it.name, it.emoji) }
                _uiState.update { state ->
                    val breakdown = calculatePeriodSpending(
                        state.allTransactions,
                        state.selectedSpendingPeriod,
                        categoryNamesMap
                    )
                    state.copy(
                        categoryNames = categoryNamesMap,
                        periodCategoryBreakdown = breakdown.second,
                        periodSpending = breakdown.first
                    )
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            // Combine all flows
            combine(
                transactionRepository.getAllTransactionsFlow(),
                userStatsRepository.getUserStatsFlow(),
                upcomingBillsRepository.getUpcomingBillsFlow(),
                eventRepository.getEventSummariesFlow()
            ) { transactions: List<Transaction>, userStats, bills: List<UpcomingBill>, eventSummaries: List<EventSummary> ->
                // Return a data class or tuple with all data
                HomeData(transactions, userStats, bills, eventSummaries)
            }.collect { homeData ->
                val transactions = homeData.transactions
                val userStats = homeData.userStats
                val bills = homeData.bills
                val eventSummaries = homeData.eventSummaries

                val currentMonth = YearMonth.now()

                // Build a set of event IDs that should be excluded from main totals
                val excludedEventIds = eventSummaries
                    .filter { it.event.excludeFromMainTotals }
                    .map { it.event.id }
                    .toSet()

                // Filter transactions for current month
                val monthlyTransactions = transactions.filter { txn ->
                    val txnMonth = YearMonth.from(txn.transactionDate)
                    txnMonth == currentMonth
                }

                // Filter out transactions from excluded events for main totals
                val regularTransactions = transactions.filter { txn ->
                    txn.eventId == null || !excludedEventIds.contains(txn.eventId)
                }

                val regularMonthlyTransactions = monthlyTransactions.filter { txn ->
                    txn.eventId == null || !excludedEventIds.contains(txn.eventId)
                }

                // Calculate totals (excluding event expenses marked as excluded)
                val totalIncome = regularTransactions
                    .filter { it.type == TransactionType.CREDIT }
                    .sumOf { it.amount }

                val totalExpenses = regularTransactions
                    .filter { it.type == TransactionType.DEBIT }
                    .sumOf { it.amount }

                val totalSavings = regularTransactions
                    .filter { it.type == TransactionType.SAVINGS }
                    .sumOf { it.amount }

                val monthlyIncome = regularMonthlyTransactions
                    .filter { it.type == TransactionType.CREDIT }
                    .sumOf { it.amount }

                val monthlySpent = regularMonthlyTransactions
                    .filter { it.type == TransactionType.DEBIT }
                    .sumOf { it.amount }

                val monthlySaved = regularMonthlyTransactions
                    .filter { it.type == TransactionType.SAVINGS }
                    .sumOf { it.amount }

                // Get recent transactions (top 5, sorted by date descending)
                val recentTransactions = transactions
                    .sortedByDescending { it.transactionDate }
                    .take(5)

                // User stats
                val currentStreak = userStats?.currentStreak ?: 0
                val totalXp = userStats?.totalXp ?: 0
                val currentLevel = userStats?.currentLevel ?: levelCalculator.calculateLevel(totalXp)
                val levelName = levelCalculator.getLevelName(currentLevel)
                val xpProgress = levelCalculator.getProgressToNextLevel(totalXp).progressPercent

                // Upcoming bills (top 3, sorted by due date)
                val nextBills = bills
                    .sortedBy { it.dueDate }
                    .take(3)

                // Get bill summary (this is a suspend call, needs to be done separately)
                val billSummary = upcomingBillsRepository.getBillSummary()

                // Get most recent ongoing event
                val activeEventSummary = eventSummaries
                    .filter { it.event.isOngoing }
                    .maxByOrNull { it.event.startDate }
                val hasActiveEvent = activeEventSummary != null

                // Calculate period spending
                val currentCategoryNames = _uiState.value.categoryNames
                val (periodTotal, periodBreakdown) = calculatePeriodSpending(
                    regularTransactions,
                    _uiState.value.selectedSpendingPeriod,
                    currentCategoryNames
                )

                // Find uncategorized transactions (null categoryId or categoryId 0 for "Other")
                // We consider transactions that need user attention for proper categorization
                val uncategorized = transactions.filter { txn ->
                    txn.categoryId == null || txn.categoryId == 0L
                }.sortedByDescending { it.transactionDate }

                _uiState.update {
                    it.copy(
                        totalBalance = totalIncome - totalExpenses - totalSavings,
                        monthlySpent = monthlySpent,
                        monthlyIncome = monthlyIncome,
                        monthlySaved = monthlySaved,
                        recentTransactions = recentTransactions,
                        currentStreak = currentStreak,
                        currentLevel = currentLevel,
                        levelName = levelName,
                        totalXp = totalXp,
                        xpProgress = xpProgress,
                        upcomingBillsSummary = billSummary,
                        nextBills = nextBills,
                        activeEventSummary = activeEventSummary,
                        hasActiveEvent = hasActiveEvent,
                        isLoading = false,
                        allTransactions = regularTransactions,
                        periodSpending = periodTotal,
                        periodCategoryBreakdown = periodBreakdown,
                        uncategorizedCount = uncategorized.size,
                        uncategorizedTransactions = uncategorized
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }

    /**
     * Select a spending period and recalculate breakdown
     */
    fun selectSpendingPeriod(period: SpendingPeriod) {
        _uiState.update { state ->
            val (periodTotal, periodBreakdown) = calculatePeriodSpending(
                state.allTransactions,
                period,
                state.categoryNames
            )
            state.copy(
                selectedSpendingPeriod = period,
                periodSpending = periodTotal,
                periodCategoryBreakdown = periodBreakdown
            )
        }
    }

    /**
     * Get the date range for a spending period (for navigation to period transactions)
     */
    fun getDateRangeForPeriod(period: SpendingPeriod): Pair<Long, Long> {
        val today = LocalDate.now()
        val (startDate, endDate) = when (period) {
            SpendingPeriod.TODAY -> today to today
            SpendingPeriod.THIS_WEEK -> {
                val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                startOfWeek to today
            }
            SpendingPeriod.THIS_MONTH -> today.withDayOfMonth(1) to today
            SpendingPeriod.THIS_YEAR -> today.withDayOfYear(1) to today
        }
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return Pair(startMillis, endMillis)
    }

    /**
     * Calculate spending for a given period
     */
    private fun calculatePeriodSpending(
        transactions: List<Transaction>,
        period: SpendingPeriod,
        categoryNames: Map<Long, Pair<String, String>>
    ): Pair<Double, List<CategorySpendingData>> {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        // Filter transactions by period
        val periodTransactions = transactions.filter { txn ->
            txn.type == TransactionType.DEBIT && when (period) {
                SpendingPeriod.TODAY -> txn.transactionDate.toLocalDate() == today
                SpendingPeriod.THIS_WEEK -> {
                    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val txnDate = txn.transactionDate.toLocalDate()
                    !txnDate.isBefore(startOfWeek) && !txnDate.isAfter(today)
                }
                SpendingPeriod.THIS_MONTH -> {
                    val txnMonth = YearMonth.from(txn.transactionDate)
                    txnMonth == YearMonth.now()
                }
                SpendingPeriod.THIS_YEAR -> {
                    txn.transactionDate.year == today.year
                }
            }
        }

        val totalSpending = periodTransactions.sumOf { it.amount }

        // Group by category
        val categoryBreakdown = periodTransactions
            .groupBy { it.categoryId ?: 0L }
            .map { (categoryId, txns) ->
                val (name, emoji) = categoryNames[categoryId] ?: Pair("Other", "📦")
                val amount = txns.sumOf { it.amount }
                val percentage = if (totalSpending > 0) (amount / totalSpending * 100).toFloat() else 0f

                CategorySpendingData(
                    categoryId = categoryId,
                    categoryName = name,
                    emoji = emoji,
                    amount = amount,
                    percentage = percentage,
                    transactionCount = txns.size
                )
            }
            .sortedByDescending { it.amount }

        return Pair(totalSpending, categoryBreakdown)
    }
}

// Helper data class for combine result
private data class HomeData(
    val transactions: List<Transaction>,
    val userStats: com.fino.app.domain.model.UserStats?,
    val bills: List<UpcomingBill>,
    val eventSummaries: List<EventSummary>
)
