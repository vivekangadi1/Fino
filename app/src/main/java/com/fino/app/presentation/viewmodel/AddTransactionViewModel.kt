package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.EventSubCategoryRepository
import com.fino.app.data.repository.EventVendorRepository
import com.fino.app.data.repository.FamilyMemberRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.EventSubCategory
import com.fino.app.domain.model.EventVendor
import com.fino.app.domain.model.FamilyMember
import com.fino.app.domain.model.PaymentMethod
import com.fino.app.domain.model.PaymentStatus
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
 * UI state for Add/Edit Transaction screen
 */
data class AddTransactionUiState(
    val isEditMode: Boolean = false,
    val editingTransactionId: Long? = null,
    val amount: String = "",
    val merchant: String = "",
    val selectedCategoryId: Long? = null,
    val transactionType: TransactionType = TransactionType.DEBIT,
    val selectedPaymentMethod: PaymentMethod? = null,
    val categories: List<Category> = emptyList(),
    val eventId: Long? = null,

    // Event expense fields
    val isEventExpense: Boolean = false,
    val subCategories: List<EventSubCategory> = emptyList(),
    val selectedSubCategoryId: Long? = null,
    val vendors: List<EventVendor> = emptyList(),
    val selectedVendorId: Long? = null,
    val familyMembers: List<FamilyMember> = emptyList(),
    val selectedPaidBy: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val isAdvancePayment: Boolean = false,
    val dueDate: LocalDate? = null,
    val expenseNotes: String = "",

    // Original transaction data for edit mode
    val originalTransaction: Transaction? = null,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userStatsRepository: UserStatsRepository,
    private val streakTracker: StreakTracker,
    private val xpCalculator: XpCalculator,
    private val eventSubCategoryRepository: EventSubCategoryRepository,
    private val eventVendorRepository: EventVendorRepository,
    private val familyMemberRepository: FamilyMemberRepository
) : ViewModel() {

    // Get eventId from navigation arguments (null if adding standalone transaction)
    private val eventId: Long? = savedStateHandle.get<Long>("eventId")?.takeIf { it > 0 }

    // Get transactionId from navigation arguments (null if adding new transaction)
    private val transactionId: Long? = savedStateHandle.get<Long>("transactionId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(
        AddTransactionUiState(
            eventId = eventId,
            isEventExpense = eventId != null,
            isEditMode = transactionId != null,
            editingTransactionId = transactionId
        )
    )
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        if (eventId != null) {
            loadEventData()
        }
        if (transactionId != null) {
            loadTransactionForEditing(transactionId)
        }
    }

    /**
     * Load existing transaction for editing
     */
    private fun loadTransactionForEditing(transactionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val transaction = transactionRepository.getById(transactionId)
                if (transaction != null) {
                    // If transaction has an eventId but we don't have it from nav args, load event data
                    val txEventId = transaction.eventId
                    if (txEventId != null && eventId == null) {
                        loadEventDataForEventId(txEventId)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            editingTransactionId = transactionId,
                            originalTransaction = transaction,
                            amount = transaction.amount.toLong().toString(),
                            merchant = transaction.merchantName,
                            selectedCategoryId = transaction.categoryId,
                            transactionType = transaction.type,
                            selectedPaymentMethod = PaymentMethod.fromString(transaction.paymentMethod),
                            eventId = transaction.eventId ?: eventId,
                            isEventExpense = transaction.eventId != null || eventId != null,
                            selectedSubCategoryId = transaction.eventSubCategoryId,
                            selectedVendorId = transaction.eventVendorId,
                            selectedPaidBy = transaction.paidBy,
                            paymentStatus = transaction.paymentStatus,
                            isAdvancePayment = transaction.isAdvancePayment,
                            dueDate = transaction.dueDate,
                            expenseNotes = transaction.expenseNotes ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Transaction not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load transaction") }
            }
        }
    }

    /**
     * Load event data for a specific event ID (used when editing transaction with eventId)
     */
    private fun loadEventDataForEventId(eventId: Long) {
        viewModelScope.launch {
            eventSubCategoryRepository.getByEventIdFlow(eventId).collect { subCategories ->
                _uiState.update { it.copy(subCategories = subCategories) }
            }
        }

        viewModelScope.launch {
            eventVendorRepository.getByEventIdFlow(eventId).collect { vendors ->
                _uiState.update { it.copy(vendors = vendors) }
            }
        }

        viewModelScope.launch {
            familyMemberRepository.getAllFlow().collect { members ->
                _uiState.update { it.copy(familyMembers = members) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllActive().collect { categories ->
                _uiState.update { state ->
                    // For event expenses, auto-select "Other" category (or first available)
                    val autoSelectedCategoryId = if (state.isEventExpense && state.selectedCategoryId == null) {
                        categories.find { it.name.equals("Other", ignoreCase = true) }?.id
                            ?: categories.firstOrNull()?.id
                    } else {
                        state.selectedCategoryId
                    }
                    state.copy(
                        categories = categories,
                        selectedCategoryId = autoSelectedCategoryId
                    )
                }
            }
        }
    }

    private fun loadEventData() {
        val eid = eventId ?: return

        viewModelScope.launch {
            // Load sub-categories for this event
            eventSubCategoryRepository.getByEventIdFlow(eid).collect { subCategories ->
                _uiState.update { it.copy(subCategories = subCategories) }
            }
        }

        viewModelScope.launch {
            // Load all vendors for this event
            eventVendorRepository.getByEventIdFlow(eid).collect { vendors ->
                _uiState.update { it.copy(vendors = vendors) }
            }
        }

        viewModelScope.launch {
            // Load family members
            familyMemberRepository.getAllFlow().collect { members ->
                val defaultMember = members.find { it.isDefault }
                _uiState.update {
                    it.copy(
                        familyMembers = members,
                        selectedPaidBy = it.selectedPaidBy ?: defaultMember?.name
                    )
                }
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

    fun setTransactionType(type: TransactionType) {
        _uiState.update { it.copy(transactionType = type) }
    }

    fun selectPaymentMethod(paymentMethod: PaymentMethod?) {
        _uiState.update { it.copy(selectedPaymentMethod = paymentMethod) }
    }

    // Event expense field setters
    fun selectSubCategory(subCategoryId: Long?) {
        _uiState.update {
            it.copy(
                selectedSubCategoryId = subCategoryId,
                // Clear vendor if sub-category changes (vendor is filtered by sub-category)
                selectedVendorId = if (subCategoryId != it.selectedSubCategoryId) null else it.selectedVendorId
            )
        }
    }

    fun selectVendor(vendorId: Long?) {
        _uiState.update { it.copy(selectedVendorId = vendorId) }
        // If vendor is selected, auto-fill merchant name from vendor
        if (vendorId != null) {
            val vendor = _uiState.value.vendors.find { it.id == vendorId }
            vendor?.let { v ->
                _uiState.update { it.copy(merchant = v.name) }
            }
        }
    }

    fun setPaidBy(paidBy: String?) {
        _uiState.update { it.copy(selectedPaidBy = paidBy) }
    }

    fun setPaymentStatus(status: PaymentStatus) {
        _uiState.update {
            it.copy(
                paymentStatus = status,
                // Clear due date if status is PAID
                dueDate = if (status == PaymentStatus.PAID) null else it.dueDate
            )
        }
    }

    fun setIsAdvancePayment(isAdvance: Boolean) {
        _uiState.update {
            it.copy(
                isAdvancePayment = isAdvance,
                // If advance payment, set status to PARTIAL by default
                paymentStatus = if (isAdvance && it.paymentStatus == PaymentStatus.PAID)
                    PaymentStatus.PARTIAL else it.paymentStatus
            )
        }
    }

    fun setDueDate(date: LocalDate?) {
        _uiState.update { it.copy(dueDate = date) }
    }

    fun setExpenseNotes(notes: String) {
        _uiState.update { it.copy(expenseNotes = notes) }
    }

    /**
     * Get vendors filtered by selected sub-category (or all if no sub-category selected)
     */
    fun getFilteredVendors(): List<EventVendor> {
        val state = _uiState.value
        return if (state.selectedSubCategoryId != null) {
            state.vendors.filter { it.subCategoryId == state.selectedSubCategoryId }
        } else {
            state.vendors
        }
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
                // Determine merchant name - use vendor name if selected, otherwise user input
                val merchantName = if (state.selectedVendorId != null) {
                    state.vendors.find { it.id == state.selectedVendorId }?.name
                        ?: state.merchant.ifBlank { "Manual Entry" }
                } else {
                    state.merchant.ifBlank { "Manual Entry" }
                }

                // Determine the eventId to use
                val transactionEventId = state.eventId ?: eventId

                if (state.isEditMode && state.originalTransaction != null) {
                    // Update existing transaction
                    val updatedTransaction = state.originalTransaction.copy(
                        amount = amount,
                        type = state.transactionType,
                        merchantName = merchantName,
                        categoryId = state.selectedCategoryId,
                        paymentMethod = state.selectedPaymentMethod?.value,
                        eventId = transactionEventId,
                        eventSubCategoryId = state.selectedSubCategoryId,
                        eventVendorId = state.selectedVendorId,
                        paidBy = state.selectedPaidBy,
                        isAdvancePayment = state.isAdvancePayment,
                        dueDate = state.dueDate,
                        expenseNotes = state.expenseNotes.ifBlank { null },
                        paymentStatus = state.paymentStatus
                    )
                    transactionRepository.update(updatedTransaction)
                } else {
                    // Create new transaction
                    val transaction = Transaction(
                        amount = amount,
                        type = state.transactionType,
                        merchantName = merchantName,
                        categoryId = state.selectedCategoryId,
                        transactionDate = LocalDateTime.now(),
                        source = TransactionSource.MANUAL,
                        needsReview = false,
                        parsedConfidence = 1.0f,
                        paymentMethod = state.selectedPaymentMethod?.value,
                        eventId = transactionEventId,
                        eventSubCategoryId = state.selectedSubCategoryId,
                        eventVendorId = state.selectedVendorId,
                        paidBy = state.selectedPaidBy,
                        isAdvancePayment = state.isAdvancePayment,
                        dueDate = state.dueDate,
                        expenseNotes = state.expenseNotes.ifBlank { null },
                        paymentStatus = state.paymentStatus
                    )
                    transactionRepository.insert(transaction)

                    // Update gamification stats (only for new transactions)
                    userStatsRepository.incrementTransactionCount()
                    streakTracker.recordActivity(LocalDate.now())

                    // Award XP for manual transaction
                    val xp = xpCalculator.getXpForAction(XpAction.ADD_MANUAL_TRANSACTION)
                    userStatsRepository.addXp(xp)
                }

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

    /**
     * Delete the transaction (only available in edit mode)
     */
    fun deleteTransaction() {
        val state = _uiState.value
        val transaction = state.originalTransaction ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                transactionRepository.delete(transaction)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        deleteSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to delete transaction"
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
