package com.fino.app.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.*
import com.fino.app.service.forecast.BudgetForecast
import com.fino.app.service.forecast.ForecastConfidence
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ForecastCard(
    forecast: BudgetForecast,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Budget Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            ConfidenceBadge(confidence = forecast.confidence)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main forecast card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month and total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = forecast.month.format(monthFormatter),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = currencyFormatter.format(forecast.totalForecast),
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Current month progress
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Current month",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = currencyFormatter.format(forecast.currentMonthSpent),
                            color = when {
                                forecast.percentageOfForecast > 1.0f -> ExpenseRed
                                forecast.percentageOfForecast > 0.75f -> Warning
                                else -> IncomeGreen
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(forecast.percentageOfForecast * 100).toInt()}% of forecast",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkSurfaceHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = forecast.percentageOfForecast.coerceAtMost(1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = when {
                                        forecast.percentageOfForecast > 1.0f -> listOf(ExpenseRed, ExpenseRed.copy(alpha = 0.7f))
                                        forecast.percentageOfForecast > 0.75f -> listOf(Warning, Warning.copy(alpha = 0.7f))
                                        else -> listOf(IncomeGreen, IncomeGreen.copy(alpha = 0.7f))
                                    }
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown: Recurring vs Variable
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ForecastBreakdownItem(
                        label = "Recurring Bills",
                        amount = forecast.recurringTotal,
                        color = Primary
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(Border)
                    )
                    ForecastBreakdownItem(
                        label = "Variable Spending",
                        amount = forecast.variableTotal,
                        color = Secondary
                    )
                }

                // Expandable category breakdown
                if (forecast.categoryForecasts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "Hide breakdown" else "Show category breakdown",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            forecast.categoryForecasts.take(6).forEach { category ->
                                CategoryForecastRow(
                                    emoji = category.emoji,
                                    name = category.categoryName,
                                    amount = category.forecastAmount,
                                    isRecurring = category.isRecurring
                                )
                            }
                            if (forecast.categoryForecasts.size > 6) {
                                Text(
                                    text = "+ ${forecast.categoryForecasts.size - 6} more categories",
                                    color = TextTertiary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(
    confidence: ForecastConfidence
) {
    val (color, bgColor) = when (confidence) {
        ForecastConfidence.HIGH -> Pair(IncomeGreen, IncomeGreen.copy(alpha = 0.15f))
        ForecastConfidence.MEDIUM -> Pair(Warning, Warning.copy(alpha = 0.15f))
        ForecastConfidence.LOW -> Pair(Info, Info.copy(alpha = 0.15f))
        ForecastConfidence.INSUFFICIENT -> Pair(TextTertiary, DarkSurfaceHigh)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = confidence.displayText,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ForecastBreakdownItem(
    label: String,
    amount: Double,
    color: Color
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = currencyFormatter.format(amount),
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CategoryForecastRow(
    emoji: String,
    name: String,
    amount: Double,
    isRecurring: Boolean
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            if (isRecurring) {
                Text(
                    text = "Recurring",
                    color = Primary,
                    fontSize = 11.sp
                )
            }
        }
        Text(
            text = currencyFormatter.format(amount),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
