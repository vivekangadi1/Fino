package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.BillSource
import com.fino.app.domain.model.BillStatus
import com.fino.app.domain.model.UpcomingBill
import com.fino.app.presentation.components.primitives.BillRow
import com.fino.app.presentation.components.primitives.DayGroup
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.components.primitives.HairlineDivider
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.JetBrainsMono
import com.fino.app.presentation.theme.Newsreader
import com.fino.app.presentation.theme.NumericStyle
import com.fino.app.presentation.viewmodel.UpcomingBillsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingBillsScreen(
    onNavigateBack: () -> Unit,
    onAddBill: () -> Unit,
    onEditBill: (Long) -> Unit = {},
    onEditCreditCardBill: (Long) -> Unit = {},
    onScanPatterns: () -> Unit = {},
    viewModel: UpcomingBillsViewModel = hiltViewModel()
) {
    Scaffold(containerColor = FinoColors.paper()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            UpcomingTopBar(onBack = onNavigateBack, onFilter = onScanPatterns)
            UpcomingBillsBody(
                viewModel = viewModel,
                onAddBill = onAddBill,
                onEditBill = onEditBill,
                onEditCreditCardBill = onEditCreditCardBill
            )
        }
    }
}

@Composable
fun UpcomingBillsBody(
    viewModel: UpcomingBillsViewModel = hiltViewModel(),
    onAddBill: () -> Unit,
    onEditBill: (Long) -> Unit = {},
    onEditCreditCardBill: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = FinoColors.accentColor())
        }
        return
    }

    val allBills = remember(uiState.enhancedGroups) {
        uiState.enhancedGroups.flatMap { it.allBills }
    }
    val today = remember { LocalDate.now() }
    val cutoff = remember(today) { today.plusDays(30) }
    val nextThirtyBills = remember(allBills, today, cutoff) {
        allBills.filter { it.dueDate in today..cutoff }.sortedBy { it.dueDate }
    }
    val totalNext30 = nextThirtyBills.sumOf { it.amount }
    val countNext30 = nextThirtyBills.size
    val billsByDate: Map<LocalDate, List<UpcomingBill>> = nextThirtyBills.groupBy { it.dueDate }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { UpcomingHeader(totalAmount = totalNext30, billCount = countNext30) }
        item {
            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                FourteenDayStrip(start = today, billsByDate = billsByDate)
            }
        }

        if (nextThirtyBills.isEmpty()) {
            item { EmptyBillsBlock(onAddBill = onAddBill) }
        } else {
            item { Spacer(Modifier.height(12.dp)) }
            val sortedDates = billsByDate.keys.sorted()
            sortedDates.forEachIndexed { index, date ->
                val billsForDay = billsByDate[date].orEmpty()
                item {
                    if (index == 0) HairlineDivider()
                    DayGroupSection(
                        date = date,
                        bills = billsForDay,
                        onBillClick = { bill ->
                            when (bill.source) {
                                BillSource.RECURRING_RULE -> onEditBill(bill.sourceId)
                                BillSource.CREDIT_CARD -> onEditCreditCardBill(bill.sourceId)
                                else -> {}
                            }
                        }
                    )
                    HairlineDivider()
                }
            }
        }
    }
}

