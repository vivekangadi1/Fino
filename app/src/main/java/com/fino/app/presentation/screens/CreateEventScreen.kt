package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.InterTight
import com.fino.app.presentation.theme.JetBrainsMono
import com.fino.app.presentation.theme.Newsreader
import com.fino.app.presentation.viewmodel.CreateEventViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onNavigateBack()
    }

    if (showStartDatePicker) {
        DesignDatePickerDialog(
            initialDate = uiState.startDate,
            onDateSelected = {
                viewModel.setStartDate(it)
                if (!uiState.hasEndDate) viewModel.setHasEndDate(true)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showEndDatePicker) {
        DesignDatePickerDialog(
            initialDate = uiState.endDate,
            minDate = uiState.startDate,
            onDateSelected = {
                viewModel.setEndDate(it)
                if (!uiState.hasEndDate) viewModel.setHasEndDate(true)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
    if (showBudgetDialog) {
        BudgetInputDialog(
            initialValue = uiState.budgetAmount,
            onConfirm = { value ->
                viewModel.setBudgetAmount(value)
                viewModel.setHasBudget(value.isNotBlank())
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false }
        )
    }
    if (showCategoryDialog) {
        CategoryPickerDialog(
            types = uiState.eventTypes,
            selectedId = uiState.selectedEventTypeId,
            onPick = {
                viewModel.setEventType(it)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
    if (showAddMemberDialog) {
        AddMemberDialog(
            onConfirm = { name ->
                viewModel.addMember(name)
                showAddMemberDialog = false
            },
            onDismiss = { showAddMemberDialog = false }
        )
    }

    if (uiState.error != null) {
        AlertDialog(
            containerColor = FinoColors.card(),
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error", color = FinoColors.ink()) },
            text = { Text(uiState.error ?: "", color = FinoColors.ink2()) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK", color = FinoColors.accentInk())
                }
            }
        )
    }

    Scaffold(containerColor = FinoColors.paper()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TopBar(
                onCancel = onNavigateBack,
                onCreate = { viewModel.saveEvent() },
                creating = uiState.isSaving
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                NameHeader(
                    value = uiState.name,
                    onValueChange = viewModel::setName
                )

                AccountingSection(
                    includeInExpenses = !uiState.excludeFromMainTotals,
                    onPickIncluded = { viewModel.setExcludeFromMainTotals(false) },
                    onPickStandalone = { viewModel.setExcludeFromMainTotals(true) }
                )

                DatesField(
                    startDate = uiState.startDate,
                    endDate = if (uiState.hasEndDate) uiState.endDate else null,
                    onClickStart = { showStartDatePicker = true },
                    onClickEnd = { showEndDatePicker = true }
                )

                DesignPeopleRow(
                    members = uiState.members,
                    onAdd = { showAddMemberDialog = true },
                    onRemove = { viewModel.removeMember(it) }
                )

                BudgetField(
                    amount = uiState.budgetAmount,
                    onClick = { showBudgetDialog = true }
                )

                CategoryField(
                    typeName = uiState.eventTypes.firstOrNull { it.id == uiState.selectedEventTypeId }?.name,
                    onClick = { showCategoryDialog = true }
                )

                AutoTagSection(
                    startDate = uiState.startDate,
                    endDate = if (uiState.hasEndDate) uiState.endDate else null,
                    enabled = uiState.autoTagTransactions,
                    onToggle = { viewModel.setAutoTagTransactions(!uiState.autoTagTransactions) }
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun TopBar(
    onCancel: () -> Unit,
    onCreate: () -> Unit,
    creating: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Cancel",
            fontFamily = InterTight,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            color = FinoColors.ink3(),
            modifier = Modifier
                .clickable(onClick = onCancel)
                .padding(8.dp)
        )
        Text(
            text = "New event",
            fontFamily = InterTight,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink()
        )
        Text(
            text = if (creating) "Saving…" else "Create",
            fontFamily = InterTight,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinoColors.accentInk(),
            modifier = Modifier
                .clickable(enabled = !creating, onClick = onCreate)
                .padding(8.dp)
        )
    }
}

@Composable
private fun NameHeader(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp)
    ) {
        Eyebrow(text = "Name")
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(FinoColors.ink()),
            textStyle = TextStyle(
                fontFamily = Newsreader,
                fontSize = 36.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.9).sp,
                color = FinoColors.ink()
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "Weekend in Bhutan",
                            fontFamily = Newsreader,
                            fontSize = 36.sp,
                            lineHeight = 40.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.9).sp,
                            color = FinoColors.ink4()
                        )
                    }
                    inner()
                }
            }
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(FinoColors.line2())
        )
    }
}

