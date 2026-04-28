package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.data.repository.EMIRepository
import com.fino.app.data.repository.LoanRepository
import com.fino.app.domain.model.CreditCard
import com.fino.app.domain.model.EMI
import com.fino.app.domain.model.Loan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unified EMI row — combines standalone EMIs and bank loans for the Cards screen.
 */
data class EMIRowData(
    val id: String,
    val name: String,
    val sub: String,
    val monthlyAmount: Double,
    val progress: Float,
    val remainingAmount: Double
)

/**
 * UI state for Cards screen
 */
data class CardsUiState(
    val cards: List<CreditCard> = emptyList(),
    val totalCreditLimit: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val activeEMIs: List<EMIRowData> = emptyList(),
    val totalEMICount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val emiRepository: EMIRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                creditCardRepository.getActiveCardsFlow(),
                emiRepository.getAllEMIsFlow(),
                loanRepository.getAllLoansFlow()
            ) { cards, emis, loans ->
                val totalCreditLimit = cards.sumOf { it.creditLimit ?: 0.0 }
                val totalOutstanding = cards.sumOf { card ->
                    card.currentUnbilled + card.previousDue
                }

                val loanRows = loans.map { it.toRowData() }
                val emiRows = emis.map { it.toRowData() }
                val active = (loanRows + emiRows).filter { it.progress < 1f }
                val all = loanRows + emiRows

                CardsUiState(
                    cards = cards,
                    totalCreditLimit = totalCreditLimit,
                    totalOutstanding = totalOutstanding,
                    activeEMIs = active,
                    totalEMICount = all.size,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }

    private fun EMI.toRowData(): EMIRowData = EMIRowData(
        id = "emi-$id",
        name = description,
        sub = listOfNotNull(merchantName, "$paidCount of $tenure").joinToString(" · "),
        monthlyAmount = monthlyAmount,
        progress = progressPercent,
        remainingAmount = remainingAmount
    )

    private fun Loan.toRowData(): EMIRowData = EMIRowData(
        id = "loan-$id",
        name = description,
        sub = "$bankName · $paidCount of $tenure",
        monthlyAmount = monthlyEMI,
        progress = progressPercent,
        remainingAmount = remainingAmount
    )
}
