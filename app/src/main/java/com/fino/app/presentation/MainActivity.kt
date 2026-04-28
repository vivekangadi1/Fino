package com.fino.app.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fino.app.service.notification.NotificationService
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fino.app.data.local.preferences.UserPreferences
import com.fino.app.data.preferences.AppPreferences
import com.fino.app.presentation.navigation.FinoNavigation
import com.fino.app.presentation.screens.LockScreen
import com.fino.app.presentation.theme.FinoTheme
import com.fino.app.security.AppLockManager
import com.fino.app.security.BiometricAuthHelper
import com.fino.app.BuildConfig
import com.fino.app.worker.BillDueCheckWorker
import com.fino.app.worker.GmailBillSyncWorker
import com.fino.app.worker.InitialSmsScanWorker
import com.fino.app.worker.NoticesGeneratorWorker
import com.fino.app.worker.RecurringPatternWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var appLockManager: AppLockManager

    private var biometricEnabled: Boolean = false
    private var hasAuthenticatedThisSession: Boolean = false

    private val deepLinkFlow = MutableStateFlow<String?>(null)
    val deepLink: StateFlow<String?> = deepLinkFlow.asStateFlow()

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readSmsGranted = permissions[Manifest.permission.READ_SMS] == true
        if (readSmsGranted) {
            scheduleInitialSmsScan()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricEnabled = runBlocking { userPreferences.settingsFlow.first().biometricEnabled }
        if (biometricEnabled && canPromptBiometric()) {
            appLockManager.lock()
        } else {
            appLockManager.unlock()
        }

        requestPermissions()
        intent?.let { handleIntentDeepLink(it) }

        setContent {
            FinoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isLocked by appLockManager.isLocked.collectAsState()
                    val pendingDeepLink by deepLink.collectAsState()

                    if (isLocked) {
                        LockScreen(onUnlockClick = { showBiometricPrompt() })

                        LaunchedEffect(isLocked) {
                            showBiometricPrompt()
                        }
                    } else {
                        FinoNavigation(
                            pendingDeepLink = pendingDeepLink,
                            onDeepLinkConsumed = { deepLinkFlow.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentDeepLink(intent)
    }

    private fun handleIntentDeepLink(intent: Intent) {
        val route = intent.getStringExtra(NotificationService.EXTRA_DEEP_LINK)
        if (!route.isNullOrBlank()) {
            deepLinkFlow.value = route
            intent.removeExtra(NotificationService.EXTRA_DEEP_LINK)
        }
    }

    override fun onStart() {
        super.onStart()
        biometricEnabled = runBlocking { userPreferences.settingsFlow.first().biometricEnabled }
        if (biometricEnabled && canPromptBiometric() && !hasAuthenticatedThisSession) {
            appLockManager.lock()
        }
    }

    override fun onStop() {
        super.onStop()
        hasAuthenticatedThisSession = false
        if (biometricEnabled) {
            appLockManager.lock()
        }
    }

    private fun canPromptBiometric(): Boolean {
        return BiometricAuthHelper.availability(this) == BiometricAuthHelper.Availability.AVAILABLE
    }

    private fun showBiometricPrompt() {
        if (!canPromptBiometric()) {
            appLockManager.unlock()
            hasAuthenticatedThisSession = true
            return
        }
        BiometricAuthHelper.prompt(
            activity = this,
            onSuccess = {
                hasAuthenticatedThisSession = true
                appLockManager.unlock()
            },
            onError = { _, _ -> },
            onFailed = { }
        )
    }

    private fun requestPermissions() {
        val smsPermissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )

        val smsPermissionsNeeded = smsPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (smsPermissionsNeeded.isNotEmpty()) {
            smsPermissionLauncher.launch(smsPermissionsNeeded.toTypedArray())
        } else {
            scheduleInitialSmsScan()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        schedulePeriodicPatternDetection()
        schedulePeriodicNoticesGeneration()
        schedulePeriodicBillDueCheck()
        if (BuildConfig.ENABLE_GMAIL) {
            schedulePeriodicGmailSync()
        }
    }

    private fun scheduleInitialSmsScan() {
        if (!appPreferences.hasCompletedInitialScan && !appPreferences.isInitialScanInProgress) {
            val workRequest = OneTimeWorkRequestBuilder<InitialSmsScanWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(this).enqueueUniqueWork(
                InitialSmsScanWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    private fun schedulePeriodicPatternDetection() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringPatternWorker>(
            1, TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RecurringPatternWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun schedulePeriodicNoticesGeneration() {
        val workRequest = PeriodicWorkRequestBuilder<NoticesGeneratorWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            NoticesGeneratorWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun schedulePeriodicBillDueCheck() {
        val workRequest = PeriodicWorkRequestBuilder<BillDueCheckWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            BillDueCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun schedulePeriodicGmailSync() {
        val workRequest = PeriodicWorkRequestBuilder<GmailBillSyncWorker>(
            12, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            GmailBillSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