@Composable
private fun AccountingSection(
    includeInExpenses: Boolean,
    onPickIncluded: () -> Unit,
    onPickStandalone: () -> Unit
) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)) {
        Box(modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)) {
            Eyebrow(text = "Accounting")
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeOption(
                selected = includeInExpenses,
                title = "Include in my expenses",
                body = "Transactions tagged to this event still count toward your monthly spending and budgets. Use for personal trips, celebrations, moves.",
                stat = "+ rolls up to Home",
                onClick = onPickIncluded
            )
            ModeOption(
                selected = !includeInExpenses,
                title = "Standalone event only",
                body = "Expenses live only inside this event. They won't affect your monthly spend, budgets, or insights. Use for group trips where others reimburse you.",
                stat = "Kept separate from Home",
                onClick = onPickStandalone
            )
        }
    }
}

@Composable
private fun ModeOption(
    selected: Boolean,
    title: String,
    body: String,
    stat: String,
    onClick: () -> Unit
) {
    val bg = if (selected) FinoColors.accentSoft() else FinoColors.card()
    val borderColor = if (selected) FinoColors.accentColor().copy(alpha = 0.35f) else FinoColors.line()
    val titleColor = if (selected) FinoColors.accentInk() else FinoColors.ink()
    val statColor = if (selected) FinoColors.accentInk() else FinoColors.ink3()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioDot(selected = selected, modifier = Modifier.padding(top = 1.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = InterTight,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = body,
                fontFamily = InterTight,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = FinoColors.ink3()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stat.uppercase(),
                fontFamily = JetBrainsMono,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.0.sp,
                color = statColor
            )
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean, modifier: Modifier = Modifier) {
    if (selected) {
        Box(
            modifier = modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(FinoColors.accentColor()),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(FinoColors.paper())
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(18.dp)
                .clip(CircleShape)
                .border(1.5.dp, FinoColors.ink4(), CircleShape)
        )
    }
}

@Composable
private fun DatesField(
    startDate: LocalDate,
    endDate: LocalDate?,
    onClickStart: () -> Unit,
    onClickEnd: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("MMM dd")
    val yearFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val value = if (endDate != null) {
        "${startDate.format(fmt)} — ${endDate.format(yearFmt)}"
    } else {
        startDate.format(yearFmt)
    }
    val sub = if (endDate != null) {
        val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
        "$days days"
    } else "1 day"

    DesignFieldRow(
        label = "Dates",
        value = value,
        sub = sub,
        onClick = { if (endDate != null) onClickEnd() else onClickStart() }
    )
}

@Composable
private fun DesignFieldRow(
    label: String,
    value: String,
    sub: String? = null,
    swatch: Color? = null,
    valueMuted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(FinoColors.line())
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = label,
                fontFamily = InterTight,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.width(80.dp)
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (swatch != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(swatch)
                    )
                }
                Text(
                    text = value,
                    fontFamily = InterTight,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (valueMuted) FinoColors.ink4() else FinoColors.ink(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (sub != null) {
                    Text(
                        text = "· $sub",
                        fontFamily = InterTight,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = FinoColors.ink3()
                    )
                }
            }
        }
    }
}

@Composable
private fun DesignPeopleRow(
    members: List<com.fino.app.presentation.viewmodel.EventMemberDraft>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(FinoColors.line())
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "People",
                fontFamily = InterTight,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.width(80.dp)
            )
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                members.forEach { member ->
                    AvatarChip(
                        letter = member.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        seed = member.seed,
                        onClick = { onRemove(member.name) }
                    )
                }
                PlusAvatarChip(onClick = onAdd)
            }
            Text(
                text = if (members.isEmpty()) "Optional · split equally"
                else "${members.size} ${if (members.size == 1) "person" else "people"} · split equally",
                fontFamily = InterTight,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3()
            )
        }
    }
}

private fun avatarColorForSeed(seed: String): Color {
    val palette = listOf(
        Color(0xFFD7E6DA),
        Color(0xFFE6DED0),
        Color(0xFFE3D9E6),
        Color(0xFFDCE2EA),
        Color(0xFFEAD7D4),
        Color(0xFFD8E7E6)
    )
    val idx = (seed.hashCode() and Int.MAX_VALUE) % palette.size
    return palette[idx]
}

@Composable
private fun AvatarChip(letter: String, seed: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(avatarColorForSeed(seed))
            .border(1.dp, FinoColors.line(), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontFamily = InterTight,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink2()
        )
    }
}

