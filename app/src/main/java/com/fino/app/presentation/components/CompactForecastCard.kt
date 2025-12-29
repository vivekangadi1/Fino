package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.*
import com.fino.app.service.forecast.BudgetForecast
import com.fino.app.service.forecast.ForecastConfidence
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Compact forecast card for Home screen - shows essential forecast info with link to details
 */
@Composable
fun CompactForecastCard(
    forecast: BudgetForecast,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Primary, Secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Forecast info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Next Month Forecast",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    CompactConfidenceBadge(confidence = forecast.confidence)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currencyFormatter.format(forecast.totalForecast),
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = forecast.month.format(monthFormatter),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Chevron for navigation
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CompactConfidenceBadge(confidence: ForecastConfidence) {
    val (backgroundColor, textColor) = when (confidence) {
        ForecastConfidence.HIGH -> Pair(IncomeGreen.copy(alpha = 0.2f), IncomeGreen)
        ForecastConfidence.MEDIUM -> Pair(Warning.copy(alpha = 0.2f), Warning)
        ForecastConfidence.LOW -> Pair(Info.copy(alpha = 0.2f), Info)
        ForecastConfidence.INSUFFICIENT -> Pair(TextSecondary.copy(alpha = 0.2f), TextSecondary)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = confidence.displayText,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
