package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for Category Transactions screen
 */
data class CategoryTransactionsUiState(
    val categoryId: Long = 0L,
    val categoryName: String = "",
    val categoryEmoji: String = "",
    val transactions: List<Transaction> = emptyList(),
    val totalAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CategoryTransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L
    private val categoryName: String = savedStateHandle.get<String>("categoryName") ?: ""

    private val _uiState = MutableStateFlow(
        CategoryTransactionsUiState(
            categoryId = categoryId,
            categoryName = categoryName
        )
    )
    val uiState: StateFlow<CategoryTransactionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Load category details
                val category = categoryRepository.getById(categoryId)

                // Collect transactions for this category
                transactionRepository.getByCategoryFlow(categoryId).collect { transactions ->
                    val totalAmount = transactions
                        .filter { it.type == com.fino.app.domain.model.TransactionType.DEBIT }
                        .sumOf { it.amount }

                    _uiState.update {
                        it.copy(
                            categoryName = category?.name ?: categoryName,
                            categoryEmoji = category?.emoji ?: "",
                            transactions = transactions,
                            totalAmount = totalAmount,
                            transactionCount = transactions.size,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load transactions: ${e.message}"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
