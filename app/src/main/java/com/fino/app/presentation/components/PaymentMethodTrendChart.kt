package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.PaymentMethodTrend
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
fun PaymentMethodTrendChart(
    paymentMethodTrend: PaymentMethodTrend,
    modifier: Modifier = Modifier
) {
    SlideInCard(delay = 250, modifier = modifier) {
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
                        text = "Payment Method Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "How you prefer to pay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferred method
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Preferred Method",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = paymentMethodTrend.preferredMethod,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Text(
                    text = String.format("%.1f%%", paymentMethodTrend.preferredMethodPercentage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-line chart showing all payment methods
            if (paymentMethodTrend.monthlyUsage.isNotEmpty()) {
                val upiEntries = paymentMethodTrend.monthlyUsage.mapIndexed { index, usage ->
                    index.toFloat() to usage.upiAmount.toFloat()
                }
                val creditCardEntries = paymentMethodTrend.monthlyUsage.mapIndexed { index, usage ->
                    index.toFloat() to usage.creditCardAmount.toFloat()
                }
                val debitCardEntries = paymentMethodTrend.monthlyUsage.mapIndexed { index, usage ->
                    index.toFloat() to usage.debitCardAmount.toFloat()
                }

                // For now, show UPI trend (in production, would show multiple lines)
                val chartEntryModel = entryModelOf(*upiEntries.toTypedArray())

                Chart(
                    chart = lineChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Payment method breakdown
            Text(
                text = "Payment Method Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show trend cards for each payment method
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // UPI Card
                item {
                    PaymentMethodCard(
                        methodName = "UPI",
                        amount = paymentMethodTrend.monthlyUsage.lastOrNull()?.upiAmount ?: 0.0,
                        trendDirection = paymentMethodTrend.trendDirection["UPI"],
                        color = Color(0xFF4CAF50)
                    )
                }

                // Credit Card
                item {
                    PaymentMethodCard(
                        methodName = "Credit Card",
                        amount = paymentMethodTrend.monthlyUsage.lastOrNull()?.creditCardAmount ?: 0.0,
                        trendDirection = paymentMethodTrend.trendDirection["Credit Card"],
                        color = Color(0xFFFF9800)
                    )
                }

                // Debit Card
                item {
                    PaymentMethodCard(
                        methodName = "Debit Card",
                        amount = paymentMethodTrend.monthlyUsage.lastOrNull()?.debitCardAmount ?: 0.0,
                        trendDirection = paymentMethodTrend.trendDirection["Debit Card"],
                        color = Color(0xFF2196F3)
                    )
                }

                // Cash
                item {
                    PaymentMethodCard(
                        methodName = "Cash",
                        amount = paymentMethodTrend.monthlyUsage.lastOrNull()?.cashAmount ?: 0.0,
                        trendDirection = paymentMethodTrend.trendDirection["Cash"],
                        color = Color(0xFF9C27B0)
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    methodName: String,
    amount: Double,
    trendDirection: TrendDirection?,
    color: Color
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Method indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, CircleShape)
                )

                Text(
                    text = methodName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            Text(
                text = "₹${numberFormat.format(amount.toInt())}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trend indicator
            if (trendDirection != null && amount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val (icon, trendColor) = when (trendDirection) {
                        TrendDirection.INCREASING -> Icons.Default.TrendingUp to IncomeGreen
                        TrendDirection.DECREASING -> Icons.Default.TrendingDown to ExpenseRed
                        TrendDirection.STABLE -> Icons.Default.TrendingFlat to Info
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(14.dp)
                    )

                    Text(
                        text = trendDirection.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = trendColor
                    )
                }
            }
        }
    }
}
