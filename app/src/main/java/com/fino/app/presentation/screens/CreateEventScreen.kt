package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.SlideInCard
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.CreateEventViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Navigate back on success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = uiState.startDate,
            onDateSelected = { date ->
                viewModel.setStartDate(date)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        DatePickerDialog(
            initialDate = uiState.endDate,
            minDate = uiState.startDate,
            onDateSelected = { date ->
                viewModel.setEndDate(date)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    // Error dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Event" else "Create Event",
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
                    containerColor = DarkSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Event Name
            item {
                SlideInCard(delay = 50) {
                    FormSection(title = "Event Name") {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.setName(it) },
                            placeholder = { Text("e.g., Paris Vacation", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = DarkSurfaceHigh,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Primary
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Description
            item {
                SlideInCard(delay = 100) {
                    FormSection(title = "Description (Optional)") {
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.setDescription(it) },
                            placeholder = { Text("Add details about this event", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = DarkSurfaceHigh,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Primary
                            ),
                            maxLines = 3
                        )
                    }
                }
            }

            // Emoji Selector
            item {
                SlideInCard(delay = 150) {
                    FormSection(title = "Event Icon") {
                        EmojiSelector(
                            selectedEmoji = uiState.selectedEmoji,
                            onEmojiSelected = { viewModel.setEmoji(it) }
                        )
                    }
                }
            }

            // Event Type Selector
            item {
                SlideInCard(delay = 200) {
                    FormSection(title = "Event Type") {
                        EventTypeSelector(
                            eventTypes = uiState.eventTypes,
                            selectedTypeId = uiState.selectedEventTypeId,
                            onTypeSelected = { viewModel.setEventType(it) }
                        )
                    }
                }
            }

            // Date Range
            item {
                SlideInCard(delay = 250) {
                    FormSection(title = "Date Range") {
                        DateRangeSelector(
                            startDate = uiState.startDate.format(dateFormatter),
                            endDate = if (uiState.hasEndDate) uiState.endDate.format(dateFormatter) else null,
                            hasEndDate = uiState.hasEndDate,
                            onStartDateClick = { showStartDatePicker = true },
                            onEndDateClick = { showEndDatePicker = true },
                            onToggleEndDate = { viewModel.setHasEndDate(it) }
                        )
                    }
                }
            }

            // Budget Section
            item {
                SlideInCard(delay = 300) {
                    FormSection(title = "Budget") {
                        BudgetSection(
                            hasBudget = uiState.hasBudget,
                            budgetAmount = uiState.budgetAmount,
                            alertAt75 = uiState.alertAt75,
                            alertAt100 = uiState.alertAt100,
                            onToggleBudget = { viewModel.setHasBudget(it) },
                            onBudgetAmountChange = { viewModel.setBudgetAmount(it) },
                            onToggleAlert75 = { viewModel.setAlertAt75(it) },
                            onToggleAlert100 = { viewModel.setAlertAt100(it) }
                        )
                    }
                }
            }

            // Save Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveEvent() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = TextPrimary
                        ),
                        enabled = !uiState.isSaving
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(FinoGradients.Primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    color = TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = if (uiState.isEditMode) "Save Changes" else "Create Event",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun EmojiSelector(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf(
        "🎉", "🎊", "🎂", "🎁", "✈️", "🏖️",
        "🎓", "💼", "🏠", "💍", "🎸", "⚽",
        "🎮", "📚", "🎨", "🍕", "☕", "🌟"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected emoji preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedEmoji,
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Emoji grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            emojis.chunked(6).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (emoji == selectedEmoji) Primary.copy(alpha = 0.3f)
                                    else DarkSurfaceVariant
                                )
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventTypeSelector(
    eventTypes: List<com.fino.app.domain.model.EventType>,
    selectedTypeId: Long?,
    onTypeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        eventTypes.forEach { eventType ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (eventType.id == selectedTypeId) Primary.copy(alpha = 0.2f)
                        else DarkSurfaceVariant
                    )
                    .clickable { onTypeSelected(eventType.id) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = eventType.emoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = eventType.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (eventType.id == selectedTypeId) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRangeSelector(
    startDate: String,
    endDate: String?,
    hasEndDate: Boolean,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onToggleEndDate: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Start date
        DateField(
            label = "Start Date",
            date = startDate,
            onClick = onStartDateClick
        )

        // End date toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set End Date",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Switch(
                checked = hasEndDate,
                onCheckedChange = onToggleEndDate,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = DarkSurfaceHigh
                )
            )
        }

        // End date (if enabled)
        if (hasEndDate && endDate != null) {
            DateField(
                label = "End Date",
                date = endDate,
                onClick = onEndDateClick
            )
        }
    }
}

@Composable
private fun DateField(
    label: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun BudgetSection(
    hasBudget: Boolean,
    budgetAmount: String,
    alertAt75: Boolean,
    alertAt100: Boolean,
    onToggleBudget: (Boolean) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onToggleAlert75: (Boolean) -> Unit,
    onToggleAlert100: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Budget toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set Budget",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Switch(
                checked = hasBudget,
                onCheckedChange = onToggleBudget,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = DarkSurfaceHigh
                )
            )
        }

        // Budget amount input (if enabled)
        if (hasBudget) {
            OutlinedTextField(
                value = budgetAmount,
                onValueChange = onBudgetAmountChange,
                label = { Text("Budget Amount", color = TextTertiary) },
                placeholder = { Text("e.g., 50000", color = TextTertiary) },
                leadingIcon = { Text("₹", color = TextPrimary, style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = DarkSurfaceHigh,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Primary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Alert checkboxes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Budget Alerts",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                AlertCheckbox(
                    label = "Alert at 75% usage",
                    checked = alertAt75,
                    onCheckedChange = onToggleAlert75
                )

                AlertCheckbox(
                    label = "Alert at 100% usage",
                    checked = alertAt100,
                    onCheckedChange = onToggleAlert100
                )
            }
        }
    }
}

@Composable
private fun AlertCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Primary,
                uncheckedColor = DarkSurfaceHigh,
                checkmarkColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}

/**
 * Date Picker Dialog composable using Material3 DatePicker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: LocalDate,
    minDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
        selectableDates = if (minDate != null) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    return !date.isBefore(minDate)
                }

                override fun isSelectableYear(year: Int): Boolean = true
            }
        } else {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                override fun isSelectableYear(year: Int): Boolean = true
            }
        }
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                }
            ) {
                Text("OK", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
                selectedYearContainerColor = Primary,
                selectedYearContentColor = TextPrimary,
                dayContentColor = TextPrimary,
                selectedDayContainerColor = Primary,
                selectedDayContentColor = TextPrimary,
                todayContentColor = Primary,
                todayDateBorderColor = Primary,
                dayInSelectionRangeContainerColor = Primary.copy(alpha = 0.3f),
                dayInSelectionRangeContentColor = TextPrimary,
                disabledDayContentColor = TextTertiary,
                navigationContentColor = TextPrimary
            )
        )
    }
}
