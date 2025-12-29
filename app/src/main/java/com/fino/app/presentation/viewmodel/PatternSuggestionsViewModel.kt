package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.PatternSuggestionRepository
import com.fino.app.domain.model.PatternSuggestion
import com.fino.app.service.pattern.PatternDetectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatternSuggestionsUiState(
    val suggestions: List<PatternSuggestion> = emptyList(),
    val isScanning: Boolean = false,
    val acceptedCount: Int = 0,
    val dismissedCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class PatternSuggestionsViewModel @Inject constructor(
    private val patternDetectionService: PatternDetectionService,
    private val patternSuggestionRepository: PatternSuggestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternSuggestionsUiState())
    val uiState: StateFlow<PatternSuggestionsUiState> = _uiState.asStateFlow()

    init {
        // Load existing suggestions on init
        loadExistingSuggestions()
    }

    /**
     * Load existing pending suggestions from database
     */
    private fun loadExistingSuggestions() {
        viewModelScope.launch {
            try {
                val existing = patternSuggestionRepository.getPendingSuggestions()
                _uiState.update { it.copy(suggestions = existing) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /**
     * Scan for recurring patterns in transaction history.
     * Detects patterns and persists them to the database.
     */
    fun scanPatterns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }

            try {
                // Detect patterns
                val patterns = patternDetectionService.detectPatterns()

                // Persist each pattern to the database
                val persistedPatterns = mutableListOf<PatternSuggestion>()
                for (pattern in patterns) {
                    val created = patternSuggestionRepository.createFromPatternDetection(pattern)
                    if (created != null) {
                        persistedPatterns.add(created)
                    }
                }

                // Load all pending suggestions (includes newly created ones)
                val allSuggestions = patternSuggestionRepository.getPendingSuggestions()

                _uiState.update {
                    it.copy(
                        suggestions = allSuggestions,
                        isScanning = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    /**
     * Accept a suggestion and create a recurring rule
     */
    fun acceptSuggestion(suggestion: PatternSuggestion) {
        viewModelScope.launch {
            try {
                // Use repository to confirm (creates RecurringRule automatically)
                patternSuggestionRepository.confirmSuggestion(suggestion.id)
                _uiState.update { state ->
                    state.copy(
                        suggestions = state.suggestions.filter { it.id != suggestion.id },
                        acceptedCount = state.acceptedCount + 1
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /**
     * Dismiss a suggestion (don't create a rule)
     */
    fun dismissSuggestion(suggestion: PatternSuggestion) {
        viewModelScope.launch {
            try {
                // Use repository to dismiss
                patternSuggestionRepository.dismissSuggestion(suggestion.id)
                _uiState.update { state ->
                    state.copy(
                        suggestions = state.suggestions.filter { it.id != suggestion.id },
                        dismissedCount = state.dismissedCount + 1
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
