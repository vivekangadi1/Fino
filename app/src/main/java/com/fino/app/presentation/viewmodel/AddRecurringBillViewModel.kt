package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.domain.model.RecurringRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UI State for adding a recurring bill
 */
data class AddRecurringBillUiState(
    val merchantName: String = "",
    val amount: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val dayOfPeriod: Int = 1,
    val selectedCategoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Add Recurring Bill screen
 */
@HiltViewModel
class AddRecurringBillViewModel @Inject constructor(
    private val recurringRuleRepository: RecurringRuleRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecurringBillUiState())
    val uiState: StateFlow<AddRecurringBillUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Load categories for selection
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllActive().collect { categories ->
                // Filter to only parent categories (no parentId)
                val parentCategories = categories.filter { it.parentId == null }
                _uiState.update { it.copy(categories = parentCategories) }
            }
        }
    }

    /**
     * Update merchant name
     */
    fun updateMerchantName(name: String) {
        _uiState.update { it.copy(merchantName = name, error = null) }
    }

    /**
     * Update amount
     */
    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount, error = null) }
    }

    /**
     * Update frequency
     */
    fun updateFrequency(frequency: RecurringFrequency) {
        _uiState.update { it.copy(frequency = frequency) }
    }

    /**
     * Update day of period
     */
    fun updateDayOfPeriod(day: Int) {
        _uiState.update { it.copy(dayOfPeriod = day.coerceIn(1, 31)) }
    }

    /**
     * Select a category
     */
    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    /**
     * Save the recurring bill
     */
    fun saveBill() {
        viewModelScope.launch {
            val state = _uiState.value

            // Validate merchant name
            if (state.merchantName.isBlank()) {
                _uiState.update { it.copy(error = "Merchant name is required") }
                return@launch
            }

            // Validate and parse amount
            val amount = state.amount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                _uiState.update { it.copy(error = "Please enter a valid amount") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                // Calculate next expected date based on frequency and day of period
                val nextExpected = calculateNextExpectedDate(state.frequency, state.dayOfPeriod)

                val rule = RecurringRule(
                    id = 0L,
                    merchantPattern = state.merchantName,
                    categoryId = state.selectedCategoryId ?: 0L,
                    expectedAmount = amount,
                    amountVariance = 0.1f,
                    frequency = state.frequency,
                    dayOfPeriod = state.dayOfPeriod,
                    lastOccurrence = null,
                    nextExpected = nextExpected,
                    occurrenceCount = 0,
                    isActive = true,
                    isUserConfirmed = true,
                    createdAt = LocalDateTime.now()
                )

                recurringRuleRepository.insert(rule)

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    /**
     * Calculate the next expected date based on frequency and day of period
     */
    private fun calculateNextExpectedDate(frequency: RecurringFrequency, dayOfPeriod: Int): LocalDate {
        val today = LocalDate.now()

        return when (frequency) {
            RecurringFrequency.WEEKLY -> {
                // dayOfPeriod is 1-7 (Monday to Sunday)
                val currentDayOfWeek = today.dayOfWeek.value
                val daysUntilNext = if (dayOfPeriod > currentDayOfWeek) {
                    dayOfPeriod - currentDayOfWeek
                } else {
                    7 - (currentDayOfWeek - dayOfPeriod)
                }
                today.plusDays(daysUntilNext.toLong())
            }
            RecurringFrequency.MONTHLY -> {
                val targetDay = dayOfPeriod.coerceAtMost(today.lengthOfMonth())
                if (today.dayOfMonth < targetDay) {
                    today.withDayOfMonth(targetDay)
                } else {
                    val nextMonth = today.plusMonths(1)
                    val nextTargetDay = dayOfPeriod.coerceAtMost(nextMonth.lengthOfMonth())
                    nextMonth.withDayOfMonth(nextTargetDay)
                }
            }
            RecurringFrequency.YEARLY -> {
                // For yearly, use current month and provided day
                val targetDay = dayOfPeriod.coerceAtMost(today.lengthOfMonth())
                if (today.dayOfMonth < targetDay) {
                    today.withDayOfMonth(targetDay)
                } else {
                    today.plusYears(1).withDayOfMonth(
                        dayOfPeriod.coerceAtMost(today.plusYears(1).lengthOfMonth())
                    )
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Reset the form
     */
    fun resetForm() {
        _uiState.update {
            AddRecurringBillUiState(categories = it.categories)
        }
    }
}
