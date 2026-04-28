package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.EventRepository
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
import javax.inject.Inject

enum class ActivityFilter { ALL, OUTGOING, INCOMING, NEEDS_REVIEW, CARDS }

data class ActivityUiState(
    val isLoading: Boolean = true,
    val filter: ActivityFilter = ActivityFilter.ALL,
    val grouped: List<Pair<LocalDate, List<Transaction>>> = emptyList(),
    val categoryNames: Map<Long, Pair<String, String>> = emptyMap(),
    val eventNames: Map<Long, String> = emptyMap(),
    val monthlyOutgoing: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyTransactionCount: Int = 0
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    private var allTxns: List<Transaction> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive(),
                eventRepository.getAllActiveEventsFlow()
            ) { txns, cats, events ->
                Triple(
                    txns,
                    cats.associate { it.id to Pair(it.name, it.emoji) },
                    events.associate { it.id to it.name }
                )
            }.collect { (txns, catNames, eventNames) ->
                allTxns = txns
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        grouped = group(txns, state.filter),
                        categoryNames = catNames,
                        eventNames = eventNames,
                        monthlyOutgoing = monthSum(txns, TransactionType.DEBIT),
                        monthlyIncome = monthSum(txns, TransactionType.CREDIT),
                        monthlyTransactionCount = monthCount(txns)
                    )
                }
            }
        }
    }

    fun setFilter(filter: ActivityFilter) {
        _uiState.update { state ->
            state.copy(filter = filter, grouped = group(allTxns, filter))
        }
    }

    private fun group(
        txns: List<Transaction>,
        filter: ActivityFilter
    ): List<Pair<LocalDate, List<Transaction>>> {
        val filtered = when (filter) {
            ActivityFilter.ALL -> txns
            ActivityFilter.OUTGOING -> txns.filter { it.type == TransactionType.DEBIT }
            ActivityFilter.INCOMING -> txns.filter { it.type == TransactionType.CREDIT }
            ActivityFilter.NEEDS_REVIEW -> txns.filter { it.needsReview }
            ActivityFilter.CARDS -> txns.filter {
                it.paymentMethod.equals("CREDIT_CARD", ignoreCase = true) || it.creditCardId != null
            }
        }
        return filtered
            .groupBy { it.transactionDate.toLocalDate() }
            .toSortedMap(compareByDescending { it })
            .map { (d, list) -> d to list.sortedByDescending { it.transactionDate } }
    }

    private fun monthSum(txns: List<Transaction>, type: TransactionType): Double {
        val ym = YearMonth.now()
        return txns.asSequence()
            .filter { it.type == type }
            .filter { YearMonth.from(it.transactionDate) == ym }
            .sumOf { it.amount }
    }

    private fun monthCount(txns: List<Transaction>): Int {
        val ym = YearMonth.now()
        return txns.count { YearMonth.from(it.transactionDate) == ym }
    }
}
