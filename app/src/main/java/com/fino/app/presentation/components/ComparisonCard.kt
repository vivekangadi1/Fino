package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.CategoryComparison
import com.fino.app.domain.model.PeriodComparison
import com.fino.app.domain.model.TrendDirection
import com.fino.app.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ComparisonCard(
    comparison: PeriodComparison,
    currentLabel: String,
    previousLabel: String,
    modifier: Modifier = Modifier
) {
    SlideInCard(delay = 100, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Text(
                text = "Period Comparison",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current vs Previous labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Text(
                    text = "vs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = previousLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Income comparison
            ComparisonRow(
                label = "Income",
                currentAmount = comparison.currentPeriod.totalIncome,
                previousAmount = comparison.previousPeriod.totalIncome,
                change = comparison.incomeChange,
                changePercentage = comparison.incomeChangePercentage,
                isPositiveGood = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Expenses comparison
            ComparisonRow(
                label = "Expenses",
                currentAmount = comparison.currentPeriod.totalExpenses,
                previousAmount = comparison.previousPeriod.totalExpenses,
                change = comparison.expensesChange,
                changePercentage = comparison.expensesChangePercentage,
                isPositiveGood = false // Lower expenses are good
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Net balance comparison
            ComparisonRow(
                label = "Net Balance",
                currentAmount = comparison.currentPeriod.netBalance,
                previousAmount = comparison.previousPeriod.netBalance,
                change = comparison.netBalanceChange,
                changePercentage = null, // Don't show percentage for net balance
                isPositiveGood = true
            )

            // Category breakdown header
            if (comparison.categoryComparisons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Show top 5 categories by absolute change
                comparison.categoryComparisons.take(5).forEach { categoryComparison ->
                    CategoryComparisonRow(categoryComparison)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    currentAmount: Double,
    previousAmount: Double,
    change: Double,
    changePercentage: Float?,
    isPositiveGood: Boolean
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Change indicator
                val isIncrease = change > 0
                val isGoodChange = if (isPositiveGood) isIncrease else !isIncrease
                val changeColor = when {
                    kotlin.math.abs(change) < 0.01 -> TextSecondary
                    isGoodChange -> IncomeGreen
                    else -> ExpenseRed
                }

                if (kotlin.math.abs(change) >= 0.01) {
                    Icon(
                        imageVector = if (isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = "₹${numberFormat.format(currentAmount.toInt())}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        // Change details
        if (kotlin.math.abs(change) >= 0.01) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val changeText = buildString {
                    if (change > 0) append("+")
                    append("₹${numberFormat.format(kotlin.math.abs(change).toInt())}")
                    if (changePercentage != null) {
                        append(" (")
                        if (changePercentage > 0) append("+")
                        append(String.format("%.1f", changePercentage))
                        append("%)")
                    }
                }

                val isIncrease = change > 0
                val isGoodChange = if (isPositiveGood) isIncrease else !isIncrease
                val changeColor = if (isGoodChange) IncomeGreen else ExpenseRed

                Text(
                    text = changeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = changeColor
                )
            }
        }
    }
}

@Composable
private fun CategoryComparisonRow(
    categoryComparison: CategoryComparison
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryComparison.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "₹${numberFormat.format(categoryComparison.currentAmount.toInt())} → ₹${numberFormat.format(categoryComparison.previousAmount.toInt())}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Change indicator
        val changeColor = when {
            categoryComparison.change > 0 -> ExpenseRed // Increase in expenses is bad
            categoryComparison.change < 0 -> IncomeGreen // Decrease in expenses is good
            else -> TextSecondary
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (categoryComparison.change > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = changeColor,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "${if (categoryComparison.change > 0) "+" else ""}${String.format("%.1f", categoryComparison.changePercentage)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = changeColor
            )
        }
    }
}
