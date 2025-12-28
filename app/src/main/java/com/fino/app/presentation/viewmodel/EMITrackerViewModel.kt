package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.data.repository.EMIRepository
import com.fino.app.data.repository.LoanRepository
import com.fino.app.domain.model.CreditCard
import com.fino.app.domain.model.EMI
import com.fino.app.domain.model.EMIStatus
import com.fino.app.domain.model.Loan
import com.fino.app.domain.model.LoanStatus
import com.fino.app.domain.model.LoanType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardWithEMIs(
    val card: CreditCard,
    val emis: List<EMI>,
    val totalMonthlyAmount: Double
)

data class LoanGroup(
    val type: LoanType,
    val loans: List<Loan>,
    val totalMonthlyEMI: Double
)

data class EMITrackerUiState(
    val activeEMIs: List<EMI> = emptyList(),
    val completedEMIs: List<EMI> = emptyList(),
    val cardsWithEMIs: List<CardWithEMIs> = emptyList(),
    val standaloneEMIs: List<EMI> = emptyList(),
    val activeLoans: List<Loan> = emptyList(),
    val completedLoans: List<Loan> = emptyList(),
    val loanGroups: List<LoanGroup> = emptyList(),
    val totalMonthlyEMI: Double = 0.0,
    val totalMonthlyLoanEMI: Double = 0.0,
    val totalMonthlyObligations: Double = 0.0,
    val selectedTab: Int = 0, // 0 = EMIs, 1 = Loans
    val isLoading: Boolean = true
)

@HiltViewModel
class EMITrackerViewModel @Inject constructor(
    private val emiRepository: EMIRepository,
    private val loanRepository: LoanRepository,
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EMITrackerUiState())
    val uiState: StateFlow<EMITrackerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                emiRepository.getAllEMIsFlow(),
                loanRepository.getAllLoansFlow(),
                creditCardRepository.getActiveCardsFlow()
            ) { emis, loans, cards ->
                Triple(emis, loans, cards)
            }.collect { (emis, loans, cards) ->
                val activeEMIs = emis.filter { it.status == EMIStatus.ACTIVE }
                val completedEMIs = emis.filter { it.status == EMIStatus.COMPLETED }

                // Group EMIs by credit card
                val cardsWithEMIs = cards.mapNotNull { card ->
                    val cardEMIs = activeEMIs.filter { it.creditCardId == card.id }
                    if (cardEMIs.isNotEmpty()) {
                        CardWithEMIs(
                            card = card,
                            emis = cardEMIs,
                            totalMonthlyAmount = cardEMIs.sumOf { it.monthlyAmount }
                        )
                    } else null
                }

                // Standalone EMIs (not linked to any credit card)
                val standaloneEMIs = activeEMIs.filter { it.creditCardId == null }

                val activeLoans = loans.filter { it.status == LoanStatus.ACTIVE }
                val completedLoans = loans.filter { it.status == LoanStatus.COMPLETED || it.status == LoanStatus.CLOSED }

                // Group loans by type
                val loanGroups = activeLoans
                    .groupBy { it.type }
                    .map { (type, typeLoans) ->
                        LoanGroup(
                            type = type,
                            loans = typeLoans,
                            totalMonthlyEMI = typeLoans.sumOf { it.monthlyEMI }
                        )
                    }
                    .sortedBy { it.type.ordinal }

                val totalMonthlyEMI = activeEMIs.sumOf { it.monthlyAmount }
                val totalMonthlyLoanEMI = activeLoans.sumOf { it.monthlyEMI }

                _uiState.update {
                    it.copy(
                        activeEMIs = activeEMIs,
                        completedEMIs = completedEMIs,
                        cardsWithEMIs = cardsWithEMIs,
                        standaloneEMIs = standaloneEMIs,
                        activeLoans = activeLoans,
                        completedLoans = completedLoans,
                        loanGroups = loanGroups,
                        totalMonthlyEMI = totalMonthlyEMI,
                        totalMonthlyLoanEMI = totalMonthlyLoanEMI,
                        totalMonthlyObligations = totalMonthlyEMI + totalMonthlyLoanEMI,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun deleteEMI(emi: EMI) {
        viewModelScope.launch {
            emiRepository.delete(emi)
        }
    }

    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            loanRepository.delete(loan)
        }
    }

    fun markEMICompleted(emi: EMI) {
        viewModelScope.launch {
            emiRepository.markCompleted(emi.id)
        }
    }

    fun markLoanCompleted(loan: Loan) {
        viewModelScope.launch {
            loanRepository.markCompleted(loan.id)
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }
}
