package com.fino.app.presentation.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
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
import java.util.Locale
import javax.inject.Inject

data class NewMerchantRow(
    val merchantKey: String,
    val displayName: String,
    val firstSeen: LocalDate,
    val categoryName: String,
    val amount: Double,
    val count: Int
)

data class NewMerchantsDetailUiState(
    val periodLabel: String = "",
    val merchantCount: Int = 0,
    val totalSpent: Double = 0.0,
    val rows: List<NewMerchantRow> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class NewMerchantsDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewMerchantsDetailUiState())
    val uiState: StateFlow<NewMerchantsDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { transactions, categories ->
                val month = YearMonth.now()
                val start = month.atDay(1)
                val end = month.atEndOfMonth()
                val catMap = categories.associateBy { it.id }
                val debits = transactions.filter { it.type == TransactionType.DEBIT }

                val firstSeenMap = mutableMapOf<String, LocalDate>()
                val earliestTxn = mutableMapOf<String, com.fino.app.domain.model.Transaction>()
                debits.forEach { txn ->
                    val key = normalize(txn.merchantName)
                    if (key.isBlank()) return@forEach
                    val date = txn.transactionDate.toLocalDate()
                    val existing = firstSeenMap[key]
                    if (existing == null || date.isBefore(existing)) {
                        firstSeenMap[key] = date
                        earliestTxn[key] = txn
                    }
                }

                val newKeys = firstSeenMap.filter { (_, firstSeen) ->
                    !firstSeen.isBefore(start) && !firstSeen.isAfter(end)
                }.keys

                val byMerchant = debits.groupBy { normalize(it.merchantName) }

                val rows = newKeys.mapNotNull { key ->
                    val txns = byMerchant[key].orEmpty()
                        .filter {
                            val d = it.transactionDate.toLocalDate()
                            !d.isBefore(start) && !d.isAfter(end)
                        }
                    if (txns.isEmpty()) return@mapNotNull null
                    val sample = earliestTxn[key] ?: txns.first()
                    val category = sample.categoryId?.let { catMap[it]?.name } ?: "Uncategorized"
                    NewMerchantRow(
                        merchantKey = key,
                        displayName = sample.merchantName,
                        firstSeen = firstSeenMap[key]!!,
                        categoryName = category,
                        amount = txns.sumOf { it.amount },
                        count = txns.size
                    )
                }.sortedByDescending { it.amount }

                NewMerchantsDetailUiState(
                    periodLabel = month.atDay(1).format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                    merchantCount = rows.size,
                    totalSpent = rows.sumOf { it.amount },
                    rows = rows,
                    isLoading = false
                )
            }.collect { s -> _uiState.update { s } }
        }
    }

    private fun normalize(raw: String): String {
        return raw.trim().lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
