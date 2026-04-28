package com.fino.app.presentation.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.domain.model.RecurringRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class SubscriptionRow(
    val rule: RecurringRule,
    val displayName: String,
    val categoryName: String,
    val monthlyAmount: Double,
    val annualAmount: Double,
    val nextChargeDate: LocalDate?,
    val isDormant: Boolean
)

data class SubscriptionsDetailUiState(
    val rows: List<SubscriptionRow> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val annualTotal: Double = 0.0,
    val dormantCount: Int = 0,
    val dormantSavingsAnnual: Double = 0.0,
    val upcomingChargeDates: List<LocalDate> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SubscriptionsDetailViewModel @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionsDetailUiState())
    val uiState: StateFlow<SubscriptionsDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                recurringRuleRepository.getActiveRulesFlow(),
                categoryRepository.getAllActive()
            ) { rules, categories ->
                val catMap = categories.associateBy { it.id }
                val today = LocalDate.now()
                val subs = rules.filter { it.frequency != RecurringFrequency.ONE_TIME }
                val rows = subs.map { rule ->
                    val monthly = when (rule.frequency) {
                        RecurringFrequency.WEEKLY -> rule.expectedAmount * 4.33
                        RecurringFrequency.MONTHLY -> rule.expectedAmount
                        RecurringFrequency.YEARLY -> rule.expectedAmount / 12
                        RecurringFrequency.ONE_TIME -> rule.expectedAmount
                    }
                    val isDormant = rule.lastOccurrence
                        ?.let { ChronoUnit.DAYS.between(it, today) > 60 } ?: false
                    SubscriptionRow(
                        rule = rule,
                        displayName = rule.merchantPattern.replace("%", "").trim()
                            .replaceFirstChar { it.uppercase() }
                            .ifBlank { "Subscription" },
                        categoryName = catMap[rule.categoryId]?.name ?: "Uncategorized",
                        monthlyAmount = monthly,
                        annualAmount = monthly * 12,
                        nextChargeDate = rule.nextExpected,
                        isDormant = isDormant
                    )
                }.sortedBy { it.nextChargeDate ?: LocalDate.MAX }
                val monthlyTotal = rows.sumOf { it.monthlyAmount }
                val annualTotal = monthlyTotal * 12
                val dormantSavings = rows.filter { it.isDormant }.sumOf { it.annualAmount }
                val upcoming = rows.mapNotNull { it.nextChargeDate }
                    .filter { !it.isBefore(today) && ChronoUnit.DAYS.between(today, it) <= 14 }
                    .sorted()
                SubscriptionsDetailUiState(
                    rows = rows,
                    monthlyTotal = monthlyTotal,
                    annualTotal = annualTotal,
                    dormantCount = rows.count { it.isDormant },
                    dormantSavingsAnnual = dormantSavings,
                    upcomingChargeDates = upcoming,
                    isLoading = false
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun cancelSubscription(ruleId: Long) {
        viewModelScope.launch {
            recurringRuleRepository.deactivate(ruleId)
        }
    }
}
