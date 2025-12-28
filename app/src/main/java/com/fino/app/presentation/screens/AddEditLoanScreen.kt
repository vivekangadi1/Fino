package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.LoanType
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AddEditLoanViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLoanScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditLoanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    var showDatePicker by remember { mutableStateOf(false) }

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
                        text = if (uiState.isEditMode) "Edit Loan" else "Add Loan",
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
            // Loan Type Selector
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loan Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LoanType.entries) { type ->
                        LoanTypeChip(
                            type = type,
                            isSelected = uiState.loanType == type,
                            onClick = { viewModel.updateLoanType(type) }
                        )
                    }
                }
            }

            // Bank Name
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.bankName,
                    onValueChange = { viewModel.updateBankName(it) },
                    label = { Text("Bank Name *") },
                    placeholder = { Text("e.g., HDFC Bank") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AccountBalance,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    colors = textFieldColors()
                )
            }

            // Account Number
            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.accountNumber,
                    onValueChange = { viewModel.updateAccountNumber(it) },
                    label = { Text("Loan Account Number") },
                    placeholder = { Text("Optional") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
            }

            // Description
            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description *") },
                    placeholder = { Text("e.g., Home Loan for Apartment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors()
                )
            }

            // Principal Amount and Interest Rate
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.principalAmount,
                        onValueChange = { viewModel.updatePrincipalAmount(it) },
                        label = { Text("Principal Amount *") },
                        placeholder = { Text("1000000") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Text("₹", color = TextSecondary)
                        },
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = uiState.interestRate,
                        onValueChange = { viewModel.updateInterestRate(it) },
                        label = { Text("Interest Rate *") },
                        placeholder = { Text("8.5") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffix = { Text("% p.a.", color = TextTertiary) },
                        colors = textFieldColors()
                    )
                }
            }

            // Tenure and Monthly EMI
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.tenure,
                        onValueChange = { viewModel.updateTenure(it) },
                        label = { Text("Tenure *") },
                        placeholder = { Text("240") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("months", color = TextTertiary) },
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = uiState.monthlyEMI,
                        onValueChange = { viewModel.updateMonthlyEMI(it) },
                        label = { Text("Monthly EMI *") },
                        placeholder = { Text("Auto-calculated") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text("₹", color = TextSecondary)
                        },
                        colors = textFieldColors()
                    )
                }
            }

            // Paid EMIs
            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.paidCount,
                    onValueChange = { viewModel.updatePaidCount(it) },
                    label = { Text("EMIs Already Paid") },
                    placeholder = { Text("0") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = {
                        Text(
                            text = "Enter how many EMIs you've already paid",
                            color = TextTertiary
                        )
                    },
                    colors = textFieldColors()
                )
            }

            // Start Date
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loan Start Date",
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

            // Outstanding Principal
            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.outstandingPrincipal,
                    onValueChange = { viewModel.updateOutstandingPrincipal(it) },
                    label = { Text("Outstanding Principal") },
                    placeholder = { Text("Current remaining principal") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Text("₹", color = TextSecondary)
                    },
                    supportingText = {
                        Text(
                            text = "Optional - helps track prepayment progress",
                            color = TextTertiary
                        )
                    },
                    colors = textFieldColors()
                )
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
                        text = if (uiState.isEditMode) "Save Changes" else "Add Loan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
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
private fun LoanTypeChip(
    type: LoanType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.2f) else DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = type.emoji,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Primary else TextSecondary
            )
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
