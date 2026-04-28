package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.components.primitives.FieldRow
import com.fino.app.presentation.components.primitives.HairlineDivider
import com.fino.app.presentation.components.primitives.Pill
import com.fino.app.presentation.components.primitives.PillVariant
import com.fino.app.presentation.components.primitives.SegmentedToggle
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AddTransactionViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess, uiState.deleteSuccess) {
        if (uiState.saveSuccess || uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete transaction", color = FinoColors.ink()) },
            text = { Text("This can't be undone.", color = FinoColors.ink3()) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction()
                    showDeleteDialog = false
                }) { Text("Delete", color = FinoColors.negative()) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = FinoColors.ink3())
                }
            },
            containerColor = FinoColors.card()
        )
    }

    Scaffold(containerColor = FinoColors.paper()) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FinoColors.accentColor())
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TopBar(
                    onCancel = onNavigateBack,
                    isEdit = uiState.isEditMode,
                    canSave = uiState.amount.isNotBlank() &&
                            uiState.selectedCategoryId != null &&
                            !uiState.isSaving,
                    isSaving = uiState.isSaving,
                    onSave = { viewModel.saveTransaction() },
                    onDelete = if (uiState.isEditMode) { { showDeleteDialog = true } } else null
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    AmountBlock(
                        amount = uiState.amount,
                        transactionType = uiState.transactionType,
                        onAmountChange = { viewModel.setAmount(it) },
                        onTypeChange = { viewModel.setTransactionType(it) }
                    )

                    if (uiState.merchant.isNotBlank() && uiState.selectedCategoryId != null) {
                        val selectedCat = uiState.categories.find { it.id == uiState.selectedCategoryId }
                        if (selectedCat != null) {
                            SmartSuggestionCard(
                                merchant = uiState.merchant,
                                category = selectedCat.name,
                                onKeep = { /* no-op; already kept */ },
                                onChange = { showCategorySheet = true }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    FieldStack(
                        merchant = uiState.merchant,
                        onMerchantClick = { /* opens inline editor below */ },
                        category = uiState.categories.find { it.id == uiState.selectedCategoryId },
                        onCategoryClick = { showCategorySheet = true },
                        paymentMethod = uiState.selectedPaymentMethod,
                        onPaymentClick = { showPaymentSheet = true },
                        date = uiState.transactionDate,
                        onDateClick = { showDatePicker = true },
                        onTimeClick = { showTimePicker = true },
                        note = uiState.expenseNotes,
                        onNoteChange = { viewModel.setExpenseNotes(it) }
                    )

                    MerchantInlineEditor(
                        merchant = uiState.merchant,
                        onChange = { viewModel.setMerchant(it) }
                    )

                    if (uiState.isEventExpense) {
                        EventExpenseInlineSection(
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
                            onDueDateChanged = { viewModel.setDueDate(it) }
                        )
                    }

                    uiState.error?.let { error ->
                        Text(
                            text = error,
                            color = FinoColors.negative(),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    if (showCategorySheet) {
        CategoryPickerSheet(
            categories = uiState.categories,
            selectedId = uiState.selectedCategoryId,
            onSelect = {
                viewModel.selectCategory(it)
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showPaymentSheet) {
        PaymentPickerSheet(
            selected = uiState.selectedPaymentMethod,
            onSelect = {
                viewModel.selectPaymentMethod(it)
                showPaymentSheet = false
            },
            onDismiss = { showPaymentSheet = false }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.transactionDate.toLocalDate().toEpochDay() * 86400000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = LocalDate.ofEpochDay(millis / 86400000L)
                        viewModel.setTransactionDate(
                            LocalDateTime.of(newDate, uiState.transactionDate.toLocalTime())
                        )
                    }
                    showDatePicker = false
                }) { Text("OK", color = FinoColors.accentInk()) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = FinoColors.ink3())
                }
            },
            colors = DatePickerDefaults.colors(containerColor = FinoColors.card())
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.transactionDate.hour,
            initialMinute = uiState.transactionDate.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time", color = FinoColors.ink()) },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    viewModel.setTransactionDate(
                        LocalDateTime.of(uiState.transactionDate.toLocalDate(), newTime)
                    )
                    showTimePicker = false
                }) { Text("OK", color = FinoColors.accentInk()) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = FinoColors.ink3())
                }
            },
            containerColor = FinoColors.card()
        )
    }
}

