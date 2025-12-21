package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.EventSubCategory
import com.fino.app.domain.model.EventVendor
import com.fino.app.domain.model.FamilyMember
import com.fino.app.domain.model.PaymentMethod
import com.fino.app.domain.model.PaymentStatus
import com.fino.app.domain.model.TransactionType
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AddTransactionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Navigate back on successful save or delete
    LaunchedEffect(uiState.saveSuccess, uiState.deleteSuccess) {
        if (uiState.saveSuccess || uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete this transaction? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction()
                        showDeleteDialog = false
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

    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                TransactionHeader(
                    onClose = onNavigateBack,
                    transactionType = uiState.transactionType,
                    onTypeChange = { viewModel.setTransactionType(it) },
                    isEventExpense = uiState.isEventExpense,
                    isEditMode = uiState.isEditMode,
                    onDeleteClick = if (uiState.isEditMode) {
                        { showDeleteDialog = true }
                    } else null
                )

            // Scrollable content for event expenses (more fields)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Amount Section
                AmountSection(
                    amount = uiState.amount,
                    onAmountChange = { viewModel.setAmount(it) },
                    transactionType = uiState.transactionType
                )

                // Merchant Input (hidden if vendor selected in event expense mode)
                if (!uiState.isEventExpense || uiState.selectedVendorId == null) {
                    MerchantSection(
                        merchant = uiState.merchant,
                        onMerchantChange = { viewModel.setMerchant(it) }
                    )
                }

                // Event Expense Fields (shown only when adding to an event)
                if (uiState.isEventExpense) {
                    EventExpenseSection(
                        subCategories = uiState.subCategories,
                        selectedSubCategoryId = uiState.selectedSubCategoryId,
                        onSubCategorySelected = { viewModel.selectSubCategory(it) },
                        vendors = viewModel.getFilteredVendors(),
                        selectedVendorId = uiState.selectedVendorId,
                        onVendorSelected = { viewModel.selectVendor(it) },
                        familyMembers = uiState.familyMembers,
                        selectedPaidBy = uiState.selectedPaidBy,
                        onPaidBySelected = { viewModel.setPaidBy(it) },
                        paymentStatus = uiState.paymentStatus,
                        onPaymentStatusChanged = { viewModel.setPaymentStatus(it) },
                        isAdvancePayment = uiState.isAdvancePayment,
                        onAdvancePaymentChanged = { viewModel.setIsAdvancePayment(it) },
                        dueDate = uiState.dueDate,
                        onDueDateChanged = { viewModel.setDueDate(it) },
                        expenseNotes = uiState.expenseNotes,
                        onExpenseNotesChanged = { viewModel.setExpenseNotes(it) }
                    )
                }

                // Category Grid (hidden for event expenses - auto-selected to "Other")
                if (!uiState.isEventExpense) {
                    CategorySection(
                        selectedCategoryId = uiState.selectedCategoryId,
                        categories = uiState.categories,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }

                // Payment Method Selector
                PaymentMethodSection(
                    selectedPaymentMethod = uiState.selectedPaymentMethod,
                    onPaymentMethodSelected = { viewModel.selectPaymentMethod(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = ExpenseRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

                // Save Button
                SaveButton(
                    enabled = uiState.amount.isNotBlank() && uiState.selectedCategoryId != null && !uiState.isSaving,
                    isLoading = uiState.isSaving,
                    isEditMode = uiState.isEditMode,
                    onClick = { viewModel.saveTransaction() }
                )
            }
        }
    }
}

// Color for Savings type
private val SavingsBlue = Color(0xFF4A90D9)

@Composable
private fun TransactionHeader(
    onClose: () -> Unit,
    transactionType: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    isEventExpense: Boolean = false,
    isEditMode: Boolean = false,
    onDeleteClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSurface, DarkBackground)
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = when {
                        isEditMode && isEventExpense -> "Edit Event Expense"
                        isEditMode -> "Edit Transaction"
                        isEventExpense -> "Add Event Expense"
                        else -> "Add Transaction"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                // Delete button (only in edit mode) or spacer
                if (onDeleteClick != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed.copy(alpha = 0.2f))
                            .clickable(onClick = onDeleteClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Type Toggle - 3 options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Expense
                TypeToggleButton(
                    text = "Expense",
                    isSelected = transactionType == TransactionType.DEBIT,
                    selectedColor = ExpenseRed,
                    onClick = { onTypeChange(TransactionType.DEBIT) },
                    modifier = Modifier.weight(1f)
                )
                // Income
                TypeToggleButton(
                    text = "Income",
                    isSelected = transactionType == TransactionType.CREDIT,
                    selectedColor = IncomeGreen,
                    onClick = { onTypeChange(TransactionType.CREDIT) },
                    modifier = Modifier.weight(1f)
                )
                // Savings
                TypeToggleButton(
                    text = "Savings",
                    isSelected = transactionType == TransactionType.SAVINGS,
                    selectedColor = SavingsBlue,
                    onClick = { onTypeChange(TransactionType.SAVINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TypeToggleButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) selectedColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    transactionType: TransactionType
) {
    val questionText = when (transactionType) {
        TransactionType.DEBIT -> "How much did you spend?"
        TransactionType.CREDIT -> "How much did you earn?"
        TransactionType.SAVINGS -> "How much did you save?"
    }

    val accentColor = when (transactionType) {
        TransactionType.DEBIT -> ExpenseRed
        TransactionType.CREDIT -> IncomeGreen
        TransactionType.SAVINGS -> SavingsBlue
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = questionText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                BasicTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // Only allow numbers and one decimal point
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            onAmountChange(filtered)
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier.widthIn(min = 100.dp, max = 250.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            if (amount.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = TextStyle(
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextTertiary
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MerchantSection(
    merchant: String,
    onMerchantChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(16.dp)
        ) {
            BasicTextField(
                value = merchant,
                onValueChange = onMerchantChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = TextPrimary
                ),
                singleLine = true,
                cursorBrush = SolidColor(Primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (merchant.isEmpty()) {
                            Text(
                                text = "e.g., Swiggy, Amazon, Salary...",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = TextTertiary
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun CategorySection(
    selectedCategoryId: Long?,
    categories: List<com.fino.app.domain.model.Category>,
    onCategorySelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Use categories from ViewModel if available, otherwise fall back to hardcoded list
        val displayCategories = if (categories.isNotEmpty()) {
            categories.map { cat ->
                TransactionCategory(
                    id = cat.id,
                    name = cat.name,
                    emoji = cat.emoji,
                    color = getCategoryColor(cat.name)
                )
            }
        } else {
            transactionCategories
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(displayCategories) { category ->
                CategoryItem(
                    category = category,
                    isSelected = selectedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) }
                )
            }
        }
    }
}

private fun getCategoryColor(name: String): Color {
    return when (name.lowercase()) {
        "food" -> CategoryFood
        "transport" -> CategoryTransport
        "shopping" -> CategoryShopping
        "health" -> CategoryHealth
        "fun", "entertainment" -> CategoryEntertainment
        "bills" -> CategoryBills
        "education" -> CategoryEducation
        "travel" -> CategoryTravel
        "groceries" -> CategoryGroceries
        "personal" -> CategoryPersonal
        "salary" -> IncomeGreen
        else -> CategoryOther
    }
}

@Composable
private fun CategoryItem(
    category: TransactionCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) category.color.copy(alpha = 0.2f) else DarkSurfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) category.color else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = category.emoji,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) category.color else TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    isLoading: Boolean = false,
    isEditMode: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        BouncyButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            gradient = if (enabled) FinoGradients.Primary else Brush.linearGradient(
                listOf(TextTertiary, TextTertiary)
            ),
            enabled = enabled && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = TextPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saving...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Update Transaction" else "Save Transaction",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

data class TransactionCategory(
    val id: Long,
    val name: String,
    val emoji: String,
    val color: Color
)

val transactionCategories = listOf(
    TransactionCategory(1L, "Food", "🍔", CategoryFood),
    TransactionCategory(2L, "Transport", "🚗", CategoryTransport),
    TransactionCategory(3L, "Shopping", "🛍️", CategoryShopping),
    TransactionCategory(4L, "Health", "💊", CategoryHealth),
    TransactionCategory(5L, "Fun", "🎬", CategoryEntertainment),
    TransactionCategory(6L, "Bills", "📱", CategoryBills),
    TransactionCategory(7L, "Education", "📚", CategoryEducation),
    TransactionCategory(8L, "Travel", "✈️", CategoryTravel),
    TransactionCategory(9L, "Groceries", "🛒", CategoryGroceries),
    TransactionCategory(10L, "Personal", "💅", CategoryPersonal),
    TransactionCategory(11L, "Salary", "💰", IncomeGreen),
    TransactionCategory(12L, "Other", "📦", CategoryOther)
)

// ============================================================================
// Event Expense Section
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventExpenseSection(
    subCategories: List<EventSubCategory>,
    selectedSubCategoryId: Long?,
    onSubCategorySelected: (Long?) -> Unit,
    vendors: List<EventVendor>,
    selectedVendorId: Long?,
    onVendorSelected: (Long?) -> Unit,
    familyMembers: List<FamilyMember>,
    selectedPaidBy: String?,
    onPaidBySelected: (String?) -> Unit,
    paymentStatus: PaymentStatus,
    onPaymentStatusChanged: (PaymentStatus) -> Unit,
    isAdvancePayment: Boolean,
    onAdvancePaymentChanged: (Boolean) -> Unit,
    dueDate: LocalDate?,
    onDueDateChanged: (LocalDate?) -> Unit,
    expenseNotes: String,
    onExpenseNotesChanged: (String) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Event,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Event Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        // Sub-category dropdown
        if (subCategories.isNotEmpty()) {
            DropdownSelector(
                label = "Sub-Category",
                selectedValue = subCategories.find { it.id == selectedSubCategoryId }?.let { "${it.emoji} ${it.name}" },
                placeholder = "Select sub-category",
                options = subCategories.map { it.id to "${it.emoji} ${it.name}" },
                onOptionSelected = { onSubCategorySelected(it) },
                allowClear = true,
                onClear = { onSubCategorySelected(null) }
            )
        }

        // Vendor dropdown
        if (vendors.isNotEmpty()) {
            DropdownSelector(
                label = "Vendor",
                selectedValue = vendors.find { it.id == selectedVendorId }?.name,
                placeholder = "Select vendor",
                options = vendors.map { it.id to it.name },
                onOptionSelected = { onVendorSelected(it) },
                allowClear = true,
                onClear = { onVendorSelected(null) }
            )
        }

        // Paid By dropdown
        if (familyMembers.isNotEmpty()) {
            DropdownSelectorString(
                label = "Paid By",
                selectedValue = selectedPaidBy,
                placeholder = "Who paid?",
                options = familyMembers.map { it.name },
                onOptionSelected = { onPaidBySelected(it) }
            )
        }

        // Payment Status selector
        PaymentStatusSelector(
            selectedStatus = paymentStatus,
            onStatusSelected = onPaymentStatusChanged
        )

        // Advance Payment toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
                .clickable { onAdvancePaymentChanged(!isAdvancePayment) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Advance Payment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Mark as partial/advance payment",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = isAdvancePayment,
                onCheckedChange = onAdvancePaymentChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Primary,
                    checkedTrackColor = Primary.copy(alpha = 0.5f),
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = DarkSurfaceHigh
                )
            )
        }

        // Due Date picker (shown for pending/partial payments)
        if (paymentStatus != PaymentStatus.PAID) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { showDatePicker = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = dueDate?.format(dateFormatter) ?: "Not set",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (dueDate != null) Info else TextSecondary
                    )
                }
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Select date",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = dueDate?.toEpochDay()?.times(86400000L)
                        ?: System.currentTimeMillis()
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    onDueDateChanged(LocalDate.ofEpochDay(millis / 86400000L))
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
                    }
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
        }

        // Expense Notes
        Column {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(16.dp)
            ) {
                BasicTextField(
                    value = expenseNotes,
                    onValueChange = onExpenseNotesChanged,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (expenseNotes.isEmpty()) {
                                Text(
                                    text = "Add notes about this expense...",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = TextTertiary
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PaymentStatusSelector(
    selectedStatus: PaymentStatus,
    onStatusSelected: (PaymentStatus) -> Unit
) {
    Column {
        Text(
            text = "Payment Status",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PaymentStatus.values().filter { it != PaymentStatus.OVERDUE }.forEach { status ->
                val isSelected = status == selectedStatus
                val (color, label) = when (status) {
                    PaymentStatus.PAID -> IncomeGreen to "Paid"
                    PaymentStatus.PENDING -> Warning to "Pending"
                    PaymentStatus.PARTIAL -> Info to "Partial"
                    PaymentStatus.OVERDUE -> ExpenseRed to "Overdue"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color else Color.Transparent)
                        .clickable { onStatusSelected(status) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) DarkBackground else TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    selectedValue: String?,
    placeholder: String,
    options: List<Pair<Long, String>>,
    onOptionSelected: (Long) -> Unit,
    allowClear: Boolean = false,
    onClear: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { expanded = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue ?: placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedValue != null) TextPrimary else TextTertiary,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    if (allowClear && selectedValue != null && onClear != null) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    onClear()
                                    expanded = false
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(DarkSurfaceHigh)
            ) {
                options.forEach { (id, text) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = text,
                                color = TextPrimary
                            )
                        },
                        onClick = {
                            onOptionSelected(id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelectorString(
    label: String,
    selectedValue: String?,
    placeholder: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { expanded = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue ?: placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedValue != null) TextPrimary else TextTertiary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(DarkSurfaceHigh)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = TextPrimary
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodSection(
    selectedPaymentMethod: PaymentMethod?,
    onPaymentMethodSelected: (PaymentMethod?) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Payment Method (Optional)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Grid of payment method options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethodItem(
                method = PaymentMethod.UPI,
                isSelected = selectedPaymentMethod == PaymentMethod.UPI,
                onClick = { onPaymentMethodSelected(if (selectedPaymentMethod == PaymentMethod.UPI) null else PaymentMethod.UPI) },
                modifier = Modifier.weight(1f)
            )
            PaymentMethodItem(
                method = PaymentMethod.CREDIT_CARD,
                isSelected = selectedPaymentMethod == PaymentMethod.CREDIT_CARD,
                onClick = { onPaymentMethodSelected(if (selectedPaymentMethod == PaymentMethod.CREDIT_CARD) null else PaymentMethod.CREDIT_CARD) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethodItem(
                method = PaymentMethod.DEBIT_CARD,
                isSelected = selectedPaymentMethod == PaymentMethod.DEBIT_CARD,
                onClick = { onPaymentMethodSelected(if (selectedPaymentMethod == PaymentMethod.DEBIT_CARD) null else PaymentMethod.DEBIT_CARD) },
                modifier = Modifier.weight(1f)
            )
            PaymentMethodItem(
                method = PaymentMethod.CASH,
                isSelected = selectedPaymentMethod == PaymentMethod.CASH,
                onClick = { onPaymentMethodSelected(if (selectedPaymentMethod == PaymentMethod.CASH) null else PaymentMethod.CASH) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethodItem(
                method = PaymentMethod.NET_BANKING,
                isSelected = selectedPaymentMethod == PaymentMethod.NET_BANKING,
                onClick = { onPaymentMethodSelected(if (selectedPaymentMethod == PaymentMethod.NET_BANKING) null else PaymentMethod.NET_BANKING) },
                modifier = Modifier.weight(1f)
            )
            PaymentMethodItem(
                method = PaymentMethod.OTHER,
                isSelected = selectedPaymentMethod == PaymentMethod.OTHER,
                onClick = { onPaymentMethodSelected(if (selectedPaymentMethod == PaymentMethod.OTHER) null else PaymentMethod.OTHER) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PaymentMethodItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (method) {
        PaymentMethod.UPI -> "📱"
        PaymentMethod.CREDIT_CARD -> "💳"
        PaymentMethod.DEBIT_CARD -> "💳"
        PaymentMethod.CASH -> "💵"
        PaymentMethod.NET_BANKING -> "🏦"
        PaymentMethod.OTHER -> "💰"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.2f) else DarkSurfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Primary else TextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 10.sp
            )
        }
    }
}
