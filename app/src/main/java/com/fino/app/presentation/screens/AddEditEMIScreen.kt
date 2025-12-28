package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.CreditCard
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AddEditEMIViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEMIScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditEMIViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCardSelector by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit EMI" else "Add EMI",
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
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Credit Card Selector
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Credit Card (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .clickable { showCardSelector = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CreditCard,
                                contentDescription = null,
                                tint = if (uiState.selectedCardId != null) Primary else TextTertiary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            val selectedCard = uiState.creditCards.find { it.id == uiState.selectedCardId }
                            Text(
                                text = selectedCard?.let { "${it.bankName} •••• ${it.lastFourDigits}" }
                                    ?: "No card selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedCard != null) TextPrimary else TextTertiary
                            )
                        }
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Description
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description *") },
                    placeholder = { Text("e.g., iPhone 15 Pro") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
            }

            // Merchant Name
            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.merchantName,
                    onValueChange = { viewModel.updateMerchantName(it) },
                    label = { Text("Merchant/Store") },
                    placeholder = { Text("e.g., Apple Store") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
            }

            // Original Amount and Tenure (side by side)
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.originalAmount,
                        onValueChange = { viewModel.updateOriginalAmount(it) },
                        label = { Text("Original Amount *") },
                        placeholder = { Text("100000") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Text("₹", color = TextSecondary)
                        },
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = uiState.tenure,
                        onValueChange = { viewModel.updateTenure(it) },
                        label = { Text("Tenure *") },
                        placeholder = { Text("12") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("months", color = TextTertiary) },
                        colors = textFieldColors()
                    )
                }
            }

            // Monthly EMI and Paid Count
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.monthlyAmount,
                        onValueChange = { viewModel.updateMonthlyAmount(it) },
                        label = { Text("Monthly EMI *") },
                        placeholder = { Text("8333") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text("₹", color = TextSecondary)
                        },
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = uiState.paidCount,
                        onValueChange = { viewModel.updatePaidCount(it) },
                        label = { Text("Paid EMIs") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors()
                    )
                }
            }

            // Start Date
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Start Date",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .clickable { showDatePicker = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.startDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Interest Rate and Processing Fee
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.interestRate,
                        onValueChange = { viewModel.updateInterestRate(it) },
                        label = { Text("Interest Rate") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text("%", color = TextTertiary) },
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = uiState.processingFee,
                        onValueChange = { viewModel.updateProcessingFee(it) },
                        label = { Text("Processing Fee") },
                        placeholder = { Text("0") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Text("₹", color = TextSecondary)
                        },
                        colors = textFieldColors()
                    )
                }
            }

            // Notes
            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    label = { Text("Notes") },
                    placeholder = { Text("Any additional notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = textFieldColors()
                )
            }

            // Error Message
            if (uiState.errorMessage != null) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ExpenseRed.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Error,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                BouncyButton(
                    onClick = { viewModel.save() },
                    modifier = Modifier.fillMaxWidth(),
                    gradient = FinoGradients.Primary,
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = TextPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        if (uiState.isEditMode) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isEditMode) "Save Changes" else "Add EMI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }

    // Card Selector Bottom Sheet
    if (showCardSelector) {
        ModalBottomSheet(
            onDismissRequest = { showCardSelector = false },
            containerColor = DarkSurface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Select Credit Card",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // No card option
                CardSelectorItem(
                    text = "No card (Standalone EMI)",
                    isSelected = uiState.selectedCardId == null,
                    onClick = {
                        viewModel.updateSelectedCard(null)
                        showCardSelector = false
                    }
                )

                uiState.creditCards.forEach { card ->
                    Spacer(modifier = Modifier.height(8.dp))
                    CardSelectorItem(
                        text = "${card.bankName} •••• ${card.lastFourDigits}",
                        subtitle = card.cardName,
                        isSelected = uiState.selectedCardId == card.id,
                        onClick = {
                            viewModel.updateSelectedCard(card.id)
                            showCardSelector = false
                        }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateStartDate(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = DarkSurface
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = Primary,
                    selectedYearContentColor = TextPrimary,
                    selectedYearContainerColor = Primary,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = TextPrimary,
                    selectedDayContainerColor = Primary,
                    todayContentColor = Primary,
                    todayDateBorderColor = Primary
                )
            )
        }
    }
}

@Composable
private fun CardSelectorItem(
    text: String,
    subtitle: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.1f) else DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Primary else TextPrimary
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Primary
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = Primary,
    unfocusedBorderColor = DarkSurfaceHigh,
    focusedLabelColor = Primary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = Primary,
    focusedContainerColor = DarkSurfaceVariant,
    unfocusedContainerColor = DarkSurfaceVariant
)