@Composable
private fun TopBar(
    onCancel: () -> Unit,
    isEdit: Boolean,
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Cancel",
            fontSize = 14.sp,
            color = FinoColors.ink3(),
            modifier = Modifier
                .clickable(onClick = onCancel)
                .padding(8.dp)
        )
        Text(
            text = if (isEdit) "Edit transaction" else "New transaction",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onDelete != null) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = FinoColors.negative(),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onDelete)
                        .padding(6.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = FinoColors.accentInk(),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = "Save",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (canSave) FinoColors.accentInk() else FinoColors.ink4(),
                modifier = Modifier
                    .clickable(enabled = canSave, onClick = onSave)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun AmountBlock(
    amount: String,
    transactionType: TransactionType,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit
) {
    val eyebrowLabel = when (transactionType) {
        TransactionType.DEBIT -> "Expense"
        TransactionType.CREDIT -> "Income"
        TransactionType.SAVINGS -> "Savings"
    }

    val intPart = amount.substringBefore('.', amount).ifBlank { "0" }
    val hasDecimal = amount.contains('.')
    val decimalPart = if (hasDecimal) {
        amount.substringAfter('.').take(2).padEnd(2, '0')
    } else {
        "00"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Eyebrow(text = eyebrowLabel)
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "₹",
                style = SerifSm.copy(color = FinoColors.ink3()),
                modifier = Modifier.padding(end = 4.dp, bottom = 12.dp)
            )
            BasicTextField(
                value = intPart.takeIf { it != "0" || amount.startsWith("0") } ?: "",
                onValueChange = { raw ->
                    val cleaned = raw.filter { it.isDigit() }
                    val composed = if (hasDecimal) "$cleaned.$decimalPart" else cleaned
                    onAmountChange(composed)
                },
                textStyle = SerifXL.copy(color = FinoColors.ink(), textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(FinoColors.accentColor()),
                decorationBox = { inner ->
                    Box {
                        if (intPart.isBlank() || (intPart == "0" && !amount.startsWith("0"))) {
                            Text("0", style = SerifXL, color = FinoColors.ink4())
                        }
                        inner()
                    }
                }
            )
            Text(
                text = ".$decimalPart",
                style = SerifMedium.copy(color = FinoColors.ink4()),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        val options = listOf("Expense", "Income", "Savings")
        val selectedIdx = when (transactionType) {
            TransactionType.DEBIT -> 0
            TransactionType.CREDIT -> 1
            TransactionType.SAVINGS -> 2
        }
        SegmentedToggle(
            options = options,
            selectedIndex = selectedIdx,
            onSelect = { idx ->
                onTypeChange(
                    when (idx) {
                        0 -> TransactionType.DEBIT
                        1 -> TransactionType.CREDIT
                        else -> TransactionType.SAVINGS
                    }
                )
            }
        )
    }
}

@Composable
private fun SmartSuggestionCard(
    merchant: String,
    category: String,
    onKeep: () -> Unit,
    onChange: () -> Unit
) {
    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FinoColors.accentSoft())
                .border(1.dp, FinoColors.accentColor().copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(FinoColors.accentColor())
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${merchant.uppercase()} · matched",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinoColors.accentInk()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Category → $category. We'll keep it there.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = FinoColors.ink2()
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(text = "Keep", onClick = onKeep, variant = PillVariant.Solid)
                    Pill(text = "Change category", onClick = onChange, variant = PillVariant.Default)
                }
            }
        }
    }
}

