package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AddRecurringBillViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringBillScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddRecurringBillViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Handle save/delete success
    LaunchedEffect(uiState.saveSuccess, uiState.deleteSuccess) {
        if (uiState.saveSuccess || uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Bill", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete this bill? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteBill()
                    }
                ) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Date Picker for ONE_TIME bills
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.specificDueDate?.toEpochDay()?.times(86400000L)
                ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / 86400000L)
                        viewModel.updateSpecificDueDate(date)
                    }
                    showDatePicker = false
                }) {
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
                    dayContentColor = TextPrimary,
                    selectedDayContainerColor = Primary,
                    selectedDayContentColor = TextPrimary,
                    todayContentColor = Primary,
                    todayDateBorderColor = Primary
                )
            )
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) "Edit Bill" else "Add Bill",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !uiState.isDeleting
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = ExpenseRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Merchant Name
            FormSection(title = "Bill Name") {
                OutlinedTextField(
                    value = uiState.merchantName,
                    onValueChange = { viewModel.updateMerchantName(it) },
                    placeholder = { Text("e.g., Netflix, Electricity", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            // Amount
            FormSection(title = "Amount") {
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    placeholder = { Text("0.00", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    prefix = {
                        Text(
                            text = "Rs. ",
                            color = TextSecondary
                        )
                    }
                )
            }

            // Frequency
            FormSection(title = "Frequency") {
                FrequencySelector(
                    selectedFrequency = uiState.frequency,
                    onFrequencySelected = { viewModel.updateFrequency(it) }
                )
            }

            // Day of Period or Due Date (for ONE_TIME)
            if (uiState.frequency == RecurringFrequency.ONE_TIME) {
                FormSection(title = "Due Date") {
                    OutlinedTextField(
                        value = uiState.specificDueDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                            ?: "Select date",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Select date",
                                    tint = Primary
                                )
                            }
                        }
                    )
                }
            } else {
                FormSection(
                    title = when (uiState.frequency) {
                        RecurringFrequency.WEEKLY -> "Day of Week"
                        RecurringFrequency.MONTHLY -> "Day of Month"
                        RecurringFrequency.YEARLY -> "Day of Month"
                        RecurringFrequency.ONE_TIME -> "" // Won't reach here
                    }
                ) {
                    DaySelector(
                        frequency = uiState.frequency,
                        selectedDay = uiState.dayOfPeriod,
                        onDaySelected = { viewModel.updateDayOfPeriod(it) }
                    )
                }
            }

            // Category
            if (uiState.categories.isNotEmpty()) {
                FormSection(title = "Category (Optional)") {
                    CategorySelector(
                        categories = uiState.categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }
            }

            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = error,
                        color = ExpenseRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            // Save button
            Button(
                onClick = { viewModel.saveBill() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !uiState.isSaving && !uiState.isLoading && !uiState.isDeleting
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = TextOnGradient,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TextOnGradient
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isEditMode) "Update Bill" else "Save Bill",
                        color = TextOnGradient,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun FrequencySelector(
    selectedFrequency: RecurringFrequency,
    onFrequencySelected: (RecurringFrequency) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RecurringFrequency.entries.forEach { frequency ->
            FrequencyChip(
                label = frequency.toDisplayString(),
                isSelected = frequency == selectedFrequency,
                onClick = { onFrequencySelected(frequency) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FrequencyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Primary else DarkSurfaceVariant
    val textColor = if (isSelected) TextOnGradient else TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun DaySelector(
    frequency: RecurringFrequency,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (frequency) {
        RecurringFrequency.WEEKLY -> {
            val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEachIndexed { index, day ->
                    val dayValue = index + 1
                    DayChip(
                        label = day,
                        isSelected = dayValue == selectedDay,
                        onClick = { onDaySelected(dayValue) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        RecurringFrequency.MONTHLY, RecurringFrequency.YEARLY -> {
            LazyRow(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..31).toList()) { day ->
                    DayChip(
                        label = day.toString(),
                        isSelected = day == selectedDay,
                        onClick = { onDaySelected(day) }
                    )
                }
            }
        }
        RecurringFrequency.ONE_TIME -> {
            // Not used - ONE_TIME uses date picker instead of DaySelector
        }
    }
}

@Composable
private fun DayChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Primary else DarkSurfaceVariant
    val textColor = if (isSelected) TextOnGradient else TextSecondary

    Box(
        modifier = modifier
            .widthIn(min = 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun CategorySelector(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "None" option
        item {
            CategoryChip(
                emoji = "-",
                name = "None",
                isSelected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) }
            )
        }

        items(categories) { category ->
            CategoryChip(
                emoji = category.emoji,
                name = category.name,
                isSelected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    emoji: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Primary.copy(alpha = 0.2f) else DarkSurfaceVariant
    val borderColor = if (isSelected) Primary else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = emoji,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                color = if (isSelected) Primary else TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

private fun RecurringFrequency.toDisplayString(): String = when (this) {
    RecurringFrequency.ONE_TIME -> "One-Time"
    RecurringFrequency.WEEKLY -> "Weekly"
    RecurringFrequency.MONTHLY -> "Monthly"
    RecurringFrequency.YEARLY -> "Yearly"
}
