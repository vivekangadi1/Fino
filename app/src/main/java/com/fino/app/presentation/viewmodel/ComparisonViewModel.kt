package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ComparisonUiState(
    val isLoading: Boolean = true,
    val currentMonth: YearMonth = YearMonth.now(),
    val previousMonth: YearMonth = YearMonth.now().minusMonths(1),
    val currentMonthLabel: String = "",
    val previousMonthLabel: String = "",
    val comparison: PeriodComparison? = null,
    val error: String? = null
)

@HiltViewModel
class ComparisonViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ComparisonUiState(
            currentMonth = YearMonth.now(),
            previousMonth = YearMonth.now().minusMonths(1),
            currentMonthLabel = formatMonthLabel(YearMonth.now()),
            previousMonthLabel = formatMonthLabel(YearMonth.now().minusMonths(1))
        )
    )
    val uiState: StateFlow<ComparisonUiState> = _uiState.asStateFlow()

    private val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    init {
        loadComparisonData()
    }

    /**
     * Load comparison data for the selected periods
     */
    private fun loadComparisonData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                combine(
                    transactionRepository.getAllTransactionsFlow(),
                    categoryRepository.getAllActive()
                ) { transactions, categories ->
                    Pair(transactions, categories)
                }.collect { (transactions, categories) ->
                    val currentMonth = _uiState.value.currentMonth
                    val previousMonth = _uiState.value.previousMonth

                    // Filter transactions for current period
                    val currentTransactions = transactions.filter { transaction ->
                        val txMonth = YearMonth.from(transaction.transactionDate)
                        txMonth == currentMonth
                    }

                    // Filter transactions for previous period
                    val previousTransactions = transactions.filter { transaction ->
                        val txMonth = YearMonth.from(transaction.transactionDate)
                        txMonth == previousMonth
                    }

                    // Build PeriodData for current period
                    val currentPeriodData = buildPeriodData(
                        yearMonth = currentMonth,
                        transactions = currentTransactions,
                        categories = categories
                    )

                    // Build PeriodData for previous period
                    val previousPeriodData = buildPeriodData(
                        yearMonth = previousMonth,
                        transactions = previousTransactions,
                        categories = categories
                    )

                    // Calculate comparison
                    val comparison = calculatePeriodComparison(
                        current = currentPeriodData,
                        previous = previousPeriodData
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            comparison = comparison,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load comparison: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Build PeriodData from transactions
     */
    private fun buildPeriodData(
        yearMonth: YearMonth,
        transactions: List<Transaction>,
        categories: List<Category>
    ): PeriodData {
        val totalIncome = transactions
            .filter { it.type == TransactionType.CREDIT }
            .sumOf { it.amount }

        val totalExpenses = transactions
            .filter { it.type == TransactionType.DEBIT }
            .sumOf { it.amount }

        val netBalance = totalIncome - totalExpenses

        // Build category breakdown
        val categoryBreakdown = categories.mapNotNull { category ->
            val categoryTransactions = transactions.filter {
                it.categoryId == category.id && it.type == TransactionType.DEBIT
            }

            if (categoryTransactions.isEmpty()) {
                null
            } else {
                val amount = categoryTransactions.sumOf { it.amount }
                val percentage = if (totalExpenses > 0) {
                    ((amount / totalExpenses) * 100).toFloat()
                } else {
                    0f
                }

                CategorySpending(
                    categoryName = category.name,
                    categoryId = category.id,
                    amount = amount,
                    percentage = percentage,
                    transactionCount = categoryTransactions.size
                )
            }
        }

        return PeriodData(
            yearMonth = yearMonth,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netBalance = netBalance,
            transactionCount = transactions.size,
            categoryBreakdown = categoryBreakdown
        )
    }

    /**
     * Change the current period being compared
     */
    fun changeCurrentPeriod(yearMonth: YearMonth) {
        _uiState.update {
            it.copy(
                currentMonth = yearMonth,
                currentMonthLabel = formatMonthLabel(yearMonth)
            )
        }
        loadComparisonData()
    }

    /**
     * Change the previous period being compared
     */
    fun changePreviousPeriod(yearMonth: YearMonth) {
        _uiState.update {
            it.copy(
                previousMonth = yearMonth,
                previousMonthLabel = formatMonthLabel(yearMonth)
            )
        }
        loadComparisonData()
    }

    /**
     * Navigate to next month for current period
     */
    fun navigateCurrentNext() {
        val nextMonth = _uiState.value.currentMonth.plusMonths(1)
        if (nextMonth <= YearMonth.now()) {
            changeCurrentPeriod(nextMonth)
        }
    }

    /**
     * Navigate to previous month for current period
     */
    fun navigateCurrentPrevious() {
        val previousMonth = _uiState.value.currentMonth.minusMonths(1)
        changeCurrentPeriod(previousMonth)
    }

    /**
     * Navigate to next month for previous period
     */
    fun navigatePreviousNext() {
        val nextMonth = _uiState.value.previousMonth.plusMonths(1)
        if (nextMonth < _uiState.value.currentMonth) {
            changePreviousPeriod(nextMonth)
        }
    }

    /**
     * Navigate to previous month for previous period
     */
    fun navigatePreviousPrevious() {
        val previousMonth = _uiState.value.previousMonth.minusMonths(1)
        changePreviousPeriod(previousMonth)
    }

    /**
     * Format YearMonth to display label
     */
    private fun formatMonthLabel(yearMonth: YearMonth): String {
        return yearMonth.format(monthFormatter)
    }
}
