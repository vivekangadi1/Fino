package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.BudgetRepository
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Budget
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.util.Locale
import javax.inject.Inject

/**
 * 6-month trend mini-bar for a category drill-in.
 */
data class CategoryMonthlyBar(
    val label: String,
    val amount: Double,
    val normalized: Float
)

/**
 * Top-merchant aggregate row for a category drill-in.
 * merchantKey is the normalized key used to navigate to Merchant Detail.
 */
data class CategoryTopMerchantRow(
    val merchantKey: String,
    val displayName: String,
    val amount: Double,
    val count: Int
)

/**
 * UI state for Category Transactions screen
 */
data class CategoryTransactionsUiState(
    val categoryId: Long = 0L,
    val categoryName: String = "",
    val categoryEmoji: String = "",
    val transactions: List<Transaction> = emptyList(),
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val monthlyBars: List<CategoryMonthlyBar> = emptyList(),
    val topMerchants: List<CategoryTopMerchantRow> = emptyList(),
    val monthlyBudget: Double? = null,
    val thisMonthSpent: Double = 0.0,
    val budgetPercent: Float = 0f,
    val isOverBudget: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CategoryTransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L
    private val categoryName: String = savedStateHandle.get<String>("categoryName") ?: ""

    private val _uiState = MutableStateFlow(
        CategoryTransactionsUiState(
            categoryId = categoryId,
            categoryName = categoryName
        )
    )
    val uiState: StateFlow<CategoryTransactionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val category = categoryRepository.getById(categoryId)
                val thisMonth = YearMonth.now()
                val budget: Budget? = budgetRepository.getBudgetsForMonth(thisMonth)
                    .firstOrNull { it.categoryId == categoryId }

                transactionRepository.getByCategoryFlow(categoryId).collect { transactions ->
                    val debits = transactions.filter { it.type == TransactionType.DEBIT }
                    val totalAmount = debits.sumOf { it.amount }

                    val monthlyBars = buildMonthlyBars(debits, thisMonth)

                    val topMerchants = debits.groupBy { normalize(it.merchantName) }
                        .filterKeys { it.isNotBlank() }
                        .map { (key, txns) ->
                            CategoryTopMerchantRow(
                                merchantKey = key,
                                displayName = txns.first().merchantName,
                                amount = txns.sumOf { it.amount },
                                count = txns.size
                            )
                        }.sortedByDescending { it.amount }.take(8)

                    val thisMonthSpent = debits
                        .filter { YearMonth.from(it.transactionDate) == thisMonth }
                        .sumOf { it.amount }
                    val budgetAmount = budget?.monthlyLimit
                    val budgetPct = if (budgetAmount != null && budgetAmount > 0) {
                        (thisMonthSpent / budgetAmount).toFloat()
                    } else 0f
                    val isOver = budgetAmount != null && thisMonthSpent > budgetAmount

                    _uiState.update {
                        it.copy(
                            categoryName = category?.name ?: categoryName,
                            categoryEmoji = category?.emoji ?: "",
                            transactions = transactions,
                            totalAmount = totalAmount,
                            transactionCount = transactions.size,
                            monthlyBars = monthlyBars,
                            topMerchants = topMerchants,
                            monthlyBudget = budgetAmount,
                            thisMonthSpent = thisMonthSpent,
                            budgetPercent = budgetPct,
                            isOverBudget = isOver,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load transactions: ${e.message}"
                    )
                }
            }
        }
    }

    private fun buildMonthlyBars(debits: List<Transaction>, thisMonth: YearMonth): List<CategoryMonthlyBar> {
        val monthFmt = java.time.format.DateTimeFormatter.ofPattern("MMM")
        val months = (5 downTo 0).map { thisMonth.minusMonths(it.toLong()) }
        val totals = months.map { ym ->
            val sum = debits.filter { YearMonth.from(it.transactionDate) == ym }.sumOf { it.amount }
            ym to sum
        }
        val max = totals.maxOfOrNull { it.second } ?: 0.0
        return totals.map { (ym, amt) ->
            CategoryMonthlyBar(
                label = ym.atDay(1).format(monthFmt),
                amount = amt,
                normalized = if (max > 0) (amt / max).toFloat() else 0f
            )
        }
    }

    private fun normalize(raw: String): String {
        return raw.trim().lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun refresh() {
        loadData()
    }
}
