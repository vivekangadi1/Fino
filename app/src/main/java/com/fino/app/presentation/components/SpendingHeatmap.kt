package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.*
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Data for heatmap visualization
 */
data class MonthSpendingData(
    val yearMonth: YearMonth,
    val totalSpent: Double,
    val intensity: Float // 0.0 to 1.0
)

@Composable
fun SpendingHeatmap(
    year: Int,
    monthlySpending: List<MonthSpendingData>,
    selectedMonth: YearMonth?,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Year label
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 3x4 grid for 12 months
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items((1..12).toList()) { month ->
                val yearMonth = YearMonth.of(year, month)
                val spendingData = monthlySpending.find { it.yearMonth == yearMonth }
                val isSelected = yearMonth == selectedMonth
                val isFuture = yearMonth > YearMonth.now()

                MonthCell(
                    month = month,
                    intensity = spendingData?.intensity ?: 0f,
                    isSelected = isSelected,
                    isFuture = isFuture,
                    onClick = {
                        if (!isFuture) {
                            onMonthSelected(yearMonth)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MonthCell(
    month: Int,
    intensity: Float,
    isSelected: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    val monthName = YearMonth.of(2024, month).format(DateTimeFormatter.ofPattern("MMM"))

    // Calculate color based on intensity
    val backgroundColor = when {
        isFuture -> DarkSurfaceVariant.copy(alpha = 0.3f)
        isSelected -> Primary
        intensity == 0f -> DarkSurfaceVariant.copy(alpha = 0.5f)
        intensity < 0.25f -> ExpenseRed.copy(alpha = 0.3f)
        intensity < 0.5f -> ExpenseRed.copy(alpha = 0.5f)
        intensity < 0.75f -> ExpenseRed.copy(alpha = 0.7f)
        else -> ExpenseRed.copy(alpha = 0.9f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(enabled = !isFuture) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = monthName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isFuture) TextSecondary.copy(alpha = 0.5f) else TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}
