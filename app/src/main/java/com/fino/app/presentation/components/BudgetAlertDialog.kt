package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fino.app.domain.model.BudgetProgress
import com.fino.app.domain.model.BudgetStatus
import com.fino.app.presentation.theme.*
import com.fino.app.util.AmountFormatter

/**
 * Alert dialog when budget reaches 75% or 100% threshold
 */
@Composable
fun BudgetAlertDialog(
    budgetProgress: BudgetProgress,
    onDismiss: () -> Unit
) {
    val isOverBudget = budgetProgress.status == BudgetStatus.OVER_BUDGET
    val alertTitle = if (isOverBudget) {
        "Budget Exceeded!"
    } else {
        "Budget Alert"
    }

    val alertMessage = if (isOverBudget) {
        "You have exceeded your ${budgetProgress.categoryName} budget by ₹${AmountFormatter.format(-budgetProgress.remaining)}"
    } else {
        "You have reached ${(budgetProgress.percentage * 100).toInt()}% of your ${budgetProgress.categoryName} budget"
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isOverBudget) ExpenseRed.copy(alpha = 0.15f) else Warning.copy(alpha = 0.15f),
                            DarkSurface
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isOverBudget) ExpenseRed else Warning,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = alertTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Message
                Text(
                    text = alertMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Budget details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Spent",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${AmountFormatter.format(budgetProgress.spent)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) ExpenseRed else Warning
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp),
                        color = DarkSurfaceHigh
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Budget",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${AmountFormatter.format(budgetProgress.budget.monthlyLimit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dismiss button
                BouncyButton(
                    onClick = onDismiss,
                    gradient = if (isOverBudget) FinoGradients.Expense else Brush.linearGradient(
                        colors = listOf(Warning, Warning.copy(alpha = 0.8f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Got it",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