@Composable
private fun PlusAvatarChip(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line2(), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Add person",
            tint = FinoColors.ink3(),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun AddMemberDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        containerColor = FinoColors.card(),
        onDismissRequest = onDismiss,
        title = { Text("Add member", color = FinoColors.ink()) },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(FinoColors.paper2())
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        cursorBrush = SolidColor(FinoColors.ink()),
                        textStyle = TextStyle(
                            fontFamily = InterTight,
                            fontSize = 16.sp,
                            color = FinoColors.ink()
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box {
                                if (name.isBlank()) {
                                    Text(
                                        "Name",
                                        color = FinoColors.ink4(),
                                        fontFamily = InterTight,
                                        fontSize = 16.sp
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tap an added chip to remove that member.",
                    fontFamily = InterTight,
                    fontSize = 11.sp,
                    color = FinoColors.ink3()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()) }
            ) { Text("Add", color = FinoColors.accentInk()) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FinoColors.ink3()) }
        }
    )
}

@Composable
private fun BudgetField(amount: String, onClick: () -> Unit) {
    val value = if (amount.isNotBlank()) {
        val d = amount.toDoubleOrNull() ?: 0.0
        "₹${"%,.0f".format(d)}"
    } else {
        "₹80,000"
    }
    val muted = amount.isBlank()
    DesignFieldRow(
        label = "Budget",
        value = value,
        sub = "optional · alerts at 75%",
        valueMuted = muted,
        onClick = onClick
    )
}

@Composable
private fun CategoryField(typeName: String?, onClick: () -> Unit) {
    val value = when (typeName) {
        null -> "Travel › Trips"
        "Trip" -> "Travel › Trips"
        else -> "Events › $typeName"
    }
    DesignFieldRow(
        label = "Category",
        value = value,
        swatch = FinoColors.chart().getOrNull(3) ?: FinoColors.accentColor(),
        onClick = onClick
    )
}

@Composable
private fun AutoTagSection(
    startDate: LocalDate,
    endDate: LocalDate?,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("MMM dd")
    val endShort = DateTimeFormatter.ofPattern("dd")
    val dateRange = if (endDate != null) "${startDate.format(fmt)} — ${endDate.format(endShort)}"
    else startDate.format(fmt)

    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp)) {
        Eyebrow(text = "Auto-tag while active")
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FinoColors.card())
                .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tag all transactions during these dates",
                    fontFamily = InterTight,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = FinoColors.ink(),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                DesignToggle(on = enabled, onClick = onToggle)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "New transactions between $dateRange will suggest this event. You can untag any at review time.",
                fontFamily = InterTight,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = FinoColors.ink3()
            )
        }
    }
}

@Composable
private fun DesignToggle(on: Boolean, onClick: () -> Unit) {
    val track = if (on) FinoColors.accentColor() else FinoColors.ink5()
    Box(
        modifier = Modifier
            .width(34.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(track)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .padding(start = if (on) 16.dp else 2.dp, top = 2.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(FinoColors.paper())
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DesignDatePickerDialog(
    initialDate: LocalDate,
    minDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                if (minDate == null) return true
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                return !date.isBefore(minDate)
            }
            override fun isSelectableYear(year: Int): Boolean = true
        }
    )
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let {
                    val d = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    onDateSelected(d)
                }
            }) { Text("OK", color = FinoColors.accentInk()) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FinoColors.ink3()) }
        }
    ) {
        androidx.compose.material3.DatePicker(state = state)
    }
}

@Composable
private fun BudgetInputDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    AlertDialog(
        containerColor = FinoColors.card(),
        onDismissRequest = onDismiss,
        title = { Text("Budget", color = FinoColors.ink()) },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FinoColors.paper2())
                    .padding(12.dp)
            ) {
                Text("₹", fontFamily = InterTight, fontSize = 20.sp, color = FinoColors.ink3())
                Spacer(Modifier.width(6.dp))
                BasicTextField(
                    value = value,
                    onValueChange = { new -> value = new.filter { it.isDigit() } },
                    singleLine = true,
                    cursorBrush = SolidColor(FinoColors.ink()),
                    textStyle = TextStyle(
                        fontFamily = InterTight,
                        fontSize = 20.sp,
                        color = FinoColors.ink()
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box {
                            if (value.isBlank()) {
                                Text(
                                    "80000",
                                    color = FinoColors.ink4(),
                                    fontFamily = InterTight,
                                    fontSize = 20.sp
                                )
                            }
                            inner()
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) { Text("Save", color = FinoColors.accentInk()) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FinoColors.ink3()) }
        }
    )
}

@Composable
private fun CategoryPickerDialog(
    types: List<com.fino.app.domain.model.EventType>,
    selectedId: Long?,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = FinoColors.card(),
        onDismissRequest = onDismiss,
        title = { Text("Category", color = FinoColors.ink()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { t ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (t.id == selectedId) FinoColors.accentSoft() else FinoColors.paper2())
                            .clickable { onPick(t.id) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(t.emoji, fontSize = 18.sp)
                        Text(
                            t.name,
                            fontFamily = InterTight,
                            fontSize = 14.sp,
                            color = if (t.id == selectedId) FinoColors.accentInk() else FinoColors.ink()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = FinoColors.accentInk()) }
        }
    )
}
