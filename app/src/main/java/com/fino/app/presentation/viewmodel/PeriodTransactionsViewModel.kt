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
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * UI state for Period Transactions screen
 */
data class PeriodTransactionsUiState(
    val periodLabel: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val transactions: List<Transaction> = emptyList(),
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PeriodTransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val startDate: Long = savedStateHandle.get<Long>("startDate") ?: 0L
    private val endDate: Long = savedStateHandle.get<Long>("endDate") ?: 0L
    private val periodLabel: String = (savedStateHandle.get<String>("periodLabel") ?: "").replace("_", " ")

    private val _uiState = MutableStateFlow(
        PeriodTransactionsUiState(
            startDate = startDate,
            endDate = endDate,
            periodLabel = periodLabel
        )
    )
    val uiState: StateFlow<PeriodTransactionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Collect all transactions and filter by date range
                transactionRepository.getAllTransactionsFlow().collect { allTransactions ->
                    // Filter by date range and DEBIT type
                    val periodTransactions = allTransactions.filter { txn ->
                        txn.type == TransactionType.DEBIT && isWithinDateRange(txn)
                    }.sortedByDescending { it.transactionDate }

                    val totalAmount = periodTransactions.sumOf { it.amount }

                    _uiState.update {
                        it.copy(
                            transactions = periodTransactions,
                            totalAmount = totalAmount,
                            transactionCount = periodTransactions.size,
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

    private fun isWithinDateRange(transaction: Transaction): Boolean {
        val txnMillis = transaction.transactionDate
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return txnMillis in startDate..endDate
    }

    fun refresh() {
        loadData()
    }
}
