package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.domain.model.BudgetAlertLevel
import com.fino.app.domain.model.EventStatus
import com.fino.app.domain.model.EventSummary
import com.fino.app.presentation.theme.*
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Card for the events list screen showing event details.
 */
@Composable
fun EventCard(
    eventSummary: EventSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val budgetStatus = eventSummary.budgetStatus

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with emoji and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Event emoji
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryStart.copy(alpha = 0.3f), PrimaryEnd.copy(alpha = 0.2f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = eventSummary.event.emoji,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = eventSummary.event.name,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (eventSummary.event.description != null) {
                            Text(
                                text = eventSummary.event.description,
                                color = TextTertiary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Status badge
                EventStatusBadge(status = eventSummary.event.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Event type badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Secondary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = eventSummary.eventTypeName,
                    color = Secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date range
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildString {
                        append(eventSummary.event.startDate.format(dateFormatter))
                        if (eventSummary.event.endDate != null) {
                            append(" - ")
                            append(eventSummary.event.endDate.format(dateFormatter))
                        } else {
                            append(" - Ongoing")
                        }
                    },
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spending summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = currencyFormatter.format(eventSummary.totalSpent),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${eventSummary.transactionCount} transactions",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                if (eventSummary.event.budgetAmount != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Budget",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = currencyFormatter.format(eventSummary.event.budgetAmount),
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        eventSummary.remainingBudget?.let { remaining ->
                            Text(
                                text = if (remaining >= 0) {
                                    "${currencyFormatter.format(remaining)} left"
                                } else {
                                    "${currencyFormatter.format(-remaining)} over"
                                },
                                color = if (remaining >= 0) IncomeGreen else ExpenseRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Budget progress bar
            if (budgetStatus != null) {
                Spacer(modifier = Modifier.height(12.dp))

                val progress = (budgetStatus.percentageUsed / 100f).coerceIn(0f, 1f)
                val progressGradient = when (budgetStatus.alertLevel) {
                    BudgetAlertLevel.EXCEEDED -> FinoGradients.Expense
                    BudgetAlertLevel.WARNING -> Brush.linearGradient(listOf(Warning, Warning.copy(alpha = 0.8f)))
                    BudgetAlertLevel.NORMAL -> FinoGradients.Income
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DarkSurfaceHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(progressGradient)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${budgetStatus.percentageUsed.toInt()}% used",
                        color = when (budgetStatus.alertLevel) {
                            BudgetAlertLevel.EXCEEDED -> ExpenseRed
                            BudgetAlertLevel.WARNING -> Warning
                            BudgetAlertLevel.NORMAL -> IncomeGreen
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (budgetStatus.daysRemaining != null && budgetStatus.daysRemaining > 0) {
                        Text(
                            text = "${budgetStatus.daysRemaining} days left",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status badge showing event status (ACTIVE/COMPLETED/CANCELLED).
 */
@Composable
private fun EventStatusBadge(
    status: EventStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        EventStatus.ACTIVE -> Triple(
            IncomeGreen.copy(alpha = 0.15f),
            IncomeGreen,
            "Active"
        )
        EventStatus.COMPLETED -> Triple(
            Info.copy(alpha = 0.15f),
            Info,
            "Completed"
        )
        EventStatus.CANCELLED -> Triple(
            TextTertiary.copy(alpha = 0.15f),
            TextTertiary,
            "Cancelled"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (status == EventStatus.COMPLETED) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
