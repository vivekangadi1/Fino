package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.domain.model.*
import com.fino.app.presentation.theme.*
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Home screen section showing upcoming bills summary and preview.
 */
@Composable
fun UpcomingBillsSection(
    summary: BillSummary?,
    nextBills: List<UpcomingBill>,
    hasUrgentBills: Boolean,
    onViewAll: () -> Unit,
    onAddBill: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upcoming Bills",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (hasUrgentBills) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Urgent",
                        tint = Warning,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            TextButton(onClick = onViewAll) {
                Text(
                    text = "View All",
                    color = Primary,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary cards row
        if (summary != null) {
            BillSummaryCards(summary = summary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next bills list
        if (nextBills.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nextBills.take(3).forEach { bill ->
                    UpcomingBillCard(bill = bill)
                }
            }
        } else {
            EmptyBillsCard(onAddBill = onAddBill)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add bill button
        TextButton(
            onClick = onAddBill,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "+ Add Recurring Bill",
                color = Secondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Summary cards showing this month and next month totals.
 */
@Composable
fun BillSummaryCards(
    summary: BillSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BillSummaryCard(
            title = "This Month",
            amount = summary.thisMonth.totalAmount,
            billCount = summary.thisMonth.billCount,
            gradient = Brush.linearGradient(listOf(PrimaryStart, PrimaryEnd)),
            modifier = Modifier.weight(1f)
        )
        BillSummaryCard(
            title = "Next Month",
            amount = summary.nextMonth.totalAmount,
            billCount = summary.nextMonth.billCount,
            gradient = Brush.linearGradient(listOf(SecondaryStart, SecondaryEnd)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BillSummaryCard(
    title: String,
    amount: Double,
    billCount: Int,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                color = TextOnGradient.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currencyFormatter.format(amount),
                color = TextOnGradient,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$billCount ${if (billCount == 1) "bill" else "bills"}",
                color = TextOnGradient.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Individual bill card showing bill details with status indicator.
 */
@Composable
fun UpcomingBillCard(
    bill: UpcomingBill,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    val statusColor = when (bill.status) {
        BillStatus.OVERDUE -> ExpenseRed
        BillStatus.DUE_TODAY -> Warning
        BillStatus.DUE_TOMORROW -> Warning.copy(alpha = 0.8f)
        BillStatus.DUE_THIS_WEEK -> Info
        BillStatus.UPCOMING -> TextSecondary
    }

    val statusText = when (bill.status) {
        BillStatus.OVERDUE -> "Overdue"
        BillStatus.DUE_TODAY -> "Due Today"
        BillStatus.DUE_TOMORROW -> "Tomorrow"
        BillStatus.DUE_THIS_WEEK -> bill.dueDate.format(dateFormatter)
        BillStatus.UPCOMING -> bill.dueDate.format(dateFormatter)
    }

    val sourceIcon = when (bill.source) {
        BillSource.RECURRING_RULE -> if (bill.isUserConfirmed) null else "?"
        BillSource.CREDIT_CARD -> bill.creditCardLastFour?.let { "***$it" }
        BillSource.PATTERN_SUGGESTION -> "?"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Bill info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bill.displayName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (!bill.isUserConfirmed) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Suggested",
                            color = Info,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Info.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(bill.amount),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (sourceIcon != null) {
                    Text(
                        text = sourceIcon,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Empty state card when no bills are set up.
 */
@Composable
private fun EmptyBillsCard(
    onAddBill: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No upcoming bills",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Text(
                text = "Add your recurring expenses to track them",
                color = TextTertiary,
                fontSize = 12.sp
            )
        }
    }
}