@Composable
private fun UpcomingTopBar(onBack: () -> Unit, onFilter: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 18.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FinoColors.ink2(),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Upcoming",
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onFilter),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "Filter",
                tint = FinoColors.ink2(),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun UpcomingHeader(totalAmount: Double, billCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)
    ) {
        Eyebrow(text = "Next 30 days")
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "₹${formatIndianLoose2(totalAmount)}",
                fontFamily = Newsreader,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-1.32).sp,
                color = FinoColors.ink(),
                style = NumericStyle
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "across $billCount ${if (billCount == 1) "bill" else "bills"}",
                fontSize = 12.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun FourteenDayStrip(
    start: LocalDate,
    billsByDate: Map<LocalDate, List<UpcomingBill>>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (0 until 14).forEach { i ->
            val day = start.plusDays(i.toLong())
            val bills = billsByDate[day].orEmpty()
            val today = i == 0
            val hasWarn = bills.any { it.status == BillStatus.OVERDUE || it.status == BillStatus.DUE_TODAY }
            val bg = if (today) FinoColors.ink() else FinoColors.card()
            val borderMod = if (today) Modifier else Modifier.border(1.dp, FinoColors.line(), RoundedCornerShape(8.dp))
            val labelColor = if (today) FinoColors.paper().copy(alpha = 0.6f) else FinoColors.ink3()
            val numberColor = if (today) FinoColors.paper() else FinoColors.ink2()
            val dotColor = if (today) FinoColors.paper() else {
                if (hasWarn) FinoColors.warn() else FinoColors.accentColor()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .then(borderMod)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day.format(DateTimeFormatter.ofPattern("E")).take(1).uppercase(),
                    fontFamily = JetBrainsMono,
                    fontSize = 9.sp,
                    color = labelColor
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = day.dayOfMonth.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = numberColor,
                    style = NumericStyle
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(4.dp)
                ) {
                    val dotCount = bills.size.coerceAtMost(3)
                    repeat(dotCount) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayGroupSection(
    date: LocalDate,
    bills: List<UpcomingBill>,
    onBillClick: (UpcomingBill) -> Unit
) {
    val dateFmt = remember { DateTimeFormatter.ofPattern("MMM d") }
    val weekdayFmt = remember { DateTimeFormatter.ofPattern("EEEE") }
    val total = bills.sumOf { it.amount }
    val hasWarn = bills.any { it.status == BillStatus.OVERDUE || it.status == BillStatus.DUE_TODAY }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 18.dp, bottom = 8.dp)
    ) {
        DayGroup(
            dateLabel = date.format(dateFmt),
            weekdayLabel = date.format(weekdayFmt),
            total = "₹${formatIndianLoose2(total)}",
            isWarn = hasWarn
        ) {
            bills.forEach { bill ->
                BillRow(
                    tag = tagForBill(bill),
                    name = bill.displayName,
                    sub = subForBill(bill),
                    amount = "₹${formatIndianLoose2(bill.amount)}",
                    isWarn = bill.status == BillStatus.OVERDUE || bill.status == BillStatus.DUE_TODAY,
                    onClick = { onBillClick(bill) }
                )
            }
        }
    }
}

@Composable
private fun EmptyBillsBlock(onAddBill: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FinoColors.cardTint())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = FinoColors.ink3(),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Nothing due soon",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Track bills to see them here.",
            fontSize = 12.sp,
            color = FinoColors.ink3(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Add a bill",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.accentInk(),
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .clickable(onClick = onAddBill)
                .background(FinoColors.accentSoft())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private fun tagForBill(bill: UpcomingBill): String = when {
    bill.source == BillSource.CREDIT_CARD -> "card"
    bill.merchantName.contains("emi", ignoreCase = true) ||
        bill.displayName.contains("emi", ignoreCase = true) ||
        bill.displayName.contains("loan", ignoreCase = true) -> "bank"
    else -> "upi"
}

private fun subForBill(bill: UpcomingBill): String {
    val parts = mutableListOf<String>()
    when (bill.source) {
        BillSource.CREDIT_CARD -> {
            parts += "Card statement"
            bill.creditCardLastFour?.let { parts += "•••• $it" }
        }
        BillSource.RECURRING_RULE -> {
            parts += bill.frequency?.let { freqLabel(it) } ?: "Recurring"
        }
        BillSource.PATTERN_SUGGESTION -> parts += "Pattern · suggested"
    }
    if (bill.status == BillStatus.OVERDUE) parts += "Overdue"
    return parts.joinToString(" · ")
}

private fun freqLabel(frequency: com.fino.app.domain.model.RecurringFrequency): String = when (frequency) {
    com.fino.app.domain.model.RecurringFrequency.ONE_TIME -> "One-time"
    com.fino.app.domain.model.RecurringFrequency.WEEKLY -> "Weekly"
    com.fino.app.domain.model.RecurringFrequency.MONTHLY -> "Monthly"
    com.fino.app.domain.model.RecurringFrequency.YEARLY -> "Yearly"
}

private fun formatIndianLoose2(amount: Double): String {
    if (amount <= 0) return "0"
    return String.format("%,.0f", amount)
}
