package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.domain.model.CreditCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class EditCreditCardBillUiState(
    val isLoading: Boolean = true,
    val card: CreditCard? = null,
    val amount: String = "",
    val dueDate: LocalDate? = null,
    val isPaid: Boolean = false,
    val paidAmount: String = "",
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class EditCreditCardBillViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {

    private val cardId: Long = savedStateHandle["cardId"] ?: 0L

    private val _uiState = MutableStateFlow(EditCreditCardBillUiState())
    val uiState: StateFlow<EditCreditCardBillUiState> = _uiState.asStateFlow()

    init {
        loadCard()
    }

    private fun loadCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val card = creditCardRepository.getById(cardId)
                if (card != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            card = card,
                            amount = card.effectiveDueAmount.toLong().toString(),
                            dueDate = card.effectiveDueDate,
                            isPaid = card.isPaid,
                            paidAmount = card.paidAmount?.toLong()?.toString() ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Card not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateDueDate(date: LocalDate) {
        _uiState.update { it.copy(dueDate = date) }
    }

    fun updateIsPaid(isPaid: Boolean) {
        _uiState.update { it.copy(isPaid = isPaid) }
    }

    fun updatePaidAmount(amount: String) {
        _uiState.update { it.copy(paidAmount = amount) }
    }

    fun saveBill() {
        val state = _uiState.value
        val card = state.card ?: return

        // Validate amount
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        viewModelScope.launch {
            try {
                // Update bill details if changed
                val hasAmountChanged = amount != card.previousDue
                val hasDateChanged = state.dueDate != card.previousDueDate

                if (hasAmountChanged || hasDateChanged) {
                    creditCardRepository.updateBillDetails(
                        cardId = cardId,
                        amount = if (hasAmountChanged) amount else null,
                        dueDate = if (hasDateChanged) state.dueDate else null
                    )
                }

                // Update payment status
                if (state.isPaid != card.isPaid) {
                    if (state.isPaid) {
                        val paidAmount = state.paidAmount.toDoubleOrNull() ?: amount
                        creditCardRepository.markAsPaid(cardId, paidAmount)
                    } else {
                        creditCardRepository.markAsUnpaid(cardId)
                    }
                }

                _uiState.update { it.copy(saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun resetBillStatus() {
        viewModelScope.launch {
            try {
                creditCardRepository.resetBillStatus(cardId)
                loadCard() // Reload to reflect changes
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
