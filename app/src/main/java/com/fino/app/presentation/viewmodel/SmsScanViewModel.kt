package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.service.sms.SmsScanResult
import com.fino.app.service.sms.SmsScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class SmsScanUiState(
    val isScanning: Boolean = false,
    val lastResult: SmsScanResult? = null,
    val error: String? = null
)

@HiltViewModel
class SmsScanViewModel @Inject constructor(
    private val smsScanner: SmsScanner
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsScanUiState())
    val uiState: StateFlow<SmsScanUiState> = _uiState.asStateFlow()

    /**
     * Scan SMS messages from the current month.
     */
    fun scanCurrentMonth() {
        scanMonth(YearMonth.now())
    }

    /**
     * Scan SMS messages from a specific month.
     */
    fun scanMonth(yearMonth: YearMonth) {
        // Don't start if already scanning
        if (_uiState.value.isScanning) return

        _uiState.update { it.copy(isScanning = true, error = null) }

        viewModelScope.launch {
            try {
                val result = smsScanner.scanMonth(yearMonth)
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        lastResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    /**
     * Clear the error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
