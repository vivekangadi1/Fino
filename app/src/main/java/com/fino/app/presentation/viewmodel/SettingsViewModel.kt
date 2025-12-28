package com.fino.app.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.fino.app.data.local.preferences.SettingsData
import com.fino.app.data.local.preferences.UserPreferences
import com.fino.app.data.preferences.AppPreferences
import com.fino.app.worker.InitialSmsScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: SettingsData = SettingsData(),
    val isLoading: Boolean = true,
    val appVersion: String = "1.0.0",
    val isSmsScanning: Boolean = false,
    val smsScanComplete: Boolean = false,
    val smsScanError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferences.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        settings = settings,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setNotifyUncategorized(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyUncategorized(enabled)
        }
    }

    fun setNotifyRecurringPatterns(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyRecurringPatterns(enabled)
        }
    }

    fun setNotifyBillReminders(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyBillReminders(enabled)
        }
    }

    fun setNotifyEMIDue(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotifyEMIDue(enabled)
        }
    }

    fun setDailyDigestEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDailyDigestEnabled(enabled)
        }
    }

    fun setDailyDigestTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferences.setDailyDigestTime(hour, minute)
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setDarkModeEnabled(enabled)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setBiometricEnabled(enabled)
        }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            userPreferences.setCurrencySymbol(symbol)
        }
    }

    fun setDefaultPaymentMethod(method: String) {
        viewModelScope.launch {
            userPreferences.setDefaultPaymentMethod(method)
        }
    }

    /**
     * Trigger historical SMS scan to detect recurring patterns.
     * This resets the scan status and runs the InitialSmsScanWorker.
     */
    fun triggerSmsScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSmsScanning = true, smsScanComplete = false, smsScanError = null) }

            // Reset the initial scan flag to force re-scan
            appPreferences.hasCompletedInitialScan = false
            appPreferences.isInitialScanInProgress = false

            // Create and enqueue the work request
            val workRequest = OneTimeWorkRequestBuilder<InitialSmsScanWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(
                "manual_sms_scan",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            // Observe work status
            workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        _uiState.update { it.copy(isSmsScanning = false, smsScanComplete = true) }
                    }
                    WorkInfo.State.FAILED -> {
                        val errorMsg = workInfo.outputData.getString("error") ?: "Scan failed. Check SMS permissions."
                        _uiState.update { it.copy(isSmsScanning = false, smsScanError = errorMsg) }
                    }
                    WorkInfo.State.CANCELLED -> {
                        _uiState.update { it.copy(isSmsScanning = false, smsScanError = "Scan cancelled") }
                    }
                    WorkInfo.State.RUNNING -> {
                        _uiState.update { it.copy(isSmsScanning = true) }
                    }
                    else -> { /* Enqueued or blocked */ }
                }
            }
        }
    }

    /**
     * Clear the scan complete/error status.
     */
    fun clearSmsScanStatus() {
        _uiState.update { it.copy(smsScanComplete = false, smsScanError = null) }
    }
}
