package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.BudgetProgress
import com.fino.app.domain.model.BudgetStatus
import com.fino.app.presentation.theme.*
import com.fino.app.util.AmountFormatter

/**
 * Budget progress card showing budget vs spent with color-coded progress bar
 */
@Composable
fun BudgetProgressCard(
    budgetProgress: BudgetProgress,
    modifier: Modifier = Modifier
) {
    val progressColor = when (budgetProgress.status) {
        BudgetStatus.UNDER_BUDGET -> IncomeGreen
        BudgetStatus.APPROACHING_LIMIT -> Warning
        BudgetStatus.OVER_BUDGET -> ExpenseRed
    }

    val progressGradient = when (budgetProgress.status) {
        BudgetStatus.UNDER_BUDGET -> Brush.linearGradient(
            colors = listOf(IncomeGreen, IncomeGreen.copy(alpha = 0.7f))
        )
        BudgetStatus.APPROACHING_LIMIT -> Brush.linearGradient(
            colors = listOf(Warning, Warning.copy(alpha = 0.7f))
        )
        BudgetStatus.OVER_BUDGET -> Brush.linearGradient(
            colors = listOf(ExpenseRed, ExpenseRed.copy(alpha = 0.7f))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budgetProgress.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Text(
                    text = "${(budgetProgress.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Spending vs Budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = AmountFormatter.format(budgetProgress.spent),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = AmountFormatter.format(budgetProgress.budget.monthlyLimit),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            AnimatedGradientProgress(
                progress = budgetProgress.percentage.coerceAtMost(1.0f),
                gradient = progressGradient,
                backgroundColor = DarkSurfaceHigh,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Remaining amount
            val remainingText = if (budgetProgress.remaining >= 0) {
                "₹${AmountFormatter.format(budgetProgress.remaining)} remaining"
            } else {
                "₹${AmountFormatter.format(-budgetProgress.remaining)} over budget"
            }

            Text(
                text = remainingText,
                style = MaterialTheme.typography.bodySmall,
                color = if (budgetProgress.remaining >= 0) TextSecondary else ExpenseRed
            )
        }
    }
}
