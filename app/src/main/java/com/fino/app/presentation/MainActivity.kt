package com.fino.app.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fino.app.data.preferences.AppPreferences
import com.fino.app.presentation.navigation.FinoNavigation
import com.fino.app.presentation.theme.FinoTheme
import com.fino.app.worker.InitialSmsScanWorker
import com.fino.app.worker.RecurringPatternWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if SMS read permission was granted
        val readSmsGranted = permissions[Manifest.permission.READ_SMS] == true
        if (readSmsGranted) {
            scheduleInitialSmsScan()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Notification permission granted or denied
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        requestPermissions()

        setContent {
            FinoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinoNavigation()
                }
            }
        }
    }

    private fun requestPermissions() {
        // Request SMS permissions
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
            // Permissions already granted, check if initial scan needed
            scheduleInitialSmsScan()
        }

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Schedule periodic pattern detection (runs daily)
        schedulePeriodicPatternDetection()
    }

    /**
     * Schedule the initial SMS scan if not already completed.
     * This scans historical SMS messages and runs pattern detection.
     */
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

    /**
     * Schedule periodic pattern detection to run daily.
     * This detects new recurring bill patterns from transaction history.
     */
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
}
