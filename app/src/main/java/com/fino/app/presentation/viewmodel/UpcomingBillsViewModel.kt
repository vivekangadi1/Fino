package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.UpcomingBillsRepository
import com.fino.app.domain.model.*
import com.fino.app.service.pattern.PatternDetectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * UI State for the Upcoming Bills screen
 */
data class UpcomingBillsUiState(
    val summary: BillSummary? = null,
    val groupedBills: List<BillGroup> = emptyList(),
    val patternSuggestions: List<PatternSuggestion> = emptyList(),
    val calendarBills: Map<LocalDate, List<UpcomingBill>> = emptyMap(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val showCalendarView: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for the Upcoming Bills feature
 */
@HiltViewModel
class UpcomingBillsViewModel @Inject constructor(
    private val upcomingBillsRepository: UpcomingBillsRepository,
    private val patternDetectionService: PatternDetectionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpcomingBillsUiState())
    val uiState: StateFlow<UpcomingBillsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    /**
     * Load all bill data
     */
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Fetch summary
                val summary = upcomingBillsRepository.getBillSummary()

                // Fetch grouped bills
                val groups = upcomingBillsRepository.getGroupedBills()

                // Fetch pattern suggestions
                val suggestions = patternDetectionService.detectPatterns()

                // Fetch calendar data if calendar view is enabled
                val calendarBills = if (_uiState.value.showCalendarView) {
                    upcomingBillsRepository.getBillsForCalendar(_uiState.value.selectedMonth)
                } else {
                    emptyMap()
                }

                _uiState.update { state ->
                    state.copy(
                        summary = summary,
                        groupedBills = groups,
                        patternSuggestions = suggestions,
                        calendarBills = calendarBills,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Toggle between list view and calendar view
     */
    fun toggleCalendarView() {
        viewModelScope.launch {
            val newShowCalendar = !_uiState.value.showCalendarView
            _uiState.update { it.copy(showCalendarView = newShowCalendar) }

            if (newShowCalendar) {
                loadCalendarData()
            }
        }
    }

    /**
     * Select a month for calendar view
     */
    fun selectMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedMonth = yearMonth) }
            if (_uiState.value.showCalendarView) {
                loadCalendarData()
            }
        }
    }

    /**
     * Mark a bill as paid
     */
    fun markBillAsPaid(bill: UpcomingBill) {
        viewModelScope.launch {
            try {
                upcomingBillsRepository.markBillAsPaid(bill, null)
                loadData() // Refresh data
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Confirm a pattern suggestion and create a recurring rule
     */
    fun confirmPatternSuggestion(suggestion: PatternSuggestion) {
        viewModelScope.launch {
            try {
                patternDetectionService.confirmPattern(suggestion)
                // Remove from suggestions and refresh
                _uiState.update { state ->
                    state.copy(
                        patternSuggestions = state.patternSuggestions.filter {
                            it.merchantPattern != suggestion.merchantPattern
                        }
                    )
                }
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Dismiss a pattern suggestion
     */
    fun dismissPatternSuggestion(suggestion: PatternSuggestion) {
        viewModelScope.launch {
            try {
                patternDetectionService.dismissPattern(suggestion)
                // Remove from suggestions list
                _uiState.update { state ->
                    state.copy(
                        patternSuggestions = state.patternSuggestions.filter {
                            it.merchantPattern != suggestion.merchantPattern
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Load calendar data for the selected month
     */
    private suspend fun loadCalendarData() {
        try {
            val calendarBills = upcomingBillsRepository.getBillsForCalendar(_uiState.value.selectedMonth)
            _uiState.update { it.copy(calendarBills = calendarBills) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }
}
