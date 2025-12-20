package com.fino.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.NumberFormat
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

            // Vico Line Chart
            val chartEntryModel = entryModelOf(
                *trendData.periods.mapIndexed { index, period ->
                    index.toFloat() to period.totalSpent.toFloat()
                }.toTypedArray()
            )

            Chart(
                chart = lineChart(),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

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
