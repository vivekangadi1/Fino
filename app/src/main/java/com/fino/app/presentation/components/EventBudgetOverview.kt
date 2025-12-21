package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.*
import com.fino.app.util.AmountFormatter

@Composable
fun EventBudgetOverview(
    totalBudget: Double,
    totalQuoted: Double,
    totalPaid: Double,
    totalPending: Double,
    modifier: Modifier = Modifier
) {
    val percentUsed = if (totalBudget > 0) ((totalPaid / totalBudget) * 100).toFloat() else 0f
    val isOverBudget = totalBudget > 0 && totalPaid > totalBudget
    val deviation = if (totalBudget > 0) ((totalPaid - totalBudget) / totalBudget) * 100 else 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .padding(20.dp)
    ) {
        Text(
            text = "Budget Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BudgetStatItem(
                label = "Budget",
                amount = totalBudget,
                color = TextPrimary
            )
            BudgetStatItem(
                label = "Quoted",
                amount = totalQuoted,
                color = Info
            )
            BudgetStatItem(
                label = "Paid",
                amount = totalPaid,
                color = IncomeGreen
            )
            BudgetStatItem(
                label = "Pending",
                amount = totalPending,
                color = Warning
            )
        }

        if (totalBudget > 0) {
            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (percentUsed / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    isOverBudget -> ExpenseRed
                    percentUsed >= 75 -> Warning
                    else -> IncomeGreen
                },
                trackColor = DarkSurfaceHigh
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", percentUsed)}% used",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                val remaining = totalBudget - totalPaid
                Text(
                    text = if (remaining >= 0)
                        "${AmountFormatter.format(remaining)} remaining"
                    else
                        "${AmountFormatter.format(-remaining)} over",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (remaining >= 0) IncomeGreen else ExpenseRed
                )
            }

            // Deviation indicator
            if (deviation != 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (deviation > 0) ExpenseRed.copy(alpha = 0.2f)
                            else IncomeGreen.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (deviation > 0)
                            "⚠️ ${String.format("%.1f", deviation)}% over budget"
                        else
                            "✅ ${String.format("%.1f", -deviation)}% under budget",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (deviation > 0) ExpenseRed else IncomeGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetStatItem(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = AmountFormatter.formatCompact(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
