package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.*
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.UpcomingBillsViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingBillsScreen(
    onNavigateBack: () -> Unit,
    onAddBill: () -> Unit,
    viewModel: UpcomingBillsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Upcoming Bills",
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
                    IconButton(onClick = { viewModel.toggleCalendarView() }) {
                        Icon(
                            imageVector = if (uiState.showCalendarView)
                                Icons.Default.ViewList else Icons.Default.CalendarMonth,
                            contentDescription = "Toggle View",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBill,
                containerColor = Primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Bill",
                    tint = TextOnGradient
                )
            }
        }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary cards
                item {
                    uiState.summary?.let { summary ->
                        BillSummaryCardsLarge(summary = summary)
                    }
                }

                // Calendar view (if enabled)
                if (uiState.showCalendarView) {
                    item {
                        BillCalendarSection(
                            selectedMonth = uiState.selectedMonth,
                            billsByDate = uiState.calendarBills,
                            onMonthChange = { viewModel.selectMonth(it) }
                        )
                    }
                }

                // Pattern suggestions
                if (uiState.patternSuggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Suggested Bills",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(uiState.patternSuggestions) { suggestion ->
                        PatternSuggestionCard(
                            suggestion = suggestion,
                            onConfirm = { viewModel.confirmPatternSuggestion(suggestion) },
                            onDismiss = { viewModel.dismissPatternSuggestion(suggestion) }
                        )
                    }
                }

                // Grouped bills
                uiState.groupedBills.forEach { group ->
                    item {
                        Text(
                            text = group.label,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(group.bills) { bill ->
                        UpcomingBillCardLarge(
                            bill = bill,
                            onMarkPaid = { viewModel.markBillAsPaid(bill) }
                        )
                    }
                }

                // Empty state
                if (uiState.groupedBills.isEmpty() && uiState.patternSuggestions.isEmpty()) {
                    item {
                        EmptyBillsState(onAddBill = onAddBill)
                    }
                }
            }
        }

        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Could show a snackbar here
            }
        }
    }
}

@Composable
private fun BillSummaryCardsLarge(
    summary: BillSummary,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryStatCard(
                title = "This Month",
                value = currencyFormatter.format(summary.thisMonth.totalAmount),
                subtitle = "${summary.thisMonth.billCount} bills",
                gradient = Brush.linearGradient(listOf(PrimaryStart, PrimaryEnd)),
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                title = "Next Month",
                value = currencyFormatter.format(summary.nextMonth.totalAmount),
                subtitle = "${summary.nextMonth.billCount} bills",
                gradient = Brush.linearGradient(listOf(SecondaryStart, SecondaryEnd)),
                modifier = Modifier.weight(1f)
            )
        }

        if (summary.overdueCount > 0 || summary.dueTodayCount > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (summary.overdueCount > 0) {
                    AlertCard(
                        label = "Overdue",
                        count = summary.overdueCount,
                        color = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (summary.dueTodayCount > 0) {
                    AlertCard(
                        label = "Due Today",
                        count = summary.dueTodayCount,
                        color = Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (summary.overdueCount == 0 || summary.dueTodayCount == 0) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = TextOnGradient.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = TextOnGradient,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextOnGradient.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AlertCard(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$count $label",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BillCalendarSection(
    selectedMonth: YearMonth,
    billsByDate: Map<LocalDate, List<UpcomingBill>>,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedMonth.year}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar grid (simplified)
            SimpleBillCalendar(
                yearMonth = selectedMonth,
                billsByDate = billsByDate
            )
        }
    }
}

@Composable
private fun SimpleBillCalendar(
    yearMonth: YearMonth,
    billsByDate: Map<LocalDate, List<UpcomingBill>>,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
    val firstDayOfMonth = yearMonth.atDay(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    Column(modifier = modifier) {
        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    color = TextTertiary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days
        var dayCounter = 1
        repeat(6) { week ->
            if (dayCounter <= daysInMonth) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayOfWeek ->
                        val cellIndex = week * 7 + dayOfWeek
                        if (cellIndex >= startOffset && dayCounter <= daysInMonth) {
                            val date = yearMonth.atDay(dayCounter)
                            val billsForDay = billsByDate[date] ?: emptyList()
                            val isToday = date == today

                            CalendarDayCell(
                                day = dayCounter,
                                isToday = isToday,
                                bills = billsForDay,
                                modifier = Modifier.weight(1f)
                            )
                            dayCounter++
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    bills: List<UpcomingBill>,
    modifier: Modifier = Modifier
) {
    val hasBills = bills.isNotEmpty()
    val hasOverdue = bills.any { it.status == BillStatus.OVERDUE }
    val hasDueToday = bills.any { it.status == BillStatus.DUE_TODAY }

    val dotColor = when {
        hasOverdue -> ExpenseRed
        hasDueToday -> Warning
        hasBills -> Info
        else -> null
    }

    Column(
        modifier = modifier.padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isToday) Primary.copy(alpha = 0.3f) else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                color = if (isToday) Primary else TextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        }
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun UpcomingBillCardLarge(
    bill: UpcomingBill,
    onMarkPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    val statusColor = when (bill.status) {
        BillStatus.OVERDUE -> ExpenseRed
        BillStatus.DUE_TODAY -> Warning
        BillStatus.DUE_TOMORROW -> Warning.copy(alpha = 0.8f)
        BillStatus.DUE_THIS_WEEK -> Info
        BillStatus.UPCOMING -> TextSecondary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Bill details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.displayName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = bill.dueDate.format(dateFormatter),
                    color = statusColor,
                    fontSize = 13.sp
                )
                if (bill.source == BillSource.CREDIT_CARD && bill.creditCardLastFour != null) {
                    Text(
                        text = "Card ***${bill.creditCardLastFour}",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            // Amount and action
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(bill.amount),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (bill.source == BillSource.RECURRING_RULE && bill.isUserConfirmed) {
                    TextButton(
                        onClick = onMarkPaid,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Mark Paid",
                            color = Secondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBillsState(
    onAddBill: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No upcoming bills",
            color = TextSecondary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Track your recurring expenses by adding bills",
            color = TextTertiary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddBill,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Bill")
        }
    }
}
