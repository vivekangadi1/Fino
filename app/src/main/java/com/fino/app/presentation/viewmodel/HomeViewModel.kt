package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
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
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userStatsRepository: UserStatsRepository,
    private val levelCalculator: LevelCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                userStatsRepository.getUserStatsFlow()
            ) { transactions, userStats ->
                Pair(transactions, userStats)
            }.collect { (transactions, userStats) ->
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
