package com.fino.app.presentation.screens.insights

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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CompareCategoryRow(
    val categoryName: String,
    val currentAmount: Double,
    val previousAmount: Double,
    val delta: Double,
    val deltaPercent: Float
)

data class CompareDetailUiState(
    val currentLabel: String = "",
    val previousLabel: String = "",
    val currentTotal: Double = 0.0,
    val previousTotal: Double = 0.0,
    val totalDelta: Double = 0.0,
    val totalDeltaPercent: Float = 0f,
    val rows: List<CompareCategoryRow> = emptyList(),
    val maxAmount: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CompareDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareDetailUiState())
    val uiState: StateFlow<CompareDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { transactions, categories ->
                val currentMonth = YearMonth.now()
                val previousMonth = currentMonth.minusMonths(1)
                val fmt = DateTimeFormatter.ofPattern("MMM yyyy")

                val catMap = categories.associateBy { it.id }
                val debits = transactions.filter { it.type == TransactionType.DEBIT }

                val current = debits.filter { YearMonth.from(it.transactionDate) == currentMonth }
                val previous = debits.filter { YearMonth.from(it.transactionDate) == previousMonth }

                val currentByCat = current.groupBy { it.categoryId }
                    .mapValues { (_, txns) -> txns.sumOf { it.amount } }
                val previousByCat = previous.groupBy { it.categoryId }
                    .mapValues { (_, txns) -> txns.sumOf { it.amount } }

                val allCatIds = (currentByCat.keys + previousByCat.keys)
                val rows = allCatIds.map { catId ->
                    val cur = currentByCat[catId] ?: 0.0
                    val prev = previousByCat[catId] ?: 0.0
                    val delta = cur - prev
                    val pct = if (prev > 0) ((delta / prev) * 100).toFloat() else 0f
                    CompareCategoryRow(
                        categoryName = catId?.let { catMap[it]?.name } ?: "Uncategorized",
                        currentAmount = cur,
                        previousAmount = prev,
                        delta = delta,
                        deltaPercent = pct
                    )
                }.sortedByDescending { kotlin.math.abs(it.delta) }.take(10)

                val curTotal = current.sumOf { it.amount }
                val prevTotal = previous.sumOf { it.amount }
                val totalDelta = curTotal - prevTotal
                val totalDeltaPct = if (prevTotal > 0) ((totalDelta / prevTotal) * 100).toFloat() else 0f

                val maxAmount = (rows.maxOfOrNull { kotlin.math.max(it.currentAmount, it.previousAmount) } ?: 0.0)

                CompareDetailUiState(
                    currentLabel = currentMonth.atDay(1).format(fmt),
                    previousLabel = previousMonth.atDay(1).format(fmt),
                    currentTotal = curTotal,
                    previousTotal = prevTotal,
                    totalDelta = totalDelta,
                    totalDeltaPercent = totalDeltaPct,
                    rows = rows,
                    maxAmount = maxAmount,
                    isLoading = false
                )
            }.collect { s -> _uiState.update { s } }
        }
    }
}
