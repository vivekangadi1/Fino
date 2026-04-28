package com.fino.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.SpendingTrend
import com.fino.app.domain.model.TrendDirection
import com.fino.app.presentation.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
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
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TrendLineChart(
    trendData: SpendingTrend,
    modifier: Modifier = Modifier
) {
    SlideInCard(delay = 150, modifier = modifier) {
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
                Text(
                    text = "Spending Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                TrendIndicator(
                    direction = trendData.trendDirection,
                    percentageChange = trendData.percentageChange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create month labels from data
            val monthLabels = remember(trendData) {
                trendData.periods.map { period ->
                    period.yearMonth.format(DateTimeFormatter.ofPattern("MMM"))
                }
            }

            // Custom x-axis formatter for month names
            val bottomAxisFormatter = remember(monthLabels) {
                AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                    monthLabels.getOrElse(value.toInt()) { "" }
                }
            }

            // Custom y-axis formatter for rupee formatting (₹1.5L, ₹50K)
            val startAxisFormatter = remember {
                AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
                    formatRupeeAxis(value)
                }
            }

            // Vico Line Chart
            val chartEntryModel = entryModelOf(
                *trendData.periods.mapIndexed { index, period ->
                    index.toFloat() to period.totalSpent.toFloat()
                }.toTypedArray()
            )

            // Create marker for tap interactivity
            val marker = rememberChartMarker()

            ProvideChartStyle(m3ChartStyle()) {
                Chart(
                    chart = lineChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(valueFormatter = startAxisFormatter),
                    bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisFormatter),
                    marker = marker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Average spending indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Average Spending:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "₹${NumberFormat.getNumberInstance(Locale("en", "IN")).format(trendData.averageSpending.toInt())}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun TrendIndicator(
    direction: TrendDirection,
    percentageChange: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val (icon, color) = when (direction) {
            TrendDirection.INCREASING -> Icons.Default.TrendingUp to ExpenseRed
            TrendDirection.DECREASING -> Icons.Default.TrendingDown to IncomeGreen
            TrendDirection.STABLE -> Icons.Default.TrendingFlat to Info
        }

        Icon(
            imageVector = icon,
            contentDescription = direction.name,
            tint = color,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = "${if (percentageChange > 0) "+" else ""}${String.format("%.1f", percentageChange)}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * Format rupee values for y-axis display
 * Examples: 150000 -> "₹1.5L", 50000 -> "₹50K", 500 -> "₹500"
 */
private fun formatRupeeAxis(value: Float): String {
    return when {
        value >= 100000 -> "₹${String.format("%.1f", value / 100000)}L"
        value >= 1000 -> "₹${String.format("%.0f", value / 1000)}K"
        else -> "₹${value.toInt()}"
    }
}

/**
 * Create a marker for chart tap interactivity
 */
@Composable
private fun rememberChartMarker(): Marker {
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
