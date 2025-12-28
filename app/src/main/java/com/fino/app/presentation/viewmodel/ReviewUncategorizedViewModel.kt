package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.MerchantMappingRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.MerchantMapping
import com.fino.app.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class ReviewUncategorizedUiState(
    val uncategorizedTransactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val categorizeSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReviewUncategorizedViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val merchantMappingRepository: MerchantMappingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUncategorizedUiState())
    val uiState: StateFlow<ReviewUncategorizedUiState> = _uiState.asStateFlow()

    // Cache of merchant name -> count of other transactions
    private val merchantTransactionCounts = mutableMapOf<String, Int>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { transactions, categories ->
                Pair(transactions, categories)
            }.collect { (transactions, categories) ->
                // Filter uncategorized (null or 0 categoryId)
                val uncategorized = transactions.filter { txn ->
                    txn.categoryId == null || txn.categoryId == 0L
                }.sortedByDescending { it.transactionDate }

                // Calculate merchant counts
                merchantTransactionCounts.clear()
                uncategorized.groupBy { it.merchantName.lowercase() }
                    .forEach { (merchant, txns) ->
                        merchantTransactionCounts[merchant] = txns.size - 1 // Exclude current
                    }

                _uiState.update {
                    it.copy(
                        uncategorizedTransactions = uncategorized,
                        categories = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Get count of other uncategorized transactions from the same merchant
     */
    fun getOtherTransactionsCount(merchantName: String): Int {
        return merchantTransactionCounts[merchantName.lowercase()] ?: 0
    }

    /**
     * Categorize a transaction and optionally create a merchant mapping
     */
    fun categorizeTransaction(
        transaction: Transaction,
        categoryId: Long,
        applyToAllFromMerchant: Boolean
    ) {
        viewModelScope.launch {
            try {
                if (applyToAllFromMerchant) {
                    // Categorize all transactions from this merchant
                    transactionRepository.categorizeByMerchant(
                        merchantName = transaction.merchantName,
                        categoryId = categoryId
                    )

                    // Create or update merchant mapping
                    val existingMapping = merchantMappingRepository.findByRawName(
                        transaction.merchantName.uppercase()
                    )

                    if (existingMapping != null) {
                        // Update existing mapping
                        merchantMappingRepository.updateMapping(
                            existingMapping.copy(
                                categoryId = categoryId,
                                confidence = 1.0f, // User-confirmed
                                matchCount = existingMapping.matchCount + 1,
                                lastUsedAt = LocalDateTime.now()
                            )
                        )
                    } else {
                        // Create new mapping
                        merchantMappingRepository.insertMapping(
                            MerchantMapping(
                                rawMerchantName = transaction.merchantName.uppercase(),
                                normalizedName = transaction.merchantNormalized ?: transaction.merchantName,
                                categoryId = categoryId,
                                confidence = 1.0f,
                                matchCount = 1,
                                isFuzzyMatch = false,
                                createdAt = LocalDateTime.now(),
                                lastUsedAt = LocalDateTime.now()
                            )
                        )
                    }
                } else {
                    // Just categorize this single transaction
                    transactionRepository.categorize(
                        transactionId = transaction.id,
                        categoryId = categoryId
                    )
                }

                _uiState.update { it.copy(categorizeSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(categorizeSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
