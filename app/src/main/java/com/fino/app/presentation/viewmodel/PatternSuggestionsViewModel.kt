package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val patternDetectionService: PatternDetectionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatternSuggestionsUiState())
    val uiState: StateFlow<PatternSuggestionsUiState> = _uiState.asStateFlow()

    /**
     * Scan for recurring patterns in transaction history
     */
    fun scanPatterns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }

            try {
                val patterns = patternDetectionService.detectPatterns()
                _uiState.update {
                    it.copy(
                        suggestions = patterns,
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
                patternDetectionService.confirmPattern(suggestion)
                _uiState.update { state ->
                    state.copy(
                        suggestions = state.suggestions.filter { it.merchantPattern != suggestion.merchantPattern },
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
                patternDetectionService.dismissPattern(suggestion)
                _uiState.update { state ->
                    state.copy(
                        suggestions = state.suggestions.filter { it.merchantPattern != suggestion.merchantPattern },
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
