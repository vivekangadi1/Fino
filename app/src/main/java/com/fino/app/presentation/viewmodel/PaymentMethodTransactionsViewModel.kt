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

data class PaymentMethodTransactionsUiState(
    val paymentMethod: String = "",
    val bankName: String? = null,
    val cardLastFour: String? = null,
    val title: String = "Transactions",
    val subtitle: String = "",
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class PaymentMethodTransactionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val paymentMethod: String = savedStateHandle.get<String>("method") ?: "UPI"
    private val filter: String = savedStateHandle.get<String>("filter") ?: ""

    private val _uiState = MutableStateFlow(PaymentMethodTransactionsUiState(
        paymentMethod = paymentMethod
    ))
    val uiState: StateFlow<PaymentMethodTransactionsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            transactionRepository.getAllTransactionsFlow().collect { allTransactions ->
                // Filter by payment method and optional bank/card filter
                val filtered = allTransactions.filter { txn ->
                    txn.type == TransactionType.DEBIT && when (paymentMethod.uppercase()) {
                        "UPI" -> txn.paymentMethod == "UPI" &&
                                (filter.isEmpty() || txn.bankName?.equals(filter, ignoreCase = true) == true)
                        "CREDIT_CARD" -> txn.paymentMethod == "CREDIT_CARD" &&
                                (filter.isEmpty() || txn.cardLastFour == filter || txn.bankName?.equals(filter, ignoreCase = true) == true)
                        else -> txn.paymentMethod == null || txn.paymentMethod == "UNKNOWN"
                    }
                }.sortedByDescending { it.transactionDate }

                val totalAmount = filtered.sumOf { it.amount }

                // Determine title and subtitle
                val (title, subtitle) = when (paymentMethod.uppercase()) {
                    "UPI" -> {
                        if (filter.isNotEmpty()) {
                            Pair("$filter UPI", "${filtered.size} transactions")
                        } else {
                            Pair("UPI Transactions", "${filtered.size} transactions")
                        }
                    }
                    "CREDIT_CARD" -> {
                        if (filter.isNotEmpty()) {
                            if (filter.length == 4 && filter.all { it.isDigit() }) {
                                Pair("Card ****$filter", "${filtered.size} transactions")
                            } else {
                                Pair("$filter Credit Card", "${filtered.size} transactions")
                            }
                        } else {
                            Pair("Credit Card Transactions", "${filtered.size} transactions")
                        }
                    }
                    else -> Pair("Other Transactions", "${filtered.size} transactions")
                }

                _uiState.update { state ->
                    state.copy(
                        title = title,
                        subtitle = subtitle,
                        bankName = if (filter.isNotEmpty() && filter.length != 4) filter else null,
                        cardLastFour = if (filter.length == 4 && filter.all { it.isDigit() }) filter else null,
                        transactions = filtered,
                        filteredTransactions = filtered,
                        totalAmount = totalAmount,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun filterByMerchant(query: String) {
        val allTransactions = _uiState.value.transactions
        if (query.isBlank()) {
            _uiState.update { it.copy(filteredTransactions = allTransactions) }
        } else {
            val filtered = allTransactions.filter { txn ->
                txn.merchantName?.contains(query, ignoreCase = true) == true
            }
            _uiState.update { it.copy(filteredTransactions = filtered) }
        }
    }
}
