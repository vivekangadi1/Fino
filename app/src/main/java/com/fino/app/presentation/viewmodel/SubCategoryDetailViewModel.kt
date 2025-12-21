package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.EventSubCategoryRepository
import com.fino.app.data.repository.EventVendorRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.EventSubCategory
import com.fino.app.domain.model.EventVendor
import com.fino.app.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Sub-Category Detail screen
 */
data class SubCategoryDetailUiState(
    val subCategory: EventSubCategory? = null,
    val transactions: List<Transaction> = emptyList(),
    val vendors: List<EventVendor> = emptyList(),
    val vendorNames: Map<Long, String> = emptyMap(),
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val isLoading: Boolean = true,
    val showBudgetDialog: Boolean = false,
    val budgetInput: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Sub-Category Detail screen
 * Shows transactions under a sub-category with budget management
 */
@HiltViewModel
class SubCategoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val subCategoryRepository: EventSubCategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val vendorRepository: EventVendorRepository
) : ViewModel() {

    private val eventId: Long = checkNotNull(savedStateHandle["eventId"])
    private val subCategoryId: Long = checkNotNull(savedStateHandle["subCategoryId"])

    private val _uiState = MutableStateFlow(SubCategoryDetailUiState())
    val uiState: StateFlow<SubCategoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadSubCategoryDetails()
    }

    private fun loadSubCategoryDetails() {
        viewModelScope.launch {
            combine(
                subCategoryRepository.getByIdFlow(subCategoryId),
                transactionRepository.getByEventSubCategoryFlow(subCategoryId),
                vendorRepository.getBySubCategoryIdFlow(subCategoryId)
            ) { subCategory, transactions, vendors ->
                Triple(subCategory, transactions, vendors)
            }.collect { (subCategory, transactions, vendors) ->
                val vendorNames = vendors.associate { it.id to it.name }
                val totalPaid = transactions
                    .filter { it.paymentStatus == com.fino.app.domain.model.PaymentStatus.PAID }
                    .sumOf { it.amount }
                val totalPending = transactions
                    .filter { it.paymentStatus != com.fino.app.domain.model.PaymentStatus.PAID }
                    .sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        subCategory = subCategory,
                        transactions = transactions.sortedByDescending { t -> t.transactionDate },
                        vendors = vendors,
                        vendorNames = vendorNames,
                        totalPaid = totalPaid,
                        totalPending = totalPending,
                        isLoading = false,
                        budgetInput = subCategory?.budgetAmount?.toLong()?.toString() ?: ""
                    )
                }
            }
        }
    }

    fun showBudgetDialog() {
        _uiState.update {
            it.copy(
                showBudgetDialog = true,
                budgetInput = it.subCategory?.budgetAmount?.toLong()?.toString() ?: ""
            )
        }
    }

    fun hideBudgetDialog() {
        _uiState.update { it.copy(showBudgetDialog = false, error = null) }
    }

    fun setBudgetInput(input: String) {
        _uiState.update { it.copy(budgetInput = input, error = null) }
    }

    fun saveBudget() {
        val state = _uiState.value
        val subCategory = state.subCategory ?: return

        val budgetAmount = if (state.budgetInput.isBlank()) {
            null
        } else {
            state.budgetInput.toDoubleOrNull()
        }

        if (state.budgetInput.isNotBlank() && budgetAmount == null) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val updated = subCategory.copy(budgetAmount = budgetAmount)
                subCategoryRepository.update(updated)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showBudgetDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save budget"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
