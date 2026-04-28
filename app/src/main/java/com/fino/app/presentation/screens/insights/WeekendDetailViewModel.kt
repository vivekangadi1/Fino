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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class DayOfWeekBar(
    val day: DayOfWeek,
    val shortLabel: String,
    val total: Double,
    val normalized: Float,
    val isWeekend: Boolean
)

data class CategoryMultiplierRow(
    val categoryName: String,
    val weekdayAvg: Double,
    val weekendAvg: Double,
    val multiplier: Float
)

data class WeekendDetailUiState(
    val periodLabel: String = "",
    val weekdayAvg: Double = 0.0,
    val weekendAvg: Double = 0.0,
    val ratio: Float = 0f,
    val weekdayTotal: Double = 0.0,
    val weekendTotal: Double = 0.0,
    val dayBars: List<DayOfWeekBar> = emptyList(),
    val topCategoryMultipliers: List<CategoryMultiplierRow> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WeekendDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeekendDetailUiState())
    val uiState: StateFlow<WeekendDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { transactions, categories ->
                val month = YearMonth.now()
                val debits = transactions.filter {
                    it.type == TransactionType.DEBIT &&
                        YearMonth.from(it.transactionDate) == month
                }
                val catMap = categories.associateBy { it.id }

                val byDow = debits.groupBy { it.transactionDate.dayOfWeek }
                    .mapValues { (_, txns) -> txns.sumOf { it.amount } }
                val max = byDow.values.maxOrNull() ?: 0.0
                val bars = DayOfWeek.values().map { dow ->
                    val total = byDow[dow] ?: 0.0
                    DayOfWeekBar(
                        day = dow,
                        shortLabel = when (dow) {
                            DayOfWeek.MONDAY -> "M"
                            DayOfWeek.TUESDAY -> "T"
                            DayOfWeek.WEDNESDAY -> "W"
                            DayOfWeek.THURSDAY -> "T"
                            DayOfWeek.FRIDAY -> "F"
                            DayOfWeek.SATURDAY -> "S"
                            DayOfWeek.SUNDAY -> "S"
                        },
                        total = total,
                        normalized = if (max > 0) (total / max).toFloat() else 0f,
                        isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
                    )
                }

                val byDate = debits.groupBy { it.transactionDate.toLocalDate() }
                val weekendDays = byDate.filterKeys {
                    it.dayOfWeek == DayOfWeek.SATURDAY || it.dayOfWeek == DayOfWeek.SUNDAY
                }
                val weekdayDays = byDate.filterKeys {
                    it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY
                }
                val weekendAvg = if (weekendDays.isNotEmpty())
                    weekendDays.values.map { it.sumOf { t -> t.amount } }.average() else 0.0
                val weekdayAvg = if (weekdayDays.isNotEmpty())
                    weekdayDays.values.map { it.sumOf { t -> t.amount } }.average() else 0.0
                val ratio = if (weekdayAvg > 0) (weekendAvg / weekdayAvg).toFloat() else 0f

                val weekendTxns = debits.filter {
                    val dow = it.transactionDate.dayOfWeek
                    dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
                }
                val weekdayTxns = debits.filter {
                    val dow = it.transactionDate.dayOfWeek
                    dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
                }

                val weekendDayCount = weekendDays.size.coerceAtLeast(1)
                val weekdayDayCount = weekdayDays.size.coerceAtLeast(1)

                val catMultipliers = debits.groupBy { it.categoryId }.mapNotNull { (catId, txns) ->
                    val weAvg = txns.filter {
                        val dow = it.transactionDate.dayOfWeek
                        dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
                    }.sumOf { it.amount } / weekendDayCount
                    val wdAvg = txns.filter {
                        val dow = it.transactionDate.dayOfWeek
                        dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
                    }.sumOf { it.amount } / weekdayDayCount
                    if (wdAvg <= 0.0 || weAvg <= 0.0) return@mapNotNull null
                    val mult = (weAvg / wdAvg).toFloat()
                    if (mult < 1.2f) return@mapNotNull null
                    CategoryMultiplierRow(
                        categoryName = catId?.let { catMap[it]?.name } ?: "Uncategorized",
                        weekdayAvg = wdAvg,
                        weekendAvg = weAvg,
                        multiplier = mult
                    )
                }.sortedByDescending { it.multiplier }.take(5)

                WeekendDetailUiState(
                    periodLabel = month.atDay(1).format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                    weekdayAvg = weekdayAvg,
                    weekendAvg = weekendAvg,
                    ratio = ratio,
                    weekdayTotal = weekdayTxns.sumOf { it.amount },
                    weekendTotal = weekendTxns.sumOf { it.amount },
                    dayBars = bars,
                    topCategoryMultipliers = catMultipliers,
                    isLoading = false
                )
            }.collect { s -> _uiState.update { s } }
        }
    }
}
