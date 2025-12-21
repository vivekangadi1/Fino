package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.EventRepository
import com.fino.app.data.repository.EventTypeRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.CategorySpending
import com.fino.app.domain.model.DailySpending
import com.fino.app.domain.model.Event
import com.fino.app.domain.model.EventBudgetStatus
import com.fino.app.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * UI State for Event Detail screen
 */
data class EventDetailUiState(
    val event: Event? = null,
    val eventTypeName: String = "",
    val transactions: List<Transaction> = emptyList(),
    val budgetStatus: EventBudgetStatus? = null,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val dailySpending: List<DailySpending> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Event Detail screen
 */
@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val eventTypeRepository: EventTypeRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val eventId: Long = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        loadEventDetails()
    }

    /**
     * Load event details and related data
     */
    private fun loadEventDetails() {
        viewModelScope.launch {
            try {
                combine(
                    eventRepository.getByIdFlow(eventId),
                    transactionRepository.getAllTransactionsFlow(),
                    eventTypeRepository.getAllActiveFlow(),
                    categoryRepository.getAllActive()
                ) { event, transactions, eventTypes, categories ->
                    EventDetailData(event, transactions, eventTypes, categories)
                }.collect { data ->
                    val event = data.event
                    if (event == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Event not found"
                            )
                        }
                        return@collect
                    }

                    // Get event type name
                    val eventTypeName = data.eventTypes.find { it.id == event.eventTypeId }?.name ?: "Unknown"

                    // Filter transactions for this event
                    val eventTransactions = data.transactions.filter { it.eventId == eventId }

                    // Calculate total spent
                    val totalSpent = eventTransactions.sumOf { it.amount }

                    // Calculate budget status
                    val budgetStatus = event.budgetAmount?.let {
                        EventBudgetStatus.calculate(event, totalSpent)
                    }

                    // Create category breakdown
                    val categoryMap = data.categories.associateBy { it.id }
                    val categoryBreakdown = eventTransactions
                        .groupBy { it.categoryId }
                        .map { (categoryId, txns) ->
                            val category = categoryMap[categoryId]
                            val amount = txns.sumOf { it.amount }
                            CategorySpending(
                                categoryId = categoryId,
                                categoryName = category?.name ?: "Unknown",
                                amount = amount,
                                transactionCount = txns.size,
                                percentage = if (totalSpent > 0) ((amount / totalSpent) * 100).toFloat() else 0f
                            )
                        }
                        .sortedByDescending { it.amount }

                    // Create daily spending breakdown
                    val dailySpending = eventTransactions
                        .groupBy { it.transactionDate.toLocalDate() }
                        .map { (date, txns) ->
                            DailySpending(
                                date = date,
                                amount = txns.sumOf { it.amount },
                                transactionCount = txns.size
                            )
                        }
                        .sortedBy { it.date }

                    _uiState.update {
                        it.copy(
                            event = event,
                            eventTypeName = eventTypeName,
                            transactions = eventTransactions.sortedByDescending { it.transactionDate },
                            budgetStatus = budgetStatus,
                            categoryBreakdown = categoryBreakdown,
                            dailySpending = dailySpending,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load event details: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Data class to hold combined flow results
     */
    private data class EventDetailData(
        val event: Event?,
        val transactions: List<Transaction>,
        val eventTypes: List<com.fino.app.domain.model.EventType>,
        val categories: List<com.fino.app.domain.model.Category>
    )

    /**
     * Complete the event
     */
    fun completeEvent() {
        viewModelScope.launch {
            try {
                eventRepository.completeEvent(eventId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to complete event: ${e.message}")
                }
            }
        }
    }

    /**
     * Delete the event
     */
    fun deleteEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                // First, unlink all transactions from this event
                transactionRepository.unlinkTransactionsFromEvent(eventId)
                // Then delete the event
                eventRepository.deleteById(eventId)
                _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        error = "Failed to delete event: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Remove a transaction from this event
     */
    fun removeTransactionFromEvent(transactionId: Long) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getById(transactionId)
                if (transaction != null) {
                    // Update transaction to remove event association
                    transactionRepository.update(transaction.copy(eventId = null))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to remove transaction: ${e.message}")
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
     * Refresh event details
     */
    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadEventDetails()
    }
}
