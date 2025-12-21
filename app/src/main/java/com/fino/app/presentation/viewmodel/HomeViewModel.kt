package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.EventRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.data.repository.UpcomingBillsRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.BillSummary
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
import java.time.YearMonth
import javax.inject.Inject

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
    val isLoading: Boolean = true
) {
    val hasUrgentBills: Boolean
        get() = (upcomingBillsSummary?.overdueCount ?: 0) > 0 ||
                (upcomingBillsSummary?.dueTodayCount ?: 0) > 0
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userStatsRepository: UserStatsRepository,
    private val upcomingBillsRepository: UpcomingBillsRepository,
    private val eventRepository: EventRepository,
    private val levelCalculator: LevelCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
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

                // Filter transactions for current month
                val monthlyTransactions = transactions.filter { txn ->
                    val txnMonth = YearMonth.from(txn.transactionDate)
                    txnMonth == currentMonth
                }

                // Calculate totals
                val totalIncome = transactions
                    .filter { it.type == TransactionType.CREDIT }
                    .sumOf { it.amount }

                val totalExpenses = transactions
                    .filter { it.type == TransactionType.DEBIT }
                    .sumOf { it.amount }

                val totalSavings = transactions
                    .filter { it.type == TransactionType.SAVINGS }
                    .sumOf { it.amount }

                val monthlyIncome = monthlyTransactions
                    .filter { it.type == TransactionType.CREDIT }
                    .sumOf { it.amount }

                val monthlySpent = monthlyTransactions
                    .filter { it.type == TransactionType.DEBIT }
                    .sumOf { it.amount }

                val monthlySaved = monthlyTransactions
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
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }
}

// Helper data class for combine result
private data class HomeData(
    val transactions: List<Transaction>,
    val userStats: com.fino.app.domain.model.UserStats?,
    val bills: List<UpcomingBill>,
    val eventSummaries: List<EventSummary>
)