@Composable
private fun FieldStack(
    merchant: String,
    onMerchantClick: () -> Unit,
    category: com.fino.app.domain.model.Category?,
    onCategoryClick: () -> Unit,
    paymentMethod: PaymentMethod?,
    onPaymentClick: () -> Unit,
    date: LocalDateTime,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    note: String,
    onNoteChange: (String) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        HairlineDivider()
        FieldRow(
            label = "Merchant",
            value = merchant.ifBlank { "Tap to enter merchant" },
            valueMuted = merchant.isBlank(),
            modifier = Modifier.padding(horizontal = 24.dp),
            onClick = onMerchantClick
        )
        HairlineDivider()
        FieldRow(
            label = "Category",
            value = category?.let { "${it.emoji} ${it.name}" } ?: "Select a category",
            valueMuted = category == null,
            swatchColor = category?.let { FinoColors.accentColor() },
            modifier = Modifier.padding(horizontal = 24.dp),
            onClick = onCategoryClick
        )
        HairlineDivider()
        FieldRow(
            label = "Account",
            value = paymentMethod?.displayName ?: "Select payment",
            valueMuted = paymentMethod == null,
            rightTag = paymentMethod?.let { accountTag(it) },
            modifier = Modifier.padding(horizontal = 24.dp),
            onClick = onPaymentClick
        )
        HairlineDivider()
        FieldRow(
            label = "Date",
            value = date.format(dateFormatter),
            modifier = Modifier.padding(horizontal = 24.dp),
            onClick = onDateClick
        )
        HairlineDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Note",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.width(80.dp)
            )
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (note.isBlank()) FinoColors.ink4() else FinoColors.ink(),
                    fontFamily = InterTight
                ),
                singleLine = true,
                cursorBrush = SolidColor(FinoColors.accentColor()),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (note.isBlank()) {
                            Text(
                                "Add a note…",
                                fontSize = 14.sp,
                                color = FinoColors.ink4(),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        inner()
                    }
                }
            )
        }
        HairlineDivider()
    }
}

private fun accountTag(method: PaymentMethod): String = when (method) {
    PaymentMethod.UPI -> "UPI"
    PaymentMethod.CREDIT_CARD -> "Card"
    PaymentMethod.DEBIT_CARD -> "Card"
    PaymentMethod.CASH -> "Cash"
    PaymentMethod.NET_BANKING -> "Bank"
    PaymentMethod.OTHER -> "Other"
}

