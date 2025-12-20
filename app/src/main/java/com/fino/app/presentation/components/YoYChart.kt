package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.TrendDirection
import com.fino.app.domain.model.YearOverYearComparison
import com.fino.app.presentation.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.NumberFormat
import java.util.Locale

@Composable
fun YoYChart(
    yoyComparison: YearOverYearComparison,
    modifier: Modifier = Modifier
) {
    SlideInCard(delay = 200, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Year-over-Year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${yoyComparison.monthName} Spending Trends",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vico Column Chart
            if (yoyComparison.yearlyData.isNotEmpty()) {
                val entries = yoyComparison.yearlyData.reversed().mapIndexed { index, data ->
                    index.toFloat() to data.totalSpent.toFloat()
                }
                val chartEntryModel = entryModelOf(*entries.toTypedArray())

                Chart(
                    chart = columnChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Average spending indicator
                val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Average:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "₹${numberFormat.format(yoyComparison.averageSpending.toInt())}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                // Highest/Lowest years
                if (yoyComparison.highestYear != null && yoyComparison.lowestYear != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Highest:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${yoyComparison.highestYear.year} (₹${numberFormat.format(yoyComparison.highestYear.totalSpent.toInt())})",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Lowest:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${yoyComparison.lowestYear.year} (₹${numberFormat.format(yoyComparison.lowestYear.totalSpent.toInt())})",
                            style = MaterialTheme.typography.bodySmall,
                            color = IncomeGreen
                        )
                    }
                }

                // Year-over-year changes
                if (yoyComparison.yearOverYearChanges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Year-over-Year Changes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(yoyComparison.yearOverYearChanges) { change ->
                            YoYChangeCard(change)
                        }
                    }
                }
            } else {
                // No data available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No year-over-year data available for ${yoyComparison.monthName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun YoYChangeCard(
    change: com.fino.app.domain.model.YearOverYearChange
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val changeColor = when {
        change.spendingChange > 0 -> ExpenseRed // Increase in spending
        change.spendingChange < 0 -> IncomeGreen // Decrease in spending
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Year range
            Text(
                text = "${change.fromYear} → ${change.toYear}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Change with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (change.spendingChange > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = changeColor,
                    modifier = Modifier.size(16.dp)
                )

                Column {
                    Text(
                        text = "${if (change.spendingChange > 0) "+" else ""}${String.format("%.1f", change.spendingChangePercentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = changeColor
                    )

                    Text(
                        text = "₹${numberFormat.format(kotlin.math.abs(change.spendingChange).toInt())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Income change
            Text(
                text = "Income: ${if (change.incomeChange > 0) "+" else ""}${String.format("%.1f", change.incomeChangePercentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = if (change.incomeChange > 0) IncomeGreen else ExpenseRed
            )
        }
    }
}
