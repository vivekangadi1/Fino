package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.domain.model.BudgetAlertLevel
import com.fino.app.domain.model.EventBudgetStatus
import com.fino.app.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Linear budget progress visualization component.
 */
@Composable
fun EventBudgetProgress(
    budgetStatus: EventBudgetStatus,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Progress",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Alert level badge
                BudgetAlertBadge(alertLevel = budgetStatus.alertLevel)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = currencyFormatter.format(budgetStatus.spent),
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Budget",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = currencyFormatter.format(budgetStatus.budgetAmount),
                        color = TextSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            val progress = (budgetStatus.percentageUsed / 100f).coerceIn(0f, 1f)
            val progressGradient = when (budgetStatus.alertLevel) {
                BudgetAlertLevel.EXCEEDED -> FinoGradients.Expense
                BudgetAlertLevel.WARNING -> Brush.linearGradient(listOf(Warning, Warning.copy(alpha = 0.8f)))
                BudgetAlertLevel.NORMAL -> FinoGradients.Income
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceHigh)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(progressGradient)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Percentage and remaining
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (budgetStatus.remaining >= 0) {
                        "${currencyFormatter.format(budgetStatus.remaining)} left"
                    } else {
                        "${currencyFormatter.format(-budgetStatus.remaining)} over"
                    },
                    color = if (budgetStatus.remaining >= 0) TextSecondary else ExpenseRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Divider)

            Spacer(modifier = Modifier.height(16.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetStatItem(
                    label = "Daily Average",
                    value = currencyFormatter.format(budgetStatus.dailyAverage),
                    modifier = Modifier.weight(1f)
                )
                BudgetStatItem(
                    label = "Days Elapsed",
                    value = "${budgetStatus.daysElapsed}",
                    modifier = Modifier.weight(1f)
                )
                if (budgetStatus.daysRemaining != null) {
                    BudgetStatItem(
                        label = "Days Left",
                        value = "${budgetStatus.daysRemaining}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Projection warning
            if (budgetStatus.projectedOverBudget) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Warning.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Projected Total",
                                color = Warning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currencyFormatter.format(budgetStatus.projectedTotal),
                                color = Warning,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular budget progress visualization component.
 */
@Composable
fun EventBudgetProgressCircular(
    budgetStatus: EventBudgetStatus,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Status",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                BudgetAlertBadge(alertLevel = budgetStatus.alertLevel)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Circular progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                val progress = (budgetStatus.percentageUsed / 100f).coerceIn(0f, 1f)
                val progressColor = when (budgetStatus.alertLevel) {
                    BudgetAlertLevel.EXCEEDED -> ExpenseRed
                    BudgetAlertLevel.WARNING -> Warning
                    BudgetAlertLevel.NORMAL -> IncomeGreen
                }

                // Background circle
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = DarkSurfaceHigh,
                    strokeWidth = 16.dp,
                    trackColor = DarkSurfaceHigh,
                    strokeCap = StrokeCap.Round
                )

                // Progress circle
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = progressColor,
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round
                )

                // Center content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${budgetStatus.percentageUsed.toInt()}%",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "used",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Spent",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = currencyFormatter.format(budgetStatus.spent),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (budgetStatus.remaining >= 0) "Remaining" else "Over",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = currencyFormatter.format(kotlin.math.abs(budgetStatus.remaining)),
                        color = if (budgetStatus.remaining >= 0) IncomeGreen else ExpenseRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = Divider)

            Spacer(modifier = Modifier.height(16.dp))

            // Daily stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetStatItem(
                    label = "Daily Avg",
                    value = currencyFormatter.format(budgetStatus.dailyAverage),
                    modifier = Modifier.weight(1f)
                )
                if (budgetStatus.daysRemaining != null) {
                    BudgetStatItem(
                        label = "Days Left",
                        value = "${budgetStatus.daysRemaining}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Projection warning
            if (budgetStatus.projectedOverBudget) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Warning.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Projected to exceed budget",
                                color = Warning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormatter.format(budgetStatus.projectedTotal),
                            color = Warning,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Budget alert level badge.
 */
@Composable
private fun BudgetAlertBadge(
    alertLevel: BudgetAlertLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (alertLevel) {
        BudgetAlertLevel.NORMAL -> Triple(
            IncomeGreen.copy(alpha = 0.15f),
            IncomeGreen,
            "On Track"
        )
        BudgetAlertLevel.WARNING -> Triple(
            Warning.copy(alpha = 0.15f),
            Warning,
            "Warning"
        )
        BudgetAlertLevel.EXCEEDED -> Triple(
            ExpenseRed.copy(alpha = 0.15f),
            ExpenseRed,
            "Exceeded"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Individual stat item.
 */
@Composable
private fun BudgetStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
