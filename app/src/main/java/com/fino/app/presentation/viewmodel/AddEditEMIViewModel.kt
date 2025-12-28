package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.data.repository.EMIRepository
import com.fino.app.domain.model.CreditCard
import com.fino.app.domain.model.EMI
import com.fino.app.domain.model.EMIStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class AddEditEMIUiState(
    val isEditMode: Boolean = false,
    val creditCards: List<CreditCard> = emptyList(),
    val selectedCardId: Long? = null,
    val description: String = "",
    val merchantName: String = "",
    val originalAmount: String = "",
    val monthlyAmount: String = "",
    val tenure: String = "",
    val paidCount: String = "0",
    val startDate: LocalDate = LocalDate.now(),
    val interestRate: String = "",
    val processingFee: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditEMIViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val emiRepository: EMIRepository,
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {

    private val emiId: Long = savedStateHandle.get<Long>("emiId") ?: 0L
    private val isEditMode = emiId > 0

    private val _uiState = MutableStateFlow(AddEditEMIUiState(isEditMode = isEditMode))
    val uiState: StateFlow<AddEditEMIUiState> = _uiState.asStateFlow()

    init {
        loadCreditCards()
        if (isEditMode) {
            loadEMI()
        }
    }

    private fun loadCreditCards() {
        viewModelScope.launch {
            creditCardRepository.getActiveCardsFlow().collect { cards ->
                _uiState.update { it.copy(creditCards = cards) }
            }
        }
    }

    private fun loadEMI() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val emi = emiRepository.getById(emiId)
                if (emi != null) {
                    _uiState.update {
                        it.copy(
                            selectedCardId = emi.creditCardId,
                            description = emi.description,
                            merchantName = emi.merchantName ?: "",
                            originalAmount = emi.originalAmount.toString(),
                            monthlyAmount = emi.monthlyAmount.toString(),
                            tenure = emi.tenure.toString(),
                            paidCount = emi.paidCount.toString(),
                            startDate = emi.startDate,
                            interestRate = emi.interestRate?.toString() ?: "",
                            processingFee = emi.processingFee?.toString() ?: "",
                            notes = emi.notes ?: "",
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "EMI not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load EMI"
                    )
                }
            }
        }
    }

    fun updateSelectedCard(cardId: Long?) {
        _uiState.update { it.copy(selectedCardId = cardId) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun updateMerchantName(value: String) {
        _uiState.update { it.copy(merchantName = value) }
    }

    fun updateOriginalAmount(value: String) {
        _uiState.update { it.copy(originalAmount = value) }
        // Auto-calculate monthly amount if tenure is set
        calculateMonthlyAmount()
    }

    fun updateMonthlyAmount(value: String) {
        _uiState.update { it.copy(monthlyAmount = value) }
    }

    fun updateTenure(value: String) {
        _uiState.update { it.copy(tenure = value) }
        // Auto-calculate monthly amount if original amount is set
        calculateMonthlyAmount()
    }

    fun updatePaidCount(value: String) {
        _uiState.update { it.copy(paidCount = value) }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun updateInterestRate(value: String) {
        _uiState.update { it.copy(interestRate = value) }
    }

    fun updateProcessingFee(value: String) {
        _uiState.update { it.copy(processingFee = value) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    private fun calculateMonthlyAmount() {
        val state = _uiState.value
        val original = state.originalAmount.toDoubleOrNull() ?: return
        val tenure = state.tenure.toIntOrNull() ?: return
        if (tenure > 0) {
            val monthly = original / tenure
            _uiState.update { it.copy(monthlyAmount = "%.2f".format(monthly)) }
        }
    }

    fun save() {
        val state = _uiState.value

        // Validation
        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Description is required") }
            return
        }
        val originalAmount = state.originalAmount.toDoubleOrNull()
        if (originalAmount == null || originalAmount <= 0) {
            _uiState.update { it.copy(errorMessage = "Valid original amount is required") }
            return
        }
        val monthlyAmount = state.monthlyAmount.toDoubleOrNull()
        if (monthlyAmount == null || monthlyAmount <= 0) {
            _uiState.update { it.copy(errorMessage = "Valid monthly amount is required") }
            return
        }
        val tenure = state.tenure.toIntOrNull()
        if (tenure == null || tenure <= 0) {
            _uiState.update { it.copy(errorMessage = "Valid tenure is required") }
            return
        }
        val paidCount = state.paidCount.toIntOrNull() ?: 0
        if (paidCount > tenure) {
            _uiState.update { it.copy(errorMessage = "Paid count cannot exceed tenure") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val endDate = state.startDate.plusMonths(tenure.toLong())
                val nextDueDate = state.startDate.plusMonths(paidCount.toLong() + 1)

                val emi = EMI(
                    id = if (isEditMode) emiId else 0,
                    creditCardId = state.selectedCardId,
                    description = state.description.trim(),
                    merchantName = state.merchantName.trim().takeIf { it.isNotEmpty() },
                    originalAmount = originalAmount,
                    monthlyAmount = monthlyAmount,
                    tenure = tenure,
                    paidCount = paidCount,
                    startDate = state.startDate,
                    endDate = endDate,
                    nextDueDate = if (nextDueDate.isAfter(endDate)) endDate else nextDueDate,
                    interestRate = state.interestRate.toFloatOrNull(),
                    processingFee = state.processingFee.toDoubleOrNull(),
                    status = if (paidCount >= tenure) EMIStatus.COMPLETED else EMIStatus.ACTIVE,
                    notes = state.notes.trim().takeIf { it.isNotEmpty() },
                    createdAt = LocalDateTime.now()
                )

                if (isEditMode) {
                    emiRepository.update(emi)
                } else {
                    emiRepository.insert(emi)
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Failed to save EMI"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
