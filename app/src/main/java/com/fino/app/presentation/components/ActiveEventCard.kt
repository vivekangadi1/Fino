package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.fino.app.domain.model.BudgetAlertLevel
import com.fino.app.domain.model.EventSummary
import com.fino.app.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Compact card for home screen showing active event summary.
 */
@Composable
fun MinimalActiveEventCard(
    eventSummary: EventSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val budgetStatus = eventSummary.budgetStatus

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event emoji
            Box(
                modifier = Modifier
                    .size(36.dp)
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
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Event info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = eventSummary.event.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (budgetStatus?.projectedOverBudget == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Over budget projection",
                            tint = Warning,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = eventSummary.eventTypeName,
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(eventSummary.totalSpent),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (budgetStatus != null) {
                    Text(
                        text = "${budgetStatus.percentageUsed.toInt()}% used",
                        color = when (budgetStatus.alertLevel) {
                            BudgetAlertLevel.EXCEEDED -> ExpenseRed
                            BudgetAlertLevel.WARNING -> Warning
                            BudgetAlertLevel.NORMAL -> IncomeGreen
                        },
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Budget progress bar (if budget exists)
        if (budgetStatus != null) {
            val progress = (budgetStatus.percentageUsed / 100f).coerceIn(0f, 1f)
            val progressColor = when (budgetStatus.alertLevel) {
                BudgetAlertLevel.EXCEEDED -> ExpenseRed
                BudgetAlertLevel.WARNING -> Warning
                BudgetAlertLevel.NORMAL -> IncomeGreen
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 12.dp)
                    .background(DarkSurfaceHigh, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(progressColor, RoundedCornerShape(2.dp))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Full active event card showing detailed information.
 */
@Composable
fun ActiveEventCard(
    eventSummary: EventSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val budgetStatus = eventSummary.budgetStatus

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Event emoji
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = eventSummary.event.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = eventSummary.eventTypeName,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Alert indicator
                if (budgetStatus?.projectedOverBudget == true) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Warning.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Budget warning",
                            tint = Warning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spending info
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
                }

                if (budgetStatus != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Budget",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = currencyFormatter.format(budgetStatus.budgetAmount),
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
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

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceHigh)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
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
                                BudgetAlertLevel.NORMAL -> TextSecondary
                            },
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${eventSummary.transactionCount} transactions",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Projection warning
                if (budgetStatus.projectedOverBudget) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Warning.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Projected to exceed budget: ${currencyFormatter.format(budgetStatus.projectedTotal)}",
                                color = Warning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View details link
            TextButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "View Details",
                    color = Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
