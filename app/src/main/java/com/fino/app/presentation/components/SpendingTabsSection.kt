package com.fino.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.CategorySpendingData
import com.fino.app.presentation.viewmodel.SpendingPeriod
import com.fino.app.util.AmountFormatter

/**
 * Spending breakdown section with period tabs and mini pie chart
 */
@Composable
fun SpendingTabsSection(
    selectedPeriod: SpendingPeriod,
    periodSpending: Double,
    categoryBreakdown: List<CategorySpendingData>,
    onPeriodSelected: (SpendingPeriod) -> Unit,
    onCategoryClick: (Long) -> Unit = {},
    onTotalClick: (SpendingPeriod) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Section Header
        Text(
            text = "Spending Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Period Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SpendingPeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) Primary.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .clickable { onPeriodSelected(period) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Primary else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spending Card with Pie Chart
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .padding(16.dp)
        ) {
            if (categoryBreakdown.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💸",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No spending ${selectedPeriod.displayName.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTotalClick(selectedPeriod) }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mini Pie Chart
                    MiniPieChart(
                        data = categoryBreakdown.take(5),
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Total and breakdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Spent",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = AmountFormatter.format(periodSpending),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Text(
                            text = "Tap to view transactions",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category List
                CategoryBreakdownList(
                    categories = categoryBreakdown.take(5),
                    onCategoryClick = onCategoryClick
                )

                if (categoryBreakdown.size > 5) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+${categoryBreakdown.size - 5} more categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Mini pie chart for category breakdown
 */
@Composable
fun MiniPieChart(
    data: List<CategorySpendingData>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Primary,
        Secondary,
        Accent,
        Warning,
        Info,
        ExpenseRed.copy(alpha = 0.7f),
        IncomeGreen.copy(alpha = 0.7f)
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 20.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        var startAngle = -90f // Start from top

        data.forEachIndexed { index, category ->
            val sweepAngle = category.percentage * 3.6f // Convert percentage to degrees
            val color = colors[index % colors.size]

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )

            startAngle += sweepAngle
        }

        // Draw remaining arc if percentages don't sum to 100
        val usedPercentage = data.sumOf { it.percentage.toDouble() }.toFloat()
        if (usedPercentage < 100) {
            val remainingAngle = (100 - usedPercentage) * 3.6f
            drawArc(
                color = DarkSurfaceHigh,
                startAngle = startAngle,
                sweepAngle = remainingAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

/**
 * List of categories with amounts and percentages
 */
@Composable
fun CategoryBreakdownList(
    categories: List<CategorySpendingData>,
    onCategoryClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Primary,
        Secondary,
        Accent,
        Warning,
        Info,
        ExpenseRed.copy(alpha = 0.7f),
        IncomeGreen.copy(alpha = 0.7f)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEachIndexed { index, category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCategoryClick(category.categoryId) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colors[index % colors.size])
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Emoji
                Text(
                    text = category.emoji,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Category name
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Amount and percentage
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = AmountFormatter.format(category.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${category.percentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Extension property for display name
 */
val SpendingPeriod.displayName: String
    get() = when (this) {
        SpendingPeriod.TODAY -> "Today"
        SpendingPeriod.THIS_WEEK -> "Week"
        SpendingPeriod.THIS_MONTH -> "Month"
        SpendingPeriod.THIS_YEAR -> "Year"
    }
