package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

/**
 * UI state for Type Transactions screen (Expenses/Income/Savings)
 */
data class TypeTransactionsUiState(
    val transactionType: TransactionType = TransactionType.DEBIT,
    val label: String = "",
    val transactions: List<Transaction> = emptyList(),
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class TypeTransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionTypeString: String = savedStateHandle.get<String>("transactionType") ?: "DEBIT"
    private val label: String = savedStateHandle.get<String>("label") ?: ""

    private val transactionType: TransactionType = try {
        TransactionType.valueOf(transactionTypeString)
    } catch (e: Exception) {
        TransactionType.DEBIT
    }

    private val _uiState = MutableStateFlow(
        TypeTransactionsUiState(
            transactionType = transactionType,
            label = label
        )
    )
    val uiState: StateFlow<TypeTransactionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val currentMonth = YearMonth.now()

                // Collect all transactions and filter by type and current month
                transactionRepository.getAllTransactionsFlow().collect { allTransactions ->
                    val typeTransactions = allTransactions.filter { txn ->
                        txn.type == transactionType &&
                        YearMonth.from(txn.transactionDate) == currentMonth
                    }.sortedByDescending { it.transactionDate }

                    val totalAmount = typeTransactions.sumOf { it.amount }

                    _uiState.update {
                        it.copy(
                            transactions = typeTransactions,
                            totalAmount = totalAmount,
                            transactionCount = typeTransactions.size,
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

    fun refresh() {
        loadData()
    }
}
