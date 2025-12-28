package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.LoanRepository
import com.fino.app.domain.model.Loan
import com.fino.app.domain.model.LoanStatus
import com.fino.app.domain.model.LoanType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class AddEditLoanUiState(
    val isEditMode: Boolean = false,
    val loanType: LoanType = LoanType.PERSONAL,
    val bankName: String = "",
    val accountNumber: String = "",
    val description: String = "",
    val principalAmount: String = "",
    val interestRate: String = "",
    val monthlyEMI: String = "",
    val tenure: String = "",
    val paidCount: String = "0",
    val startDate: LocalDate = LocalDate.now(),
    val outstandingPrincipal: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditLoanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val loanId: Long = savedStateHandle.get<Long>("loanId") ?: 0L
    private val isEditMode = loanId > 0

    private val _uiState = MutableStateFlow(AddEditLoanUiState(isEditMode = isEditMode))
    val uiState: StateFlow<AddEditLoanUiState> = _uiState.asStateFlow()

    init {
        if (isEditMode) {
            loadLoan()
        }
    }

    private fun loadLoan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val loan = loanRepository.getById(loanId)
                if (loan != null) {
                    _uiState.update {
                        it.copy(
                            loanType = loan.type,
                            bankName = loan.bankName,
                            accountNumber = loan.accountNumber ?: "",
                            description = loan.description,
                            principalAmount = loan.principalAmount.toString(),
                            interestRate = loan.interestRate.toString(),
                            monthlyEMI = loan.monthlyEMI.toString(),
                            tenure = loan.tenure.toString(),
                            paidCount = loan.paidCount.toString(),
                            startDate = loan.startDate,
                            outstandingPrincipal = loan.outstandingPrincipal?.toString() ?: "",
                            notes = loan.notes ?: "",
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Loan not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load loan"
                    )
                }
            }
        }
    }

    fun updateLoanType(type: LoanType) {
        _uiState.update { it.copy(loanType = type) }
    }

    fun updateBankName(value: String) {
        _uiState.update { it.copy(bankName = value) }
    }

    fun updateAccountNumber(value: String) {
        _uiState.update { it.copy(accountNumber = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun updatePrincipalAmount(value: String) {
        _uiState.update { it.copy(principalAmount = value) }
        calculateEMI()
    }

    fun updateInterestRate(value: String) {
        _uiState.update { it.copy(interestRate = value) }
        calculateEMI()
    }

    fun updateMonthlyEMI(value: String) {
        _uiState.update { it.copy(monthlyEMI = value) }
    }

    fun updateTenure(value: String) {
        _uiState.update { it.copy(tenure = value) }
        calculateEMI()
    }

    fun updatePaidCount(value: String) {
        _uiState.update { it.copy(paidCount = value) }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun updateOutstandingPrincipal(value: String) {
        _uiState.update { it.copy(outstandingPrincipal = value) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    private fun calculateEMI() {
        val state = _uiState.value
        val principal = state.principalAmount.toDoubleOrNull() ?: return
        val annualRate = state.interestRate.toFloatOrNull() ?: return
        val tenureMonths = state.tenure.toIntOrNull() ?: return

        if (principal <= 0 || tenureMonths <= 0) return

        // EMI = [P x R x (1+R)^N] / [(1+R)^N - 1]
        // where P = Principal, R = Monthly interest rate, N = Number of months
        if (annualRate > 0) {
            val monthlyRate = (annualRate.toDouble() / 12) / 100
            val factor = Math.pow(1 + monthlyRate, tenureMonths.toDouble())
            val emi = (principal * monthlyRate * factor) / (factor - 1)
            _uiState.update { it.copy(monthlyEMI = "%.2f".format(emi)) }
        } else {
            // 0% interest - simple division
            val emi = principal / tenureMonths
            _uiState.update { it.copy(monthlyEMI = "%.2f".format(emi)) }
        }
    }

    fun save() {
        val state = _uiState.value

        // Validation
        if (state.bankName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Bank name is required") }
            return
        }
        if (state.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Description is required") }
            return
        }
        val principalAmount = state.principalAmount.toDoubleOrNull()
        if (principalAmount == null || principalAmount <= 0) {
            _uiState.update { it.copy(errorMessage = "Valid principal amount is required") }
            return
        }
        val interestRate = state.interestRate.toFloatOrNull()
        if (interestRate == null || interestRate < 0) {
            _uiState.update { it.copy(errorMessage = "Valid interest rate is required") }
            return
        }
        val monthlyEMI = state.monthlyEMI.toDoubleOrNull()
        if (monthlyEMI == null || monthlyEMI <= 0) {
            _uiState.update { it.copy(errorMessage = "Valid monthly EMI is required") }
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

                val loan = Loan(
                    id = if (isEditMode) loanId else 0,
                    type = state.loanType,
                    bankName = state.bankName.trim(),
                    accountNumber = state.accountNumber.trim().takeIf { it.isNotEmpty() },
                    description = state.description.trim(),
                    principalAmount = principalAmount,
                    interestRate = interestRate,
                    monthlyEMI = monthlyEMI,
                    tenure = tenure,
                    paidCount = paidCount,
                    startDate = state.startDate,
                    endDate = endDate,
                    nextDueDate = if (nextDueDate.isAfter(endDate)) endDate else nextDueDate,
                    outstandingPrincipal = state.outstandingPrincipal.toDoubleOrNull(),
                    status = if (paidCount >= tenure) LoanStatus.COMPLETED else LoanStatus.ACTIVE,
                    notes = state.notes.trim().takeIf { it.isNotEmpty() },
                    createdAt = LocalDateTime.now()
                )

                if (isEditMode) {
                    loanRepository.update(loan)
                } else {
                    loanRepository.insert(loan)
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Failed to save loan"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
