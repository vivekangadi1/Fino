package com.fino.app.presentation.screens.insights

import androidx.lifecycle.SavedStateHandle
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
import javax.inject.Inject

data class DayCategoryStack(
    val categoryName: String,
    val amount: Double,
    val fraction: Float
)

data class DayDetailUiState(
    val date: LocalDate = LocalDate.now(),
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val stacks: List<DayCategoryStack> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val epochDay: Long = savedStateHandle["epochDay"] ?: LocalDate.now().toEpochDay()
    private val date: LocalDate = LocalDate.ofEpochDay(epochDay)

    private val _uiState = MutableStateFlow(DayDetailUiState(date = date))
    val uiState: StateFlow<DayDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { transactions, categories ->
                val catMap = categories.associateBy { it.id }
                val dayTxns = transactions.filter { it.transactionDate.toLocalDate() == date }
                val debits = dayTxns.filter { it.type == TransactionType.DEBIT }
                val total = debits.sumOf { it.amount }
                val stacks = if (total > 0) {
                    debits.groupBy { it.categoryId }
                        .map { (catId, txns) ->
                            val sum = txns.sumOf { it.amount }
                            DayCategoryStack(
                                categoryName = catId?.let { catMap[it]?.name } ?: "Uncategorized",
                                amount = sum,
                                fraction = (sum / total).toFloat()
                            )
                        }
                        .sortedByDescending { it.amount }
                } else emptyList()
                DayDetailUiState(
                    date = date,
                    totalSpent = total,
                    totalIncome = dayTxns.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount },
                    stacks = stacks,
                    transactions = dayTxns.sortedByDescending { it.transactionDate },
                    isLoading = false
                )
            }.collect { s -> _uiState.update { s } }
        }
    }
}
