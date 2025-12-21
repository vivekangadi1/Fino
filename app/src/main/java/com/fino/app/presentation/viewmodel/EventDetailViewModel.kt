package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.EventAnalyticsRepository
import com.fino.app.data.repository.EventRepository
import com.fino.app.data.repository.EventSubCategoryRepository
import com.fino.app.data.repository.EventTypeRepository
import com.fino.app.data.repository.EventVendorRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.CategorySpending
import com.fino.app.domain.model.DailySpending
import com.fino.app.domain.model.Event
import com.fino.app.domain.model.EventBudgetStatus
import com.fino.app.domain.model.EventSubCategory
import com.fino.app.domain.model.EventSubCategorySummary
import com.fino.app.domain.model.EventVendor
import com.fino.app.domain.model.EventVendorSummary
import com.fino.app.domain.model.PaymentStatus
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

    // Enhanced event expense tracking
    val subCategorySummaries: List<EventSubCategorySummary> = emptyList(),
    val vendorSummaries: List<EventVendorSummary> = emptyList(),
    val pendingPayments: List<Transaction> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalQuoted: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val vendorNames: Map<Long, String> = emptyMap(),
    val subCategoryNames: Map<Long, String> = emptyMap(),

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
    private val categoryRepository: CategoryRepository,
    private val eventSubCategoryRepository: EventSubCategoryRepository,
    private val eventVendorRepository: EventVendorRepository,
    private val eventAnalyticsRepository: EventAnalyticsRepository
) : ViewModel() {

    private val eventId: Long = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        loadEventDetails()
        loadEventExpenseData()
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
     * Load event expense tracking data (sub-categories, vendors, pending payments)
     */
    private fun loadEventExpenseData() {
        // Combine sub-categories and transactions to recalculate summaries when either changes
        viewModelScope.launch {
            combine(
                eventSubCategoryRepository.getByEventIdFlow(eventId),
                transactionRepository.getTransactionsForEventFlow(eventId)
            ) { subCategories, _ ->
                subCategories  // transactions triggers recalculation but value not needed
            }.collect { subCategories ->
                val subCategoryNames = subCategories.associate { it.id to it.name }
                _uiState.update { it.copy(subCategoryNames = subCategoryNames) }
                // Recalculate summaries whenever sub-categories OR transactions change
                calculateSubCategorySummaries(subCategories)
            }
        }

        // Combine vendors and transactions to recalculate summaries when either changes
        viewModelScope.launch {
            combine(
                eventVendorRepository.getByEventIdFlow(eventId),
                transactionRepository.getTransactionsForEventFlow(eventId)
            ) { vendors, _ ->
                vendors  // transactions triggers recalculation but value not needed
            }.collect { vendors ->
                val vendorNames = vendors.associate { it.id to it.name }
                _uiState.update { it.copy(vendorNames = vendorNames) }
                // Recalculate summaries whenever vendors OR transactions change
                calculateVendorSummaries(vendors)
            }
        }

        // Load pending payments (already reactive to transaction changes)
        viewModelScope.launch {
            transactionRepository.getTransactionsForEventFlow(eventId).collect { transactions ->
                val pendingPayments = transactions.filter {
                    it.paymentStatus != PaymentStatus.PAID
                }.sortedWith(
                    compareBy<Transaction> { it.isOverdue }.reversed()
                        .thenBy { it.dueDate }
                )

                // Calculate totals
                val totalPaid = transactions
                    .filter { it.paymentStatus == PaymentStatus.PAID }
                    .sumOf { it.amount }
                val totalPending = transactions
                    .filter { it.paymentStatus != PaymentStatus.PAID }
                    .sumOf { it.amount }

                _uiState.update {
                    it.copy(
                        pendingPayments = pendingPayments,
                        totalPaid = totalPaid,
                        totalPending = totalPending
                    )
                }
            }
        }

        // Load total budget (sum of sub-category budgets + event budget)
        viewModelScope.launch {
            val subCategoryBudget = eventSubCategoryRepository.getTotalBudgetByEventId(eventId)
            val vendorQuoted = eventVendorRepository.getTotalQuotedByEventId(eventId)
            val event = eventRepository.getById(eventId)
            val eventBudget = event?.budgetAmount ?: 0.0

            _uiState.update {
                it.copy(
                    totalBudget = if (subCategoryBudget > 0) subCategoryBudget else eventBudget,
                    totalQuoted = vendorQuoted
                )
            }
        }
    }

    /**
     * Calculate sub-category summaries with spending data
     */
    private fun calculateSubCategorySummaries(subCategories: List<EventSubCategory>) {
        viewModelScope.launch {
            val summaries = subCategories.map { subCategory ->
                val paidAmount = transactionRepository.getPaidAmountForSubCategory(subCategory.id)
                val pendingAmount = transactionRepository.getPendingAmountForSubCategory(subCategory.id)
                val transactions = transactionRepository.getByEventSubCategory(subCategory.id)
                val transactionCount = transactions.size
                val vendorCount = eventVendorRepository.getCountBySubCategoryId(subCategory.id)
                val quotedAmount = eventVendorRepository.getTotalQuotedBySubCategoryId(subCategory.id)

                EventSubCategorySummary(
                    subCategory = subCategory,
                    budgetAmount = subCategory.budgetAmount ?: 0.0,
                    quotedAmount = quotedAmount,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount,
                    transactionCount = transactionCount,
                    vendorCount = vendorCount
                )
            }

            _uiState.update { it.copy(subCategorySummaries = summaries) }
        }
    }

    /**
     * Calculate vendor summaries with payment data
     */
    private fun calculateVendorSummaries(vendors: List<EventVendor>) {
        viewModelScope.launch {
            val subCategoryNames = _uiState.value.subCategoryNames

            val summaries = vendors.map { vendor ->
                val paidAmount = transactionRepository.getPaidAmountForVendor(vendor.id)
                val pendingAmount = transactionRepository.getPendingAmountForVendor(vendor.id)
                val transactions = transactionRepository.getByEventVendor(vendor.id)
                val paymentCount = transactions.size
                val quotedAmount = vendor.quotedAmount ?: 0.0

                EventVendorSummary(
                    vendor = vendor,
                    subCategoryName = vendor.subCategoryId?.let { subCategoryNames[it] },
                    quotedAmount = quotedAmount,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount,
                    paymentCount = paymentCount,
                    payments = transactions
                )
            }

            _uiState.update { it.copy(vendorSummaries = summaries) }
        }
    }

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
