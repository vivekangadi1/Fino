package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.theme.ExpenseRed
import com.fino.app.presentation.theme.IncomeGreen
import com.fino.app.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMerchantMappings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Notifications Section
            item {
                SectionHeader(
                    title = "Notifications",
                    icon = Icons.Outlined.Notifications
                )
            }

            item {
                SettingsToggle(
                    title = "Uncategorized Transactions",
                    subtitle = "Alert when new transactions need categorization",
                    isChecked = uiState.settings.notifyUncategorized,
                    onCheckedChange = { viewModel.setNotifyUncategorized(it) }
                )
            }

            item {
                SettingsToggle(
                    title = "Recurring Pattern Detection",
                    subtitle = "Notify when new recurring bills are detected",
                    isChecked = uiState.settings.notifyRecurringPatterns,
                    onCheckedChange = { viewModel.setNotifyRecurringPatterns(it) }
                )
            }

            item {
                SettingsToggle(
                    title = "Bill Reminders",
                    subtitle = "Remind before upcoming bill due dates",
                    isChecked = uiState.settings.notifyBillReminders,
                    onCheckedChange = { viewModel.setNotifyBillReminders(it) }
                )
            }

            item {
                SettingsToggle(
                    title = "EMI Due Reminders",
                    subtitle = "Remind before EMI and loan payment dates",
                    isChecked = uiState.settings.notifyEMIDue,
                    onCheckedChange = { viewModel.setNotifyEMIDue(it) }
                )
            }

            item {
                SettingsToggle(
                    title = "Daily Digest",
                    subtitle = "Summary of daily spending at ${formatTime(uiState.settings.dailyDigestHour, uiState.settings.dailyDigestMinute)}",
                    isChecked = uiState.settings.dailyDigestEnabled,
                    onCheckedChange = { viewModel.setDailyDigestEnabled(it) }
                )
            }

            if (uiState.settings.dailyDigestEnabled) {
                item {
                    SettingsItem(
                        title = "Digest Time",
                        subtitle = formatTime(uiState.settings.dailyDigestHour, uiState.settings.dailyDigestMinute),
                        icon = Icons.Outlined.Schedule,
                        onClick = { showTimePickerDialog = true }
                    )
                }
            }

            // Data & Privacy Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Data & Privacy",
                    icon = Icons.Outlined.Security
                )
            }

            item {
                SmsScanItem(
                    isScanning = uiState.isSmsScanning,
                    scanComplete = uiState.smsScanComplete,
                    scanError = uiState.smsScanError,
                    onScanClick = { viewModel.triggerSmsScan() },
                    onDismissStatus = { viewModel.clearSmsScanStatus() }
                )
            }

            item {
                SettingsItem(
                    title = "Manage Merchant Mappings",
                    subtitle = "View and edit merchant-to-category mappings",
                    icon = Icons.Outlined.Category,
                    onClick = onNavigateToMerchantMappings
                )
            }

            item {
                SettingsItem(
                    title = "Export Data",
                    subtitle = "Export your transactions as CSV",
                    icon = Icons.Outlined.FileDownload,
                    onClick = { /* TODO */ }
                )
            }

            item {
                SettingsItem(
                    title = "Backup & Restore",
                    subtitle = "Backup to Google Drive",
                    icon = Icons.Outlined.CloudUpload,
                    onClick = { /* TODO */ },
                    badge = "Coming Soon"
                )
            }

            // Appearance Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Appearance",
                    icon = Icons.Outlined.Palette
                )
            }

            item {
                SettingsToggle(
                    title = "Dark Mode",
                    subtitle = "Enable dark theme",
                    isChecked = uiState.settings.darkModeEnabled,
                    onCheckedChange = { viewModel.setDarkModeEnabled(it) }
                )
            }

            // Security Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Security",
                    icon = Icons.Outlined.Lock
                )
            }

            item {
                SettingsToggle(
                    title = "Biometric Lock",
                    subtitle = "Require fingerprint or face to open app",
                    isChecked = uiState.settings.biometricEnabled,
                    onCheckedChange = { viewModel.setBiometricEnabled(it) }
                )
            }

            // About Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "About",
                    icon = Icons.Outlined.Info
                )
            }

            item {
                SettingsItem(
                    title = "Version",
                    subtitle = uiState.appVersion,
                    icon = Icons.Outlined.Android,
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    title = "Rate the App",
                    subtitle = "Help us with a 5-star rating",
                    icon = Icons.Outlined.Star,
                    onClick = { /* TODO */ }
                )
            }

            item {
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = { /* TODO */ }
                )
            }

            item {
                SettingsItem(
                    title = "Terms of Service",
                    subtitle = "Read our terms of service",
                    icon = Icons.Outlined.Description,
                    onClick = { /* TODO */ }
                )
            }
        }
    }

    // Time Picker Dialog
    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.settings.dailyDigestHour,
            initialMinute = uiState.settings.dailyDigestMinute
        )
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDailyDigestTime(timePickerState.hour, timePickerState.minute)
                        showTimePickerDialog = false
                    }
                ) {
                    Text("OK", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            title = {
                Text("Set Digest Time", color = TextPrimary)
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = DarkSurfaceVariant,
                        clockDialSelectedContentColor = TextPrimary,
                        clockDialUnselectedContentColor = TextSecondary,
                        selectorColor = Primary,
                        containerColor = DarkSurface,
                        periodSelectorSelectedContainerColor = Primary,
                        periodSelectorUnselectedContainerColor = DarkSurfaceVariant,
                        periodSelectorSelectedContentColor = TextPrimary,
                        periodSelectorUnselectedContentColor = TextSecondary,
                        timeSelectorSelectedContainerColor = Primary.copy(alpha = 0.2f),
                        timeSelectorUnselectedContainerColor = DarkSurfaceVariant,
                        timeSelectorSelectedContentColor = Primary,
                        timeSelectorUnselectedContentColor = TextPrimary
                    )
                )
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Primary
        )
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = DarkSurface
                )
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    badge: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    badge?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Primary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextTertiary
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(displayHour, minute, period)
}

@Composable
private fun SmsScanItem(
    isScanning: Boolean,
    scanComplete: Boolean,
    scanError: String?,
    onScanClick: () -> Unit,
    onDismissStatus: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable(enabled = !isScanning) { onScanClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Outlined.Sms,
                    contentDescription = null,
                    tint = if (scanComplete) IncomeGreen else if (scanError != null) ExpenseRed else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scan Historical SMS",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = when {
                        isScanning -> "Scanning SMS for recurring patterns..."
                        scanComplete -> "Scan complete! Check Upcoming Bills for suggestions."
                        scanError != null -> scanError
                        else -> "Detect recurring bills from past 3 months"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        scanComplete -> IncomeGreen
                        scanError != null -> ExpenseRed
                        else -> TextSecondary
                    }
                )
            }
            if (scanComplete || scanError != null) {
                IconButton(onClick = onDismissStatus) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (!isScanning) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextTertiary
                )
            }
        }
    }
}
