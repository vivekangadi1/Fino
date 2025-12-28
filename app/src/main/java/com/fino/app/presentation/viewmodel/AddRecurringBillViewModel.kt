package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
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
 * UI State for adding/editing a recurring bill
 */
data class AddRecurringBillUiState(
    val isEditMode: Boolean = false,
    val editingRuleId: Long? = null,
    val originalRule: RecurringRule? = null,
    val merchantName: String = "",
    val amount: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val dayOfPeriod: Int = 1,
    val specificDueDate: LocalDate? = null, // For ONE_TIME bills
    val selectedCategoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Add/Edit Recurring Bill screen
 */
@HiltViewModel
class AddRecurringBillViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recurringRuleRepository: RecurringRuleRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // Get ruleId from navigation arguments (null if adding new)
    private val ruleId: Long? = savedStateHandle.get<Long>("ruleId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(
        AddRecurringBillUiState(
            isEditMode = ruleId != null,
            editingRuleId = ruleId
        )
    )
    val uiState: StateFlow<AddRecurringBillUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        if (ruleId != null) {
            loadRuleForEditing(ruleId)
        }
    }

    /**
     * Load existing rule for editing
     */
    private fun loadRuleForEditing(ruleId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val rule = recurringRuleRepository.getById(ruleId)
                if (rule != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            originalRule = rule,
                            merchantName = rule.merchantPattern,
                            amount = rule.expectedAmount.toLong().toString(),
                            frequency = rule.frequency,
                            dayOfPeriod = rule.dayOfPeriod ?: 1,
                            selectedCategoryId = if (rule.categoryId > 0) rule.categoryId else null
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Bill not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
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
     * Update specific due date (for ONE_TIME bills)
     */
    fun updateSpecificDueDate(date: LocalDate) {
        _uiState.update { it.copy(specificDueDate = date, error = null) }
    }

    /**
     * Select a category
     */
    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    /**
     * Save the recurring bill (create new or update existing)
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

            // Validate due date for ONE_TIME bills
            if (state.frequency == RecurringFrequency.ONE_TIME && state.specificDueDate == null) {
                _uiState.update { it.copy(error = "Please select a due date") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                // Calculate next expected date based on frequency and day of period
                val nextExpected = if (state.frequency == RecurringFrequency.ONE_TIME) {
                    state.specificDueDate!!
                } else {
                    calculateNextExpectedDate(state.frequency, state.dayOfPeriod)
                }

                if (state.isEditMode && state.originalRule != null) {
                    // Update existing rule
                    val updatedRule = state.originalRule.copy(
                        merchantPattern = state.merchantName,
                        categoryId = state.selectedCategoryId ?: 0L,
                        expectedAmount = amount,
                        frequency = state.frequency,
                        dayOfPeriod = state.dayOfPeriod,
                        nextExpected = nextExpected
                    )
                    recurringRuleRepository.update(updatedRule)
                } else {
                    // Create new rule
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
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    /**
     * Delete the recurring bill (only in edit mode)
     */
    fun deleteBill() {
        viewModelScope.launch {
            val state = _uiState.value
            val rule = state.originalRule ?: return@launch

            _uiState.update { it.copy(isDeleting = true, error = null) }

            try {
                recurringRuleRepository.delete(rule)
                _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeleting = false, error = e.message) }
            }
        }
    }

    /**
     * Deactivate rule instead of deleting (keeps history)
     */
    fun deactivateBill() {
        viewModelScope.launch {
            val state = _uiState.value
            val rule = state.originalRule ?: return@launch

            _uiState.update { it.copy(isDeleting = true, error = null) }

            try {
                val deactivatedRule = rule.copy(isActive = false)
                recurringRuleRepository.update(deactivatedRule)
                _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeleting = false, error = e.message) }
            }
        }
    }

    /**
     * Calculate the next expected date based on frequency and day of period
     */
    private fun calculateNextExpectedDate(frequency: RecurringFrequency, dayOfPeriod: Int): LocalDate {
        val today = LocalDate.now()

        return when (frequency) {
            RecurringFrequency.ONE_TIME -> {
                // Should not reach here as we use specificDueDate for ONE_TIME
                today
            }
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
