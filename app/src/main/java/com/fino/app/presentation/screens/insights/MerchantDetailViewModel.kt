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
import java.net.URLDecoder
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import javax.inject.Inject

data class MerchantMonthlyBar(
    val label: String,
    val amount: Double,
    val normalized: Float
)

data class MerchantVariantRow(
    val rawName: String,
    val amount: Double,
    val count: Int
)

data class MerchantDetailUiState(
    val merchantKey: String = "",
    val displayName: String = "",
    val periodTotal: Double = 0.0,
    val transactionCount: Int = 0,
    val monthlyBars: List<MerchantMonthlyBar> = emptyList(),
    val variants: List<MerchantVariantRow> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MerchantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val merchantKeyRaw: String = savedStateHandle["merchantKey"] ?: ""
    private val merchantKey: String = try {
        URLDecoder.decode(merchantKeyRaw, "UTF-8").lowercase(Locale.ROOT)
    } catch (e: Exception) {
        merchantKeyRaw.lowercase(Locale.ROOT)
    }

    private val _uiState = MutableStateFlow(MerchantDetailUiState(merchantKey = merchantKey))
    val uiState: StateFlow<MerchantDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { transactions, _ ->
                val matching = transactions.filter {
                    it.type == TransactionType.DEBIT && normalize(it.merchantName) == merchantKey
                }
                val display = matching.firstOrNull()?.merchantName
                    ?: merchantKey.replaceFirstChar { it.uppercase() }

                val now = YearMonth.now()
                val last6 = (5 downTo 0).map { offset -> now.minusMonths(offset.toLong()) }
                val byMonth = last6.map { ym ->
                    val sum = matching
                        .filter { YearMonth.from(it.transactionDate) == ym }
                        .sumOf { it.amount }
                    ym to sum
                }
                val max = byMonth.maxOfOrNull { it.second } ?: 0.0
                val bars = byMonth.map { (ym, amt) ->
                    MerchantMonthlyBar(
                        label = ym.monthValue.toString().padStart(2, '0'),
                        amount = amt,
                        normalized = if (max > 0) (amt / max).toFloat() else 0f
                    )
                }

                val variants = matching.groupBy { it.merchantName.trim() }
                    .map { (raw, txns) ->
                        MerchantVariantRow(
                            rawName = raw,
                            amount = txns.sumOf { it.amount },
                            count = txns.size
                        )
                    }.sortedByDescending { it.amount }

                val recent = matching.sortedByDescending { it.transactionDate }.take(20)

                MerchantDetailUiState(
                    merchantKey = merchantKey,
                    displayName = display,
                    periodTotal = matching.sumOf { it.amount },
                    transactionCount = matching.size,
                    monthlyBars = bars,
                    variants = variants,
                    recentTransactions = recent,
                    isLoading = false
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    private fun normalize(raw: String): String {
        return raw.trim().lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
