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
import java.time.LocalDateTime
import javax.inject.Inject

data class AddEditCreditCardUiState(
    val bankName: String = "",
    val cardName: String = "",
    val lastFourDigits: String = "",
    val creditLimit: String = "",
    val billingCycleDay: String = "",
    val dueDateDay: String = "",
    val isEditMode: Boolean = false,
    val editingCardId: Long = 0L,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = bankName.isNotBlank() && lastFourDigits.length == 4
}

@HiltViewModel
class AddEditCreditCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {

    private val cardId: Long = savedStateHandle.get<Long>("cardId") ?: 0L

    private val _uiState = MutableStateFlow(AddEditCreditCardUiState())
    val uiState: StateFlow<AddEditCreditCardUiState> = _uiState.asStateFlow()

    init {
        if (cardId > 0) {
            loadCard(cardId)
        }
    }

    private fun loadCard(id: Long) {
        viewModelScope.launch {
            val card = creditCardRepository.getById(id)
            card?.let { c ->
                _uiState.update {
                    it.copy(
                        bankName = c.bankName,
                        cardName = c.cardName ?: "",
                        lastFourDigits = c.lastFourDigits,
                        creditLimit = c.creditLimit?.toLong()?.toString() ?: "",
                        billingCycleDay = c.billingCycleDay?.toString() ?: "",
                        dueDateDay = c.dueDateDay?.toString() ?: "",
                        isEditMode = true,
                        editingCardId = c.id
                    )
                }
            }
        }
    }

    fun updateBankName(value: String) {
        _uiState.update { it.copy(bankName = value) }
    }

    fun updateCardName(value: String) {
        _uiState.update { it.copy(cardName = value) }
    }

    fun updateLastFourDigits(value: String) {
        _uiState.update { it.copy(lastFourDigits = value) }
    }

    fun updateCreditLimit(value: String) {
        _uiState.update { it.copy(creditLimit = value.filter { c -> c.isDigit() }) }
    }

    fun updateBillingCycleDay(value: String) {
        _uiState.update { it.copy(billingCycleDay = value) }
    }

    fun updateDueDateDay(value: String) {
        _uiState.update { it.copy(dueDateDay = value) }
    }

    fun saveCard() {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val card = CreditCard(
                    id = if (state.isEditMode) state.editingCardId else 0L,
                    bankName = state.bankName.trim(),
                    cardName = state.cardName.trim().takeIf { it.isNotEmpty() },
                    lastFourDigits = state.lastFourDigits,
                    creditLimit = state.creditLimit.toDoubleOrNull(),
                    billingCycleDay = state.billingCycleDay.toIntOrNull(),
                    dueDateDay = state.dueDateDay.toIntOrNull(),
                    isActive = true,
                    createdAt = LocalDateTime.now()
                )

                if (state.isEditMode) {
                    creditCardRepository.update(card)
                } else {
                    creditCardRepository.insert(card)
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteCard() {
        val state = _uiState.value
        if (!state.isEditMode) return

        viewModelScope.launch {
            try {
                val card = creditCardRepository.getById(state.editingCardId)
                card?.let {
                    // Soft delete by marking as inactive
                    creditCardRepository.update(it.copy(isActive = false))
                }
                _uiState.update { it.copy(deleteSuccess = true, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
