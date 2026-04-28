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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.TrendDirection
import com.fino.app.domain.model.YearOverYearComparison
import com.fino.app.domain.model.YearlySpendingData
import com.fino.app.presentation.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.marker.Marker
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

            // Handle different data states
            when {
                yoyComparison.yearlyData.isEmpty() -> {
                    // No data available
                    EmptyYoYState(monthName = yoyComparison.monthName)
                }
                yoyComparison.yearlyData.size == 1 -> {
                    // Single year - show card instead of chart
                    SingleYearCard(
                        data = yoyComparison.yearlyData.first(),
                        monthName = yoyComparison.monthName
                    )
                }
                else -> {
                    // Multiple years - show bar chart with proper labels
                    val reversedData = yoyComparison.yearlyData.reversed()

                    // Create year labels
                    val yearLabels = remember(reversedData) {
                        reversedData.map { it.year.toString() }
                    }

                    // Custom x-axis formatter for year labels
                    val bottomAxisFormatter = remember(yearLabels) {
                        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                            yearLabels.getOrElse(value.toInt()) { "" }
                        }
                    }

                    // Custom y-axis formatter for rupee formatting
                    val startAxisFormatter = remember {
                        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
                            formatRupeeAxisYoY(value)
                        }
                    }

                    val entries = reversedData.mapIndexed { index, data ->
                        index.toFloat() to data.totalSpent.toFloat()
                    }
                    val chartEntryModel = entryModelOf(*entries.toTypedArray())

                    // Create marker for tap interactivity
                    val marker = rememberYoYMarker()

                    ProvideChartStyle(m3ChartStyle()) {
                        Chart(
                            chart = columnChart(),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(valueFormatter = startAxisFormatter),
                            bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
                            marker = marker,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }

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

/**
 * Empty state when no YoY data is available
 */
@Composable
private fun EmptyYoYState(monthName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📊",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No data available for $monthName",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "Keep tracking to see year comparisons",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

/**
 * Card shown when only one year of data exists
 */
@Composable
private fun SingleYearCard(data: YearlySpendingData, monthName: String) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Primary.copy(alpha = 0.2f), Secondary.copy(alpha = 0.2f))
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "$monthName ${data.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spending",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "₹${numberFormat.format(data.totalSpent.toInt())}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "₹${numberFormat.format(data.totalIncome.toInt())}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${data.transactionCount} transactions",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "📈 Track more months to see year-over-year comparisons",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

/**
 * Format rupee values for y-axis display
 */
private fun formatRupeeAxisYoY(value: Float): String {
    return when {
        value >= 100000 -> "₹${String.format("%.1f", value / 100000)}L"
        value >= 1000 -> "₹${String.format("%.0f", value / 1000)}K"
        else -> "₹${value.toInt()}"
    }
}

/**
 * Create a marker for YoY chart tap interactivity
 */
@Composable
private fun rememberYoYMarker(): Marker {
    val labelComponent = textComponent(
        color = androidx.compose.ui.graphics.Color.White,
        background = shapeComponent(
            shape = Shapes.roundedCornerShape(allPercent = 25),
            color = DarkSurfaceHigh
        ),
        padding = dimensionsOf(horizontal = 8.dp, vertical = 4.dp)
    )

    return remember {
        MarkerComponent(
            label = labelComponent,
            indicator = null,
            guideline = null
        )
    }
}