@Composable
private fun MerchantInlineEditor(
    merchant: String,
    onChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Eyebrow(text = "Edit merchant")
        Spacer(Modifier.height(10.dp))
        BasicTextField(
            value = merchant,
            onValueChange = onChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink(),
                fontFamily = InterTight
            ),
            singleLine = true,
            cursorBrush = SolidColor(FinoColors.accentColor()),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(FinoColors.cardTint())
                .border(1.dp, FinoColors.line(), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { inner ->
                Box {
                    if (merchant.isBlank()) {
                        Text(
                            "e.g., Swiggy, Amazon, Salary",
                            fontSize = 14.sp,
                            color = FinoColors.ink4()
                        )
                    }
                    inner()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    categories: List<com.fino.app.domain.model.Category>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FinoColors.card(),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Eyebrow(text = "Category")
            Spacer(Modifier.height(16.dp))
            val list = if (categories.isNotEmpty()) categories else emptyList()
            if (list.isEmpty()) {
                Text(
                    "No categories available",
                    color = FinoColors.ink3(),
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(list, key = { it.id }) { cat ->
                        val selected = cat.id == selectedId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) FinoColors.accentSoft() else Color.Transparent
                                )
                                .clickable { onSelect(cat.id) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.emoji, fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = cat.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selected) FinoColors.accentInk() else FinoColors.ink()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentPickerSheet(
    selected: PaymentMethod?,
    onSelect: (PaymentMethod?) -> Unit,
    onDismiss: () -> Unit
) {
    val methods = listOf(
        PaymentMethod.UPI to "📱",
        PaymentMethod.CREDIT_CARD to "💳",
        PaymentMethod.DEBIT_CARD to "💳",
        PaymentMethod.CASH to "💵",
        PaymentMethod.NET_BANKING to "🏦",
        PaymentMethod.OTHER to "💰"
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FinoColors.card(),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Eyebrow(text = "Account")
            Spacer(Modifier.height(16.dp))
            methods.forEach { (method, emoji) ->
                val isSel = method == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSel) FinoColors.accentSoft() else Color.Transparent
                        )
                        .clickable { onSelect(if (isSel) null else method) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji, fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = method.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSel) FinoColors.accentInk() else FinoColors.ink(),
                        modifier = Modifier.weight(1f)
                    )
                    if (isSel) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = FinoColors.accentInk(),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventExpenseInlineSection(
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
    onDueDateChanged: (LocalDate?) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    var showDueDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Eyebrow(text = "Event details")

        if (subCategories.isNotEmpty()) {
            Eyebrow(text = "Sub-category")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                subCategories.forEach { sub ->
                    val sel = sub.id == selectedSubCategoryId
                    PickerRow(
                        label = "${sub.emoji} ${sub.name}",
                        selected = sel,
                        onClick = { onSubCategorySelected(if (sel) null else sub.id) }
                    )
                }
            }
        }

        if (vendors.isNotEmpty()) {
            Eyebrow(text = "Vendor")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                vendors.forEach { vendor ->
                    val sel = vendor.id == selectedVendorId
                    PickerRow(
                        label = vendor.name,
                        selected = sel,
                        onClick = { onVendorSelected(if (sel) null else vendor.id) }
                    )
                }
            }
        }

        if (familyMembers.isNotEmpty()) {
            Eyebrow(text = "Paid by")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                familyMembers.forEach { member ->
                    val sel = member.name == selectedPaidBy
                    Pill(
                        text = member.name,
                        onClick = { onPaidBySelected(if (sel) null else member.name) },
                        variant = if (sel) PillVariant.Solid else PillVariant.Default
                    )
                }
            }
        }

        Eyebrow(text = "Payment status")
        SegmentedToggle(
            options = listOf("Paid", "Pending", "Partial"),
            selectedIndex = when (paymentStatus) {
                PaymentStatus.PAID -> 0
                PaymentStatus.PENDING, PaymentStatus.OVERDUE -> 1
                PaymentStatus.PARTIAL -> 2
            },
            onSelect = {
                onPaymentStatusChanged(
                    when (it) {
                        0 -> PaymentStatus.PAID
                        1 -> PaymentStatus.PENDING
                        else -> PaymentStatus.PARTIAL
                    }
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FinoColors.cardTint())
                .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
                .clickable { onAdvancePaymentChanged(!isAdvancePayment) }
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Advance payment",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = FinoColors.ink()
                )
                Text(
                    text = "Mark as partial/advance payment",
                    fontSize = 12.sp,
                    color = FinoColors.ink3()
                )
            }
            Switch(
                checked = isAdvancePayment,
                onCheckedChange = onAdvancePaymentChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = FinoColors.paper(),
                    checkedTrackColor = FinoColors.accentColor(),
                    uncheckedThumbColor = FinoColors.paper(),
                    uncheckedTrackColor = FinoColors.ink5()
                )
            )
        }

        if (paymentStatus != PaymentStatus.PAID) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FinoColors.cardTint())
                    .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
                    .clickable { showDueDatePicker = true }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Due date",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = FinoColors.ink()
                    )
                    Text(
                        text = dueDate?.format(dateFormatter) ?: "Not set",
                        fontSize = 12.sp,
                        color = if (dueDate != null) FinoColors.accentInk() else FinoColors.ink3()
                    )
                }
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = "Select date",
                    tint = FinoColors.ink3(),
                    modifier = Modifier.size(18.dp)
                )
            }

            if (showDueDatePicker) {
                val dp = rememberDatePickerState(
                    initialSelectedDateMillis = dueDate?.toEpochDay()?.times(86400000L)
                        ?: System.currentTimeMillis()
                )
                DatePickerDialog(
                    onDismissRequest = { showDueDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            dp.selectedDateMillis?.let { millis ->
                                onDueDateChanged(LocalDate.ofEpochDay(millis / 86400000L))
                            }
                            showDueDatePicker = false
                        }) { Text("OK", color = FinoColors.accentInk()) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDueDatePicker = false }) {
                            Text("Cancel", color = FinoColors.ink3())
                        }
                    },
                    colors = DatePickerDefaults.colors(containerColor = FinoColors.card())
                ) { DatePicker(state = dp) }
            }
        }
    }
}

@Composable
private fun PickerRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) FinoColors.accentSoft() else FinoColors.cardTint())
            .border(
                1.dp,
                if (selected) FinoColors.accentColor() else FinoColors.line(),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) FinoColors.accentInk() else FinoColors.ink(),
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = FinoColors.accentInk(),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
