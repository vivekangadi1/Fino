package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.PatternSuggestionRepository
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
    val enhancedGroups: List<EnhancedBillGroup> = emptyList(),
    val patternSuggestions: List<PatternSuggestion> = emptyList(),
    val calendarBills: Map<LocalDate, List<UpcomingBill>> = emptyMap(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val showCalendarView: Boolean = false,
    val expandedGroups: Set<BillGroupType> = setOf(BillGroupType.TODAY, BillGroupType.TOMORROW, BillGroupType.THIS_WEEK),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for the Upcoming Bills feature
 */
@HiltViewModel
class UpcomingBillsViewModel @Inject constructor(
    private val upcomingBillsRepository: UpcomingBillsRepository,
    private val patternDetectionService: PatternDetectionService,
    private val patternSuggestionRepository: PatternSuggestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpcomingBillsUiState())
    val uiState: StateFlow<UpcomingBillsUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observePendingSuggestions()
    }

    /**
     * Observe pending suggestions from the repository for real-time updates.
     */
    private fun observePendingSuggestions() {
        viewModelScope.launch {
            patternSuggestionRepository.getPendingSuggestionsFlow().collect { suggestions ->
                _uiState.update { state ->
                    state.copy(patternSuggestions = suggestions)
                }
            }
        }
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

                // Pattern suggestions are now loaded via observePendingSuggestions()
                // No need to call detectPatterns() here

                // Fetch calendar data if calendar view is enabled
                val calendarBills = if (_uiState.value.showCalendarView) {
                    upcomingBillsRepository.getBillsForCalendar(_uiState.value.selectedMonth)
                } else {
                    emptyMap()
                }

                // Create enhanced groups with category subgroups
                val expandedSet = _uiState.value.expandedGroups
                val enhancedGroups = groups.map { group ->
                    EnhancedBillGroup.fromBillGroup(group, group.type in expandedSet)
                }

                _uiState.update { state ->
                    state.copy(
                        summary = summary,
                        groupedBills = groups,
                        enhancedGroups = enhancedGroups,
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
     * Toggle expansion state of a bill group
     */
    fun toggleGroupExpansion(groupType: BillGroupType) {
        _uiState.update { state ->
            val newExpandedGroups = if (groupType in state.expandedGroups) {
                state.expandedGroups - groupType
            } else {
                state.expandedGroups + groupType
            }
            val updatedEnhancedGroups = state.enhancedGroups.map { group ->
                group.copy(isExpanded = group.type in newExpandedGroups)
            }
            state.copy(
                expandedGroups = newExpandedGroups,
                enhancedGroups = updatedEnhancedGroups
            )
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
                // Use repository to confirm (creates RecurringRule automatically)
                patternSuggestionRepository.confirmSuggestion(suggestion.id)
                // UI will update automatically via the Flow observer
                loadData() // Refresh bill data
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
                // Use repository to dismiss
                patternSuggestionRepository.dismissSuggestion(suggestion.id)
                // UI will update automatically via the Flow observer
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
