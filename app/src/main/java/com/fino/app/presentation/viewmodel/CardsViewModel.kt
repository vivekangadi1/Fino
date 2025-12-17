package com.fino.app.presentation.viewmodel

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
import javax.inject.Inject

/**
 * UI state for Cards screen
 */
data class CardsUiState(
    val cards: List<CreditCard> = emptyList(),
    val totalCreditLimit: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            creditCardRepository.getActiveCardsFlow().collect { cards ->
                val totalCreditLimit = cards.sumOf { it.creditLimit ?: 0.0 }
                val totalOutstanding = cards.sumOf { card ->
                    card.currentUnbilled + card.previousDue
                }

                _uiState.update {
                    it.copy(
                        cards = cards,
                        totalCreditLimit = totalCreditLimit,
                        totalOutstanding = totalOutstanding,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadCards()
    }
}
