package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType
import com.fino.app.gamification.StreakTracker
import com.fino.app.gamification.XpAction
import com.fino.app.gamification.XpCalculator
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
 * UI state for Add Transaction screen
 */
data class AddTransactionUiState(
    val amount: String = "",
    val merchant: String = "",
    val selectedCategoryId: Long? = null,
    val isExpense: Boolean = true,
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userStatsRepository: UserStatsRepository,
    private val streakTracker: StreakTracker,
    private val xpCalculator: XpCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllActive().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun setAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun setMerchant(merchant: String) {
        _uiState.update { it.copy(merchant = merchant) }
    }

    fun selectCategory(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun setIsExpense(isExpense: Boolean) {
        _uiState.update { it.copy(isExpense = isExpense) }
    }

    fun saveTransaction() {
        val state = _uiState.value

        // Validation
        if (state.amount.isBlank()) {
            _uiState.update { it.copy(error = "Please enter an amount") }
            return
        }

        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        if (state.selectedCategoryId == null) {
            _uiState.update { it.copy(error = "Please select a category") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                // Create transaction
                val transaction = Transaction(
                    amount = amount,
                    type = if (state.isExpense) TransactionType.DEBIT else TransactionType.CREDIT,
                    merchantName = state.merchant.ifBlank { "Manual Entry" },
                    categoryId = state.selectedCategoryId,
                    transactionDate = LocalDateTime.now(),
                    source = TransactionSource.MANUAL,
                    needsReview = false,
                    parsedConfidence = 1.0f
                )

                // Save to database
                transactionRepository.insert(transaction)

                // Update gamification stats
                userStatsRepository.incrementTransactionCount()
                streakTracker.recordActivity(LocalDate.now())

                // Award XP for manual transaction
                val xp = xpCalculator.getXpForAction(XpAction.ADD_MANUAL_TRANSACTION)
                userStatsRepository.addXp(xp)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save transaction"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
