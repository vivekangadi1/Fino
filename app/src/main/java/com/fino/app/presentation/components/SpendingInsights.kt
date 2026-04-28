package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.*

/**
 * Types of spending insights
 */
enum class InsightType {
    SPENDING_INCREASE,
    SPENDING_DECREASE,
    CATEGORY_ANOMALY,
    TIP,
    MILESTONE
}

/**
 * Data class representing a spending insight
 */
data class SpendingInsight(
    val type: InsightType,
    val title: String,
    val message: String,
    val emoji: String = "",
    val percentageChange: Float? = null,
    val category: String? = null
)

/**
 * Card displaying spending insights
 */
@Composable
fun SpendingInsightsCard(
    insights: List<SpendingInsight>,
    modifier: Modifier = Modifier
) {
    if (insights.isEmpty()) return

    SlideInCard(delay = 350, modifier = modifier) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "${insights.size} tips",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Insights list
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                insights.forEach { insight ->
                    InsightRow(insight = insight)
                }
            }
        }
    }
}

@Composable
private fun InsightRow(insight: SpendingInsight) {
    val (icon, iconColor, bgColor) = when (insight.type) {
        InsightType.SPENDING_INCREASE -> Triple(
            Icons.Default.TrendingUp,
            ExpenseRed,
            ExpenseRed.copy(alpha = 0.1f)
        )
        InsightType.SPENDING_DECREASE -> Triple(
            Icons.Default.TrendingDown,
            IncomeGreen,
            IncomeGreen.copy(alpha = 0.1f)
        )
        InsightType.CATEGORY_ANOMALY -> Triple(
            Icons.Default.Warning,
            Warning,
            Warning.copy(alpha = 0.1f)
        )
        InsightType.TIP -> Triple(
            Icons.Default.Lightbulb,
            Info,
            Info.copy(alpha = 0.1f)
        )
        InsightType.MILESTONE -> Triple(
            Icons.Default.Lightbulb,
            Primary,
            Primary.copy(alpha = 0.1f)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (insight.emoji.isNotEmpty()) {
                Text(
                    text = insight.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = insight.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Percentage change if available
        if (insight.percentageChange != null) {
            Text(
                text = "${if (insight.percentageChange > 0) "+" else ""}${insight.percentageChange.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = iconColor
            )
        }
    }
}

/**
 * Generate insights from spending data
 */
fun generateSpendingInsights(
    currentMonthSpending: Double,
    lastMonthSpending: Double?,
    topCategory: String?,
    topCategoryPercentage: Float?,
    averageSpending: Double?
): List<SpendingInsight> {
    val insights = mutableListOf<SpendingInsight>()

    // Month-over-month change insight
    if (lastMonthSpending != null && lastMonthSpending > 0) {
        val change = ((currentMonthSpending - lastMonthSpending) / lastMonthSpending) * 100
        when {
            change > 20 -> {
                insights.add(
                    SpendingInsight(
                        type = InsightType.SPENDING_INCREASE,
                        title = "Spending Up",
                        message = "You spent more this month compared to last month",
                        percentageChange = change.toFloat()
                    )
                )
            }
            change < -15 -> {
                insights.add(
                    SpendingInsight(
                        type = InsightType.SPENDING_DECREASE,
                        title = "Great Progress!",
                        message = "You reduced spending compared to last month",
                        percentageChange = change.toFloat()
                    )
                )
            }
        }
    }

    // Category dominance warning
    if (topCategory != null && topCategoryPercentage != null && topCategoryPercentage > 40) {
        insights.add(
            SpendingInsight(
                type = InsightType.CATEGORY_ANOMALY,
                emoji = "??????",
                title = "$topCategory dominates",
                message = "Consider diversifying or reviewing this category",
                percentageChange = topCategoryPercentage
            )
        )
    }

    // Average spending comparison
    if (averageSpending != null && averageSpending > 0) {
        val vsAverage = ((currentMonthSpending - averageSpending) / averageSpending) * 100
        if (vsAverage > 30) {
            insights.add(
                SpendingInsight(
                    type = InsightType.TIP,
                    emoji = "????",
                    title = "Above Average",
                    message = "This month's spending is higher than your average",
                    percentageChange = vsAverage.toFloat()
                )
            )
        } else if (vsAverage < -20) {
            insights.add(
                SpendingInsight(
                    type = InsightType.MILESTONE,
                    emoji = "????",
                    title = "Below Average!",
                    message = "You're spending less than your typical month",
                    percentageChange = vsAverage.toFloat()
                )
            )
        }
    }

    return insights.take(3) // Limit to 3 insights
}
